package com.akalea.proxy.proxybroker.domain.configuration;

import java.util.List;

import com.akalea.proxy.proxybroker.domain.ProxyValidator;
import com.google.common.collect.Lists;

public class ProxyProperties {

    public Proxy proxy = new Proxy();

    public static class Proxy {
        private ProxyProviders providers = new ProxyProviders();
        private Proxies        proxies   = new Proxies();

        public Proxies getProxies() {
            return proxies;
        }

        public Proxy setProxies(Proxies proxies) {
            this.proxies = proxies;
            return this;
        }

        public ProxyProviders getProviders() {
            return providers;
        }

        public Proxy setProviders(ProxyProviders providers) {
            this.providers = providers;
            return this;
        }

    }

    public static class Proxies {
        private ProxyCheckPolicy check = new ProxyCheckPolicy();

        public ProxyCheckPolicy getCheck() {
            return check;
        }

        public Proxies setCheck(ProxyCheckPolicy check) {
            this.check = check;
            return this;
        }

    }

    public static class ProxyCheckPolicy {

        private Boolean testOnBorrow;
        private Boolean testOnCreate;
        private Integer minValidationIntervalSeconds;
        private Integer timeBetweenValidationRunsSeconds;
        private Integer validationMaxConnectionsCount;
        private Integer validationIntervalSeconds;
        private Integer validationMinValidatorsCount;

        private List<ProxyValidator> validators = Lists.newArrayList();

        public Integer getValidationMinValidatorsCount() {
            return validationMinValidatorsCount;
        }

        public ProxyCheckPolicy setValidationMinValidatorsCount(Integer validationMinValidatorsCount) {
            this.validationMinValidatorsCount = validationMinValidatorsCount;
            return this;
        }

        public Boolean getTestOnBorrow() {
            return testOnBorrow;
        }

        public ProxyCheckPolicy setTestOnBorrow(Boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
            return this;
        }

        public Boolean getTestOnCreate() {
            return testOnCreate;
        }

        public ProxyCheckPolicy setTestOnCreate(Boolean testOnCreate) {
            this.testOnCreate = testOnCreate;
            return this;
        }

        public Integer getMinValidationIntervalSeconds() {
            return minValidationIntervalSeconds;
        }

        public ProxyCheckPolicy setMinValidationIntervalSeconds(
            Integer minValidationIntervalSeconds) {
            this.minValidationIntervalSeconds = minValidationIntervalSeconds;
            return this;
        }

        public Integer getTimeBetweenValidationRunsSeconds() {
            return timeBetweenValidationRunsSeconds;
        }

        public ProxyCheckPolicy setTimeBetweenValidationRunsSeconds(
            Integer timeBetweenValidationRunsSeconds) {
            this.timeBetweenValidationRunsSeconds = timeBetweenValidationRunsSeconds;
            return this;
        }

        public Integer getValidationMaxConnectionsCount() {
            return validationMaxConnectionsCount;
        }

        public ProxyCheckPolicy setValidationMaxConnectionsCount(
            Integer validationMaxConnectionsCount) {
            this.validationMaxConnectionsCount = validationMaxConnectionsCount;
            return this;
        }

        public Integer getValidationIntervalSeconds() {
            return validationIntervalSeconds;
        }

        public ProxyCheckPolicy setValidationIntervalSeconds(Integer validationIntervalSeconds) {
            this.validationIntervalSeconds = validationIntervalSeconds;
            return this;
        }

        public List<ProxyValidator> getValidators() {
            return validators;
        }

        public ProxyCheckPolicy setValidators(List<ProxyValidator> validators) {
            this.validators = validators;
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
