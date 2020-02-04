package com.akalea.utils.jproxy.app.configuration;

import java.util.List;

import com.google.common.collect.Lists;

public class ProxyProperties {

    public Proxy proxy = new Proxy();

    public static class Proxy {
        private List<ProxyProviderConfiguration> providers = Lists.newArrayList();

        public List<ProxyProviderConfiguration> getProviders() {
            return providers;
        }

        public Proxy setProviders(List<ProxyProviderConfiguration> providers) {
            this.providers = providers;
            return this;
        }
    }

    public static class ProxyProviderConfiguration {
        String url;
        String protocols;

        public String getUrl() {
            return url;
        }

        public ProxyProviderConfiguration setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getProtocols() {
            return protocols;
        }

        public ProxyProviderConfiguration setProtocols(String protocols) {
            this.protocols = protocols;
            return this;
        }

    }

    public Proxy getProxy() {
        return proxy;
    }

    public ProxyProperties setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

}
