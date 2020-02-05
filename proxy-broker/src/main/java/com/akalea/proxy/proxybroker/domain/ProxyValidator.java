package com.akalea.proxy.proxybroker.domain;

public class ProxyValidator {

    private String url;
    private String scheme;

    public String getUrl() {
        return url;
    }

    public ProxyValidator setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public ProxyValidator setScheme(String format) {
        this.scheme = format;
        return this;
    }

}
