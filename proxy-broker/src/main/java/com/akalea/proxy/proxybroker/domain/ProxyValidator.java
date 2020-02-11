package com.akalea.proxy.proxybroker.domain;

import java.net.URI;

public class ProxyValidator {

    private String url;

    public String getUrl() {
        return url;
    }

    public ProxyValidator setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getScheme() {
        try {
            return new URI(url).getScheme();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not guess scheme from url %s", url));
        }
    }

}
