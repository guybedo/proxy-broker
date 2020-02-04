package com.akalea.proxy.proxybroker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyProvider;
import com.akalea.proxy.proxybroker.domain.ProxyStatus;
import com.google.common.collect.Lists;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class ProxyProviderParser {

    private final static Logger logger = LoggerFactory.getLogger(ProxyProviderParser.class);

    private List<ProxyProvider> providers;

    public ProxyProviderParser(List<ProxyProvider> providers) {
        super();
        this.providers = providers;
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
            provider.setLastCheck(ProxyStatus.invalid);
        return Lists.newArrayList();
    }

}
