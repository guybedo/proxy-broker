package com.akalea.proxy.proxybroker.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyProvider;
import com.akalea.proxy.proxybroker.domain.ProxyStatus;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties;
import com.akalea.proxy.proxybroker.domain.parser.ProxyDataParser;
import com.akalea.proxy.proxybroker.utils.HttpUtils;
import com.akalea.proxy.proxybroker.utils.ThreadUtils;
import com.google.common.collect.Lists;

public class ProxyProviderRepository {

    private ProxyProperties properties;

    public ProxyProviderRepository() {
        this(ProxyConfiguration.proxyProperties());
    }

    public ProxyProviderRepository(ProxyProperties properties) {
        super();
        this.properties = properties;
    }

    private final Logger logger = LoggerFactory.getLogger(ProxyProviderRepository.class);

    public Map<String, Proxy> fetchProvidersProxies(List<ProxyProvider> providers) {
        try {
            ExecutorService executor =
                Executors.newFixedThreadPool(
                    getThreadCount(),
                    new ThreadFactory() {
                        public Thread newThread(Runnable r) {
                            Thread thread = new Thread(r);
                            thread.setName("ProxyProviderFetcher");
                            return thread;
                        }
                    });

            List<Future<List<Proxy>>> futures =
                providers
                    .stream()
                    .map(pp -> executor.submit(() -> fetchProviderProxies(pp)))
                    .collect(Collectors.toList());
            Map<String, Proxy> proxies =
                futures
                    .stream()
                    .map(f -> {
                        try {
                            return f.get();
                        } catch (Exception e) {
                            logger.error("Error executing provider refresh task", e);
                            return (List<Proxy>) (List) Lists.newArrayList();
                        }
                    })
                    .flatMap(List::stream)
                    .collect(
                        Collectors.toMap(
                            p -> p.getUrl(),
                            p -> p,
                            (p1, p2) -> p1));
            ThreadUtils.shutdown(executor, 1, TimeUnit.MINUTES);
            logger.debug(String.format("Found %d unique proxies", proxies.size()));
            return proxies;
        } catch (Exception e) {
            logger.error("Error fetching providers proxies", e);
            return new HashMap<>();
        }
    }

    public List<Proxy> fetchProviderProxies(ProxyProvider provider) {
        logger.debug(String.format("Fetching proxies from provider %s", provider.getUrl()));
        provider.setLastStatusUpdate(LocalDate.now());
        try {
            List<Proxy> proxies = Lists.newArrayList();
            if (StringUtils.isEmpty(provider.getPageUrl()))
                proxies =
                    extractProxiesFromPageData(
                        provider,
                        HttpUtils.fetchData(provider.getPageUrl()));
            else {
                int page = 0;
                List<Proxy> pageProxies;
                while (!(pageProxies = extractProxiesFromPage(provider, page++)).isEmpty()) {
                    proxies.addAll(pageProxies);
                    ThreadUtils.sleep(getDelayBetweenPageFetches());
                }
            }
            provider.setStatus(proxies.isEmpty() ? ProxyStatus.ko : ProxyStatus.ok);
            logger.debug(
                String.format(
                    "Found %d proxies from provider %s",
                    proxies.size(),
                    provider.getUrl()));
            return proxies;
        } catch (Exception e) {
            logger.error(
                String.format("Error fetching proxies from provider %s", provider.getUrl()));
            return Lists.newArrayList();
        }
    }

    private List<Proxy> extractProxiesFromPage(ProxyProvider provider, int page) {
        try {
            String pageData = fetchPage(page, provider.getPageUrl());
            return extractProxiesFromPageData(provider, pageData);
        } catch (Exception e) {
            logger.warn(
                String.format(
                    "Error extracting proxies from provider %s page %d",
                    provider.getUrl(),
                    page));
            return Lists.newArrayList();
        }
    }

    private List<Proxy> extractProxiesFromPageData(ProxyProvider provider, String pageData) {
        if (StringUtils.isEmpty(pageData))
            return Lists.newArrayList();
        ProxyDataParser parser =
            Optional
                .ofNullable(ProxyDataParser.getParser(provider.getFormat()))
                .orElseThrow(
                    () -> new RuntimeException(
                        String.format("Missing parser for format %s", provider.getFormat())));
        List<Proxy> proxies = parser.parse(pageData);
        return proxies;
    }

    public String fetchPage(int page, String urlPattern) {
        String url = urlPattern.replaceAll("\\{page\\}", String.valueOf(page));
        logger.debug(String.format("Fetching provider page data from %s", url));
        return HttpUtils.fetchData(url);

    }

    public int getDelayBetweenPageFetches() {
        return Optional
            .ofNullable(
                this.properties
                    .getProxy()
                    .getProviders()
                    .getRefresh()
                    .getDelayBetweenPageFetchesMsecs())
            .orElse(1000);
    }

    public int getThreadCount() {
        return Optional
            .ofNullable(
                this.properties
                    .getProxy()
                    .getProviders()
                    .getRefresh()
                    .getRefreshThreadCount())
            .orElse(1);
    }
}
