package com.akalea.proxy.proxybroker.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyQuery;
import com.akalea.proxy.proxybroker.domain.ProxyStatus;
import com.akalea.proxy.proxybroker.domain.ProxyValidator;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties.ProxyCheckPolicy;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties.ProxyEvictionPolicy;
import com.akalea.proxy.proxybroker.utils.HttpUtils;
import com.akalea.proxy.proxybroker.utils.ThreadUtils;
import com.google.common.collect.Lists;

public class ProxyChecker {

    private final static Logger logger = LoggerFactory.getLogger(ProxyChecker.class);

    private ExecutorService checkExecutor;
    private ExecutorService validationRunExecutor;
    private ExecutorService evictionRunExecutor;

    private ProxyProperties properties;

    private LinkedBlockingQueue<String> checking      = new LinkedBlockingQueue<>();
    private Map<String, ProxyCheckTask> checkingTasks =
        new HashMap<String, ProxyChecker.ProxyCheckTask>();

    private boolean enabled = true;
    private boolean started = false;

    private ProxyBroker broker;

    public ProxyChecker(ProxyBroker broker) {
        this(ProxyConfiguration.proxyProperties(), broker);
    }

    public ProxyChecker(ProxyProperties properties, ProxyBroker broker) {
        this.properties = properties;
        this.broker = broker;
    }

    public void start() {
        this.started = true;
        try {
            int connectionCount =
                this.properties
                    .getProxy()
                    .getProxies()
                    .getCheck()
                    .getValidationMaxConnectionsCount();
            checkExecutor =
                Executors.newFixedThreadPool(
                    connectionCount,
                    new ThreadFactory() {
                        public Thread newThread(Runnable r) {
                            Thread thread = new Thread(r);
                            thread.setName("ProxyFetcherValidator");
                            return thread;
                        }
                    });
            IntStream
                .range(0, connectionCount)
                .mapToObj(i -> proxyValidator())
                .forEach(v -> checkExecutor.execute(v));

            if (isValidationRunsEnabled()) {
                validationRunExecutor =
                    Executors.newFixedThreadPool(
                        1,
                        new ThreadFactory() {
                            public Thread newThread(Runnable r) {
                                Thread thread = new Thread(r);
                                thread.setName("ProxyCheckerScheduler");
                                return thread;
                            }
                        });
                validationRunExecutor.execute(validationRun());
            }

            if (isEvictionRunsEnabled()) {
                evictionRunExecutor =
                    Executors.newFixedThreadPool(
                        1,
                        new ThreadFactory() {
                            public Thread newThread(Runnable r) {
                                Thread thread = new Thread(r);
                                thread.setName("ProxyCheckerEviction");
                                return thread;
                            }
                        });
                evictionRunExecutor.execute(evictionRun());
            }
        } catch (Exception e) {
            logger.error("Error starting checker", e);
            throw new RuntimeException(e);
        }
    }

    private void validateExistingProxies(List<Proxy> proxies) {
        Consumer<Proxy> handler = (p) -> {
            if (!ProxyStatus.ok.equals(p.getLastCheck()))
                broker.evictProxies(Lists.newArrayList(p.getHost()));
        };
        check(proxies, handler);
    }

    public void check(List<Proxy> proxies, Consumer<Proxy> checkedProxyHandler) {
        if (!started)
            start();
        long submitted =
            proxies
                .stream()
                .map(
                    p -> new ProxyCheckTask()
                        .setProxy(p)
                        .setHandler(checkedProxyHandler))
                .map(t -> check(t))
                .filter(r -> r)
                .count();
        logger.debug(String.format("Checking %d proxies", submitted));
    }

    private boolean check(ProxyCheckTask task) {
        String taskUuid = task.getProxy().getUrl();
        if (checking.contains(taskUuid))
            return false;
        checkingTasks.put(taskUuid, task);
        checking.offer(taskUuid);
        return true;
    }

    private Runnable proxyValidator() {
        return () -> {
            while (enabled) {
                try {
                    String uuid = this.checking.poll(1, TimeUnit.DAYS);
                    Optional
                        .ofNullable(this.checkingTasks.remove(uuid))
                        .ifPresent(p -> checkProxy(p));
                } catch (Exception e) {
                    logger.error("Error processing check task", e);
                }
            }
        };
    }

