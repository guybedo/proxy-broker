package com.akalea.proxy.proxybroker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyProvider;
import com.akalea.proxy.proxybroker.domain.ProxyStatus;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties;
import com.akalea.proxy.proxybroker.domain.parser.ProxyDataParser;
import com.akalea.proxy.proxybroker.utils.ThreadUtils;
import com.google.common.collect.Lists;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class ProxyProviderRepository {

    private ProxyProperties properties;

    public ProxyProviderRepository() {

    }

    public ProxyProviderRepository(ProxyProperties properties) {
        super();
        this.properties = properties;
    }

    private final Logger logger = LoggerFactory.getLogger(ProxyProviderRepository.class);

    public Map<String, Proxy> fetchProvidersProxies(List<ProxyProvider> providers) {
        Map<String, Proxy> proxies =
            providers
                .stream()
                .map(pp -> fetchProviderProxies(pp))
                .flatMap(List::stream)
                .collect(
                    Collectors.toMap(
                        p -> p.getUrl(),
                        p -> p,
                        (p1, p2) -> p1));
        logger.info(String.format("Found %d unique proxies", proxies.size()));
        return proxies;
    }

    public List<Proxy> fetchProviderProxies(ProxyProvider provider) {
        logger.info(String.format("Fetching proxies from provider %s", provider.getUrl()));
        provider.setLastStatusUpdate(LocalDate.now());
        try {
            List<Proxy> proxies = Lists.newArrayList();
            if (StringUtils.isEmpty(provider.getPageUrl()))
                proxies = extractProxiesFromPageData(provider, fetchData(provider.getPageUrl()));
            else {
                int page = 0;
                List<Proxy> pageProxies;
                while (!(pageProxies = extractProxiesFromPage(provider, page++)).isEmpty()) {
                    proxies.addAll(pageProxies);
                    ThreadUtils.sleep(getDelayBetweenPageFetches());
                }
            }
            provider.setStatus(proxies.isEmpty() ? ProxyStatus.ko : ProxyStatus.ok);
            logger.info(
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
        return fetchData(url);

    }

    private String fetchData(String url) {
        HttpResponse<String> resp =
            Unirest
                .get(url)
                .asString();
        if (resp.isSuccess()) {
            return resp.getBody();
        } else
            return null;
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
}
