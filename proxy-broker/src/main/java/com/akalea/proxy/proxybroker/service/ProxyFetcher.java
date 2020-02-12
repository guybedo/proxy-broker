package com.akalea.proxy.proxybroker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyProvider;
import com.akalea.proxy.proxybroker.domain.ProxyQuery;
import com.akalea.proxy.proxybroker.domain.ProxyStatus;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties;
import com.akalea.proxy.proxybroker.repository.ProxyProviderRepository;
import com.akalea.proxy.proxybroker.utils.ThreadUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ProxyFetcher {

    private final static Logger logger = LoggerFactory.getLogger(ProxyFetcher.class);

    private ProxyProperties properties;

    private ExecutorService fetcherExecutor;
    private LocalDateTime   lastFetch;

    private ProxyProviderRepository providerRepository;

    private ProxyBroker  broker;
    private ProxyChecker checker;

    private boolean             started;
    private List<ProxyProvider> providers;

    public ProxyFetcher(
        ProxyBroker broker,
        ProxyChecker checker) {
        this(
            ProxyConfiguration.proxyProperties(),
            broker,
            checker,
            new ProxyProviderRepository());
    }

    public ProxyFetcher(
        ProxyProperties properties,
        ProxyBroker broker,
        ProxyChecker checker,
        ProxyProviderRepository providerRepository) {
        this.properties = properties;
        this.providerRepository = providerRepository;
        this.broker = broker;
        this.checker = checker;
    }

    public void start() {
        this.started = true;
        logger.info("Starting proxy fetcher");
        loadProviders();
        startFetcherThread();
    }

    public void loadProviders() {
        loadProviders(true, null);
    }

    public void loadProviders(
        boolean loadDefaultProviders,
        List<ProxyProvider> additionalProviders) {
        try {
            if (loadDefaultProviders) {
                logger.debug("Loading default proxy providers");
                this.providers =
                    properties
                        .getProxy()
                        .getProviders()
                        .getProviders()
                        .stream()
                        .map(
                            p -> new ProxyProvider()
                                .setUrl(p.getUrl())
                                .setPageUrl(p.getPageUrl())
                                .setFormat(p.getFormat()))
                        .collect(Collectors.toList());
            }
            this.providers.addAll(
                Optional
                    .ofNullable(additionalProviders)
                    .orElse(Lists.newArrayList()));
            logger.info(String.format("Loaded %d providers", this.providers.size()));
        } catch (Exception e) {
            logger.error("Error loading default proxy providers", e);
        }
    }

    public void startFetcherThread() {
        Optional
            .ofNullable(this.fetcherExecutor)
            .ifPresent(e -> {
                try {
                    e.shutdownNow();
                    ThreadUtils.waitUntil(() -> e.isShutdown(), 5, 100);
                } catch (Exception e1) {
                    logger.error("Error while trying to stop executor", e);
                }
            });
        this.fetcherExecutor =
            Executors.newFixedThreadPool(
                1,
                new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("ProxyFetcher");
                        return thread;
                    }
                });
        fetcherExecutor.execute(fetchThread());
    }

    private Runnable fetchThread() {
        return () -> {
            if (!isProxyProviderAutoRefresh()) {
                fetchNewProxies();
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
                        .ofNullable(lastFetch)
                        .map(d -> d.plusSeconds(delaySeconds))
                        .orElse(LocalDateTime.now().minusMinutes(1));
                if (next.isBefore(LocalDateTime.now())) {
                    logger.info(
                        String.format(
                            "%d valid proxies in DB so far",
                            broker
                                .findProxies(
                                    new ProxyQuery()
                                        .setStatus(ProxyStatus.ok)
                                        .setWait(false))
                                .size()));
                    fetchNewProxies();
                    lastFetch = LocalDateTime.now();
                } else
                    ThreadUtils.sleep(1000);
            }
        };
    }

    private void fetchNewProxies() {
        try {
            logger.debug("Fetching new proxies");
            Set<String> currentProxies =
                this.broker
                    .findProxies(
                        new ProxyQuery()
                            .setWait(false)
                            .setStatus(null))
                    .stream()
                    .map(p -> p.getUrl())
                    .collect(Collectors.toSet());
            Map<String, Proxy> fetchedProxies =
                providerRepository.fetchProvidersProxies(this.providers);
            Map<String, Proxy> newProxies =
                Sets
                    .difference(fetchedProxies.keySet(), currentProxies)
                    .stream()
                    .map(k -> fetchedProxies.get(k))
                    .collect(Collectors.toMap(p -> p.getUrl(), p -> p));
            logger.info(String.format("Fetched %d new proxies from providers", newProxies.size()));
            checker.check(
                Lists.newArrayList(newProxies.values()),
                (p) -> this.broker.addProxy(p, false));
        } catch (Exception e) {
            logger.error("Error parsing proxy providers", e);
        }
    }

    public boolean isProxyProviderAutoRefresh() {
        return this.properties
            .getProxy()
            .getProviders()
            .getRefresh()
            .isAutorefresh();

    }

    public boolean isStarted() {
        return started;
    }

}