    public void checkProxy(ProxyCheckTask task) {
        if (task == null)
            return;
        Proxy proxy = task.getProxy();
        proxy.setLastCheckDate(LocalDateTime.now());

        List<ProxyValidator> validators = Lists.newArrayList(getProxyCheckPolicy().getValidators());
        int requiredValidationCount =
            java.lang.Math.min(
                getProxyCheckPolicy().getValidationMinValidatorsCount(),
                validators.size());
        Collections.shuffle(validators);
        long validatedCount =
            validators
                .subList(0, requiredValidationCount)
                .stream()
                .map(v -> isRequestProxyingOk(task.getProxy(), v.getUrl()))
                .filter(r -> r)
                .count();

        proxy.setLastCheck(
            validatedCount >= requiredValidationCount ? ProxyStatus.ok : ProxyStatus.ko);
        logger.debug(
            String.format(
                "Checked proxy %s, status is %s",
                proxy.getUrl(),
                proxy.getLastCheck().toString()));
        task.getHandler().accept(proxy);
    }

    private boolean isRequestProxyingOk(Proxy proxy, String targetUrl) {
        try {
            return HttpUtils
                .getRequest(targetUrl, proxy)
                .isSuccess();
        } catch (Exception e) {
            logger.debug(String.format("Proxying error w/ proxy %s", proxy.getUrl()));
            return false;
        }
    }

    private Runnable validationRun() {
        return () -> {
            while (isValidationRunsEnabled()) {
                try {
                    List<Proxy> proxies = broker.findProxies(new ProxyQuery().setStatus(null));
                    if (!proxies.isEmpty()) {
                        logger.info(
                            String.format("Validation run, checking %d proxies", proxies.size()));
                        validateExistingProxies(proxies);
                    }
                } catch (Exception e) {
                    logger.error("Error running validations", e);
                }
                ThreadUtils.sleep(getValidationRunDelaySeconds() * 1000);
            }
        };
    }

    private Runnable evictionRun() {
        return () -> {
            while (isEvictionRunsEnabled()) {
                List<String> evicted =
                    broker
                        .findProxies(new ProxyQuery().setStatus(null))
                        .stream()
                        .filter(p -> ProxyStatus.ko.equals(p.getLastCheck()))
                        .filter(
                            p -> p.getLastOkDate() != null
                                && p.getLastOkDate()
                                    .plusSeconds(getEvictionProxyMaxAge())
                                    .isBefore(LocalDateTime.now()))
                        .map(p -> p.getUrl())
                        .collect(Collectors.toList());
                if (!evicted.isEmpty()) {
                    logger.debug(
                        String.format("Eviction run, evicting %d proxies", evicted.size()));
                    broker.evictProxies(evicted);
                }
                ThreadUtils.sleep(getEvictionRunDelaySeconds() * 1000);
            }
        };
    }

    private int getValidationRunDelaySeconds() {
        return Optional
            .ofNullable(
                getProxyCheckPolicy()
                    .getTimeBetweenValidationRunsSeconds())
            .orElse(10 * 60);
    }

    private boolean isValidationRunsEnabled() {
        return getProxyCheckPolicy()
            .getTimeBetweenValidationRunsSeconds() > 0;
    }

    private int getEvictionRunDelaySeconds() {
        return Optional
            .ofNullable(
                getProxyEvictionPolicy().getEvictionIntervalSeconds())
            .orElse(10 * 60);
    }

    private boolean isEvictionRunsEnabled() {
        return Optional
            .ofNullable(getProxyEvictionPolicy().getEvictKoProxies())
            .orElse(false);
    }

    private int getEvictionProxyMaxAge() {
        return Optional
            .ofNullable(getProxyEvictionPolicy().getEvictionProxyMaxAgeSeconds())
            .orElse(4 * 60 * 60);
    }

    private ProxyCheckPolicy getProxyCheckPolicy() {
        return this.properties.getProxy().getProxies().getCheck();
    }

    private ProxyEvictionPolicy getProxyEvictionPolicy() {
        return this.properties.getProxy().getProxies().getEviction();
    }

    public boolean isStarted() {
        return started;
    }

    public static class ProxyCheckTask {
        private Proxy           proxy;
        private Consumer<Proxy> handler;

        public Proxy getProxy() {
            return proxy;
        }

        public ProxyCheckTask setProxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Consumer<Proxy> getHandler() {
            return handler;
        }

        public ProxyCheckTask setHandler(Consumer<Proxy> handler) {
            this.handler = handler;
            return this;
        }

    }
}
