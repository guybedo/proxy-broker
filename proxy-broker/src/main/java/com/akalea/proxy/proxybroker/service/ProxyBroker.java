package com.akalea.proxy.proxybroker.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyProvider;
import com.akalea.proxy.proxybroker.domain.ProxyQuery;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties;
import com.akalea.proxy.proxybroker.utils.ThreadUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ProxyBroker {

    private final static Logger logger = LoggerFactory.getLogger(ProxyBroker.class);

    private ProxyProperties properties;

    private List<ProxyProvider> providers = Collections.synchronizedList(Lists.newArrayList());
    private Map<String, Proxy>  proxies   =
        Collections.synchronizedMap(new HashMap<String, Proxy>());

    private Executor      proxyProviderParseExecutor;
    private LocalDateTime lastProxyProviderParsing;

    private Random rand = new Random();

    public ProxyBroker() {
        this.properties = ProxyConfiguration.proxyProperties();
    }

    public void init() {
        proxyProviderParseExecutor = Executors.newFixedThreadPool(1);
        loadProviders();
        startProxyProviderParsing();
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

    public void loadProviders() {
        loadProviders(true, null);
    }

    public void loadProviders(
        boolean loadDefaultProviders,
        List<ProxyProvider> additionalProviders) {
        if (loadDefaultProviders)
            this.providers =
                properties
                    .getProxy()
                    .getProviders()
                    .getProviders()
                    .stream()
                    .map(p -> new ProxyProvider().setUrl(p.getUrl()))
                    .collect(Collectors.toList());
        this.providers.addAll(
            Optional
                .ofNullable(additionalProviders)
                .orElse(Lists.newArrayList()));
    }

    public void startProxyProviderParsing() {
        proxyProviderParseExecutor.execute(proxyParseTask());
    }

    private Runnable proxyParseTask() {
        return () -> {
            if (!isProxyProviderAutoRefresh()) {
                parseProxyProviders();
                return;
            }

            while (isProxyProviderAutoRefresh()) {
                int delaySeconds =
                    Optional
                        .ofNullable(
                            this.properties
                                .getProxy()
                                .getProviders()
                                .getRefresh()
                                .getRefreshDelaySeconds())
                        .orElse(10 * 60);
                LocalDateTime next =
                    Optional
                        .ofNullable(lastProxyProviderParsing)
                        .map(d -> d.plusSeconds(delaySeconds))
                        .orElse(LocalDateTime.now());
                if (next.isBefore(LocalDateTime.now())) {
                    parseProxyProviders();
                    lastProxyProviderParsing = LocalDateTime.now();
                } else
                    ThreadUtils.sleep(1000);
            }
        };
    }

    public boolean isProxyProviderAutoRefresh() {
        return this.properties
            .getProxy()
            .getProviders()
            .getRefresh()
            .isAutorefresh();
    }

    private void parseProxyProviders() {
        try {
            logger.debug("Parsing proxy providers");
            Map<String, Proxy> fetchedProxies = new ProxyProviderParser(providers).parse();
            Map<String, Proxy> newProxies =
                Sets
                    .difference(fetchedProxies.keySet(), this.proxies.keySet())
                    .stream()
                    .map(k -> fetchedProxies.get(k))
                    .collect(Collectors.toMap(p -> p.getUrl(), p -> p));
            logger.debug(String.format("Found %d new proxies", newProxies.size()));
            new ProxyChecker().check(
                Lists.newArrayList(newProxies.values()),
                getCheckedProxyHandler());
        } catch (Exception e) {
            logger.error("Error parsing proxy providers", e);
        }
    }

    private Consumer<Proxy> getCheckedProxyHandler() {
        return (proxy) -> {
            if (!this.proxies.containsKey(proxy.getUrl()))
                this.proxies.put(proxy.getUrl(), proxy);
        };
    }

}
