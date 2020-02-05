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

public class ProxyBroker {

    private final static Logger logger = LoggerFactory.getLogger(ProxyBroker.class);

    private ProxyProperties properties;

    private ProxyChecker checker;
    private ProxyFetcher fetcher;

    private Map<String, Proxy> proxies =
        Collections.synchronizedMap(new HashMap<String, Proxy>());

    private Random rand = new Random();

    public ProxyBroker() {
        this.properties = ProxyConfiguration.proxyProperties();
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

    public void setup() {
        this.fetcher.start();
    }

    public Proxy randomProxy() {
        return randomProxy(null);
    }

    public Proxy randomProxy(Float minSuccessRate) {
        List<Proxy> valid = getProxies(new ProxyQuery().setMinSuccessRate(minSuccessRate));
        return valid.get(rand.nextInt(valid.size()));
    }

    public List<Proxy> getProxies(ProxyQuery query) {
        boolean wait = Optional.ofNullable(query.getWait()).orElse(false);
        if (!wait)
            return executeProxyQuery(query);

        Long maxWait =
            Optional
                .ofNullable(query.getMaxWait().getSeconds())
                .orElse(1l);
        List<Proxy> found;
        LocalDateTime start = LocalDateTime.now();
        while ((found = executeProxyQuery(query)).isEmpty()
            && start.plusSeconds(maxWait).isAfter(LocalDateTime.now()))
            ThreadUtils.sleep(50);
        return found;
    }

    public void addProxy(Proxy proxy, boolean overwrite) {
        if (this.proxies.containsKey(proxy.getUrl()) && !overwrite)
            return;
        this.proxies.put(proxy.getUrl(), proxy);
    }

    private List<Proxy> executeProxyQuery(ProxyQuery query) {
        return this.proxies
            .values()
            .stream()
            .filter(
                p -> p.getSuccessRate() >= Optional
                    .ofNullable(query.getMinSuccessRate())
                    .orElse(0f))
            .limit(
                Optional
                    .ofNullable(query.getCount())
                    .orElse(Integer.MAX_VALUE))
            .collect(Collectors.toList());
    }

}
