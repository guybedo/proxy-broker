package com.akalea.proxy.proxybroker.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyQuery;
import com.akalea.proxy.proxybroker.domain.ProxyStatus;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties;
import com.akalea.proxy.proxybroker.utils.ThreadUtils;

public class ProxyChecker {

    private final static Logger logger = LoggerFactory.getLogger(ProxyChecker.class);

    private Executor proxyCheckExecutor;
    private Executor validationRunExecutor;

    private ProxyProperties properties;

    private LinkedBlockingQueue<String> checking      = new LinkedBlockingQueue<>();
    private Map<String, ProxyCheckTask> checkingTasks =
        new HashMap<String, ProxyChecker.ProxyCheckTask>();

    private boolean isEnabled = true;

    private ProxyBroker broker;

    public ProxyChecker(ProxyBroker broker) {
        this(ProxyConfiguration.proxyProperties(), broker);
    }

    public ProxyChecker(ProxyProperties properties, ProxyBroker broker) {
        this.properties = properties;
        this.broker = broker;
        proxyCheckExecutor =
            Executors.newFixedThreadPool(
                this.properties
                    .getProxy()
                    .getProxies()
                    .getCheck()
                    .getValidationMaxConnectionsCount());
        IntStream
            .range(
                0,
                this.properties
                    .getProxy()
                    .getProxies()
                    .getCheck()
                    .getValidationMaxConnectionsCount())
            .mapToObj(i -> proxyValidator())
            .forEach(v -> proxyCheckExecutor.execute(v));

        if (isValidationRunsEnabled()) {
            validationRunExecutor = Executors.newFixedThreadPool(1);
            validationRunExecutor.execute(validationRun());
        }
    }

    public void check(List<Proxy> proxies) {
        Consumer<Proxy> dummyHandler = (p) -> {
            return;
        };
        check(proxies, dummyHandler);
    }

    public void check(List<Proxy> proxies, Consumer<Proxy> checkedProxyHandler) {
        long submitted =
            proxies
                .stream()
                .map(p -> new ProxyCheckTask().setProxy(p).setHandler(checkedProxyHandler))
                .map(t -> check(t))
                .filter(r -> r)
                .count();
        logger.debug(String.format("Submitted %d proxies for validation", submitted));
    }

    private boolean check(ProxyCheckTask task) {
        String taskUuid = task.getProxy().getUrl();
        if (checking.contains(taskUuid))
            return false;
        checking.offer(taskUuid);
        checkingTasks.put(taskUuid, task);
        return true;
    }

    private Runnable proxyValidator() {
        return () -> {
            while (isEnabled) {
                String uuid = this.checking.poll();
                checkProxy(this.checkingTasks.remove(uuid));
            }
        };
    }

    private void checkProxy(ProxyCheckTask task) {
        Proxy proxy = task.getProxy();
        logger.debug(String.format("Checking proxy %s", proxy.getUrl()));
        proxy.setLastCheckDate(LocalDateTime.now());
        proxy.setLastCheck(ProxyStatus.ok);
        task.getHandler().accept(proxy);
    }

    private Runnable validationRun() {
        return () -> {
            while (isValidationRunsEnabled()) {
                List<Proxy> proxies = broker.getProxies(new ProxyQuery());
                logger.debug(String.format("Validation run w/ %d proxies", proxies.size()));
                check(proxies);
                ThreadUtils.sleep(getValidationRunDelay());
            }
        };
    }

    private int getValidationRunDelay() {
        return Optional
            .ofNullable(
                this.properties
                    .getProxy()
                    .getProxies()
                    .getCheck()
                    .getTimeBetweenValidationRunsSeconds())
            .orElse(10 * 60);
    }

    private boolean isValidationRunsEnabled() {
        return this.properties
            .getProxy()
            .getProxies()
            .getCheck()
            .getTimeBetweenValidationRunsSeconds() > 0;
    }

    private static class ProxyCheckTask {
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
