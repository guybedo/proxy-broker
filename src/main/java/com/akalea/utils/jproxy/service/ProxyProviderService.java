package com.akalea.utils.jproxy.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akalea.utils.jproxy.app.configuration.ProxyProperties;
import com.akalea.utils.jproxy.domain.Proxy;
import com.akalea.utils.jproxy.domain.ProxyProvider;
import com.akalea.utils.jproxy.domain.ProxyStatus;
import com.google.common.collect.Lists;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

@Service
public class ProxyProviderService {

    @Autowired
    private ProxyProperties properties;

    private List<ProxyProvider> providers = Lists.newArrayList();
    private List<Proxy>         proxies   = Lists.newArrayList();

    private Executor proxyUpdater;

    @PostConstruct
    public void initProxies() {
        this.providers =
            properties
                .getProxy()
                .getProviders()
                .stream()
                .map(p -> new ProxyProvider().setUrl(p.getUrl()))
                .collect(Collectors.toList());

        proxyUpdater = Executors.newFixedThreadPool(1);
        proxyUpdater.execute(proxyUpdateTask());
    }

    private Runnable proxyUpdateTask() {
        return () -> {

        };
    }

    private List<Proxy> fetchProviderProxies(ProxyProvider provider) {
        HttpResponse<String> response =
            Unirest
                .get(provider.getUrl())
                .asString();
        provider.setLastStatusUpdate(LocalDate.now());
        if (!response.isSuccess())
            provider.setStatus(ProxyStatus.invalid);
        return Lists.newArrayList();
    }
}
