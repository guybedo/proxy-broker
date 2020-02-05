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
import com.akalea.proxy.proxybroker.domain.parser.ProxyDataParser;
import com.google.common.collect.Lists;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class ProxyProviderRepository {

    private final Logger logger = LoggerFactory.getLogger(ProxyProviderRepository.class);

    public Map<String, Proxy> fetchProvidersProxies(List<ProxyProvider> providers) {
        return providers
            .stream()
            .map(pp -> fetchProviderProxies(pp))
            .flatMap(List::stream)
            .collect(Collectors.toMap(p -> p.getUrl(), p -> p));
    }

    public List<Proxy> fetchProviderProxies(ProxyProvider provider) {
        provider.setLastStatusUpdate(LocalDate.now());
        try {
            ProxyDataParser parser =
                Optional
                    .ofNullable(ProxyDataParser.getParser(provider.getFormat()))
                    .orElseThrow(
                        () -> new RuntimeException(
                            String.format("Missing parser for format %s", provider.getFormat())));
            List<String> pages = fetchProxyProviderData(provider);
            List<Proxy> proxies =
                pages
                    .stream()
                    .map(p -> parser.parse(p))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            provider.setStatus(proxies.isEmpty() ? ProxyStatus.ko : ProxyStatus.ok);
            return proxies;
        } catch (Exception e) {
            logger.error(
                String.format("Error fetching proxies from provider %s", provider.getUrl()));
            return Lists.newArrayList();
        }
    }

    private List<String> fetchProxyProviderData(ProxyProvider provider) {
        if (StringUtils.isEmpty(provider.getPageUrl()))
            return Lists.newArrayList(
                Unirest
                    .get(provider.getUrl())
                    .asString()
                    .getBody());
        else
            return fetchPages(provider.getPageUrl());
    }

    public List<String> fetchPages(String urlPattern) {
        List<String> pages = Lists.newArrayList();
        try {
            int page = 0;
            while (true) {
                String url = urlPattern.replaceAll("\\{page\\}", String.valueOf(page));
                logger.debug(String.format("Fetching provider page data from %s", url));
                HttpResponse<String> resp =
                    Unirest
                        .get(url)
                        .asString();
                if (resp.isSuccess()) {
                    pages.add(resp.getBody());
                    page++;
                } else
                    return pages;
            }
        } catch (Exception e) {
            logger.error("Error fetching pages", e);
            return pages;
        }

    }
}
