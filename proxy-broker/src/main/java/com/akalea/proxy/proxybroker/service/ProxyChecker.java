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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
import com.akalea.proxy.proxybroker.utils.HttpUtils;
import com.akalea.proxy.proxybroker.utils.ThreadUtils;
import com.google.common.collect.Lists;

public class ProxyChecker {

    private final static Logger logger = LoggerFactory.getLogger(ProxyChecker.class);

    private ExecutorService checkExecutor;
    private ExecutorService validationRunExecutor;

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
            checkExecutor = Executors.newFixedThreadPool(connectionCount);
            IntStream
                .range(0, connectionCount)
                .mapToObj(i -> proxyValidator())
                .forEach(v -> checkExecutor.execute(v));

            if (isValidationRunsEnabled()) {
                validationRunExecutor = Executors.newFixedThreadPool(1);
                validationRunExecutor.execute(validationRun());
            }
        } catch (Exception e) {
            logger.error("Error starting checker", e);
            throw new RuntimeException(e);
        }
    }

    public void check(List<Proxy> proxies) {
        Consumer<Proxy> dummyHandler = (p) -> {
            return;
        };
        check(proxies, dummyHandler);
    }

    public void check(List<Proxy> proxies, Consumer<Proxy> checkedProxyHandler) {
        if (!started)
            start();
        long submitted =
            proxies
                .stream()
                .map(p -> new ProxyCheckTask().setProxy(p).setHandler(checkedProxyHandler))
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
                    checkProxy(this.checkingTasks.remove(uuid));
                } catch (Exception e) {
                    logger.error("Error processing check task", e);
                }
            }
        };
    }

    public void checkProxy(ProxyCheckTask task) {
        Proxy proxy = task.getProxy();
        logger.debug(String.format("Checking proxy %s", proxy.getUrl()));
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
                "Proxy %s is %s",
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
            logger.debug(String.format("Proxying error w/ proxy %s", proxy.getUrl()), e);
            return false;
        }
    }

    private Runnable validationRun() {
        return () -> {
            while (isValidationRunsEnabled()) {
                List<Proxy> proxies = broker.findProxies(new ProxyQuery().setStatus(null));
                if (!proxies.isEmpty()) {
                    logger.debug(String.format("Validation run w/ %d proxies", proxies.size()));
                    check(proxies);
                }
                ThreadUtils.sleep(getValidationRunDelaySeconds() * 1000);
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

    private ProxyCheckPolicy getProxyCheckPolicy() {
        return this.properties.getProxy().getProxies().getCheck();
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
