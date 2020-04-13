package com.akalea.proxy.proxybroker.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyQuery;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties;
import com.akalea.proxy.proxybroker.utils.ThreadUtils;
import com.google.common.collect.Lists;

public class ProxyBroker {

    private final static Logger logger = LoggerFactory.getLogger(ProxyBroker.class);

    private ProxyProperties properties;

    private ProxyChecker checker;
    private ProxyFetcher fetcher;

    private Map<String, Proxy> proxies =
        Collections.synchronizedMap(new HashMap<String, Proxy>());

    private Random rand = new Random();

    public ProxyBroker() {
        this(ProxyConfiguration.proxyProperties());
    }

    public ProxyBroker(ProxyProperties properties) {
        this.properties = properties;
        this.checker = new ProxyChecker(this);
        this.fetcher = new ProxyFetcher(this, this.checker);
    }

    public ProxyBroker(
        ProxyProperties properties,
        ProxyFetcher fetcher,
        ProxyChecker checker) {
        super();
        this.properties = properties;
        this.checker = checker;
        this.fetcher = fetcher;
    }

    private void startComponentsIfNeeded() {
        if (!this.fetcher.isStarted())
            this.fetcher.start();
        if (!this.checker.isStarted())
            this.checker.start();
    }

    public Proxy randomProxy() {
        return randomProxy(null);
    }

    public Proxy randomProxy(Float minSuccessRate) {
        List<Proxy> valid = findProxies(new ProxyQuery().setMinSuccessRate(minSuccessRate));
        return valid.get(rand.nextInt(valid.size()));
    }

    public List<Proxy> findProxies(ProxyQuery query) {
        startComponentsIfNeeded();
        boolean wait = Optional.ofNullable(query.getWait()).orElse(false);
        if (!wait)
            return executeProxyQuery(query);

        Long maxWait =
            Optional
                .ofNullable(query.getMaxWait())
                .map(d -> d.getSeconds())
                .orElse(1 * 3600l);
        LocalDateTime start = LocalDateTime.now();
        while (start.plusSeconds(maxWait).isAfter(LocalDateTime.now())) {
            List<Proxy> found = executeProxyQuery(query);
            if (!found.isEmpty() && (query.getCount() == null || found.size() >= query.getCount()))
                return found;
            ThreadUtils.sleep(50);
        }
        return Lists.newArrayList();
    }

    public void addProxy(Proxy proxy, boolean overwrite) {
        if (this.proxies.containsKey(proxy.getUrl()) && !overwrite)
            return;
        this.proxies.put(proxy.getUrl(), proxy);
    }

    public void evictProxies(List<String> urls) {
        try {
            logger.info("Evicting %d ko proxies", urls.size());
            urls
                .stream()
                .forEach(u -> this.proxies.remove(u));
        } catch (Exception e) {
            logger.error("Error evicting proxies", e);
        }
    }

    private List<Proxy> executeProxyQuery(ProxyQuery query) {
        return this.proxies
            .values()
            .stream()
            .filter(p -> query.getStatus() == null || query.getStatus().equals(p.getLastCheck()))
            .filter(
                p -> p.getSuccessRate() >= Optional
                    .ofNullable(query.getMinSuccessRate())
                    .orElse(0f))
            .limit(
                Optional
                    .ofNullable(query.getCount())
                    .map(c -> c > 0 ? c : Integer.MAX_VALUE)
                    .orElse(Integer.MAX_VALUE))
            .collect(Collectors.toList());
    }

}
