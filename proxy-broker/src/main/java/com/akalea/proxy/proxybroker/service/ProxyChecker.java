package com.akalea.proxy.proxybroker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyStatus;

public class ProxyChecker {

    private Executor proxyCheckExecutor;

    public ProxyChecker() {
        proxyCheckExecutor = Executors.newFixedThreadPool(10);
    }

    public void check(List<Proxy> proxies, Consumer<Proxy> checkedProxyHandler) {
        proxies
            .stream()
            .forEach(p -> check(p, checkedProxyHandler));
    }

    public Proxy check(Proxy proxy, Consumer<Proxy> checkedProxyHandler) {
        proxyCheckExecutor.execute(() -> {
            proxy.setLastCheckDate(LocalDateTime.now());
            proxy.setLastCheck(ProxyStatus.ok);
            checkedProxyHandler.accept(proxy);
        });
        return proxy;
    }
}
