package com.akalea.utils.jproxy.domain;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;

public class ProxyProvider {

    private String url;

    private List<String> protocols = Lists.newArrayList();

    private ProxyStatus status;
    private LocalDate   lastStatusUpdate;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyProvider other = (ProxyProvider) obj;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

    public String getUrl() {
        return url;
    }

    public ProxyProvider setUrl(String url) {
        this.url = url;
        return this;
    }

    public List<String> getProtocols() {
        return protocols;
    }

    public ProxyProvider setProtocols(List<String> protocols) {
        this.protocols = protocols;
        return this;
    }

    public ProxyStatus getStatus() {
        return status;
    }

    public ProxyProvider setStatus(ProxyStatus status) {
        this.status = status;
        return this;
    }

    public LocalDate getLastStatusUpdate() {
        return lastStatusUpdate;
    }

    public ProxyProvider setLastStatusUpdate(LocalDate lastStatusUpdate) {
        this.lastStatusUpdate = lastStatusUpdate;
        return this;
    }

}
