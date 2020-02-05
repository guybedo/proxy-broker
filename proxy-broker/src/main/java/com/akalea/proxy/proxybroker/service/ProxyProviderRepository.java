package com.akalea.proxy.proxybroker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyProvider;
import com.akalea.proxy.proxybroker.domain.ProxyQuery;
import com.akalea.proxy.proxybroker.domain.ProxyStatus;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties;
import com.akalea.proxy.proxybroker.utils.ThreadUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class ProxyProviderRepository {

    private final static Logger logger = LoggerFactory.getLogger(ProxyProviderRepository.class);

    private ProxyProperties properties;

    private List<ProxyProvider> providers;
    private Executor            proxyProviderParseExecutor;
    private LocalDateTime       lastProxyProviderParsing;

    private ProxyBroker  broker;
    private ProxyChecker checker;

    public ProxyProviderRepository(ProxyBroker broker, ProxyChecker checker) {
        this.properties = ProxyConfiguration.proxyProperties();
        this.broker = broker;
        this.checker = checker;
        this.proxyProviderParseExecutor = Executors.newFixedThreadPool(1);
    }

    public void init() {
        loadProviders();
        startProxyProviderParsing();
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
            Set<String> currentProxies =
                this.broker
                    .getProxies(new ProxyQuery())
                    .stream()
                    .map(p -> p.getUrl())
                    .collect(Collectors.toSet());
            Map<String, Proxy> fetchedProxies = parse();
            Map<String, Proxy> newProxies =
                Sets
                    .difference(fetchedProxies.keySet(), currentProxies)
                    .stream()
                    .map(k -> fetchedProxies.get(k))
                    .collect(Collectors.toMap(p -> p.getUrl(), p -> p));
            logger.debug(String.format("Found %d new proxies", newProxies.size()));
            checker.check(
                Lists.newArrayList(newProxies.values()),
                (p) -> this.broker.addProxy(p, false));
        } catch (Exception e) {
            logger.error("Error parsing proxy providers", e);
        }
    }

    public Map<String, Proxy> parse() {
        return this.providers
            .stream()
            .map(pp -> fetchProviderProxies(pp))
            .flatMap(List::stream)
            .collect(Collectors.toMap(p -> p.getUrl(), p -> p));
    }

    private List<Proxy> fetchProviderProxies(ProxyProvider provider) {
        HttpResponse<String> response =
            Unirest
                .get(provider.getUrl())
                .asString();
        provider.setLastStatusUpdate(LocalDate.now());
        if (!response.isSuccess())
            provider.setStatus(ProxyStatus.ko);
        return Lists.newArrayList();
    }

}
