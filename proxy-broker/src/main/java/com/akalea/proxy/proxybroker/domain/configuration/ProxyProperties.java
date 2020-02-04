package com.akalea.proxy.proxybroker.domain.configuration;

import java.util.List;

import com.google.common.collect.Lists;

public class ProxyProperties {

    public Proxy proxy = new Proxy();

    public static class Proxy {
        private ProxyProviders providers = new ProxyProviders();

        public ProxyProviders getProviders() {
            return providers;
        }

        public Proxy setProviders(ProxyProviders providers) {
            this.providers = providers;
            return this;
        }

    }

    public static class ProxyProviders {
        private ProxyProviderRefreshPolicy       refresh   = new ProxyProviderRefreshPolicy();
        private List<ProxyProviderConfiguration> providers = Lists.newArrayList();

        public ProxyProviderRefreshPolicy getRefresh() {
            return refresh;
        }

        public ProxyProviders setRefresh(ProxyProviderRefreshPolicy refresh) {
            this.refresh = refresh;
            return this;
        }

        public List<ProxyProviderConfiguration> getProviders() {
            return providers;
        }

        public ProxyProviders setProviders(List<ProxyProviderConfiguration> providers) {
            this.providers = providers;
            return this;
        }
    }

    public static class ProxyProviderRefreshPolicy {
        private boolean autorefresh;
        private int     refreshDelaySeconds;

        public boolean isAutorefresh() {
            return autorefresh;
        }

        public ProxyProviderRefreshPolicy setAutorefresh(boolean autorefresh) {
            this.autorefresh = autorefresh;
            return this;
        }

        public int getRefreshDelaySeconds() {
            return refreshDelaySeconds;
        }

        public ProxyProviderRefreshPolicy setRefreshDelaySeconds(int refreshDelaySeconds) {
            this.refreshDelaySeconds = refreshDelaySeconds;
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
