package com.akalea.proxy.proxybroker.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

public class Proxy {

    private String url;

    private List<String> protocols = Lists.newArrayList();

    private ProxyStatus   lastCheck;
    private LocalDateTime lastCheckDate;

    private Stats stats = new Stats();

    public void incOkCount() {
        this.stats.ok.incrementAndGet();
    }

    public void incKoCount() {
        this.stats.ko.incrementAndGet();
    }

    public float getSuccessRate() {
        return this.stats.getSuccessRate();
    }

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
        Proxy other = (Proxy) obj;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

    public static class Stats {
        private AtomicInteger ok = new AtomicInteger(0);
        private AtomicInteger ko = new AtomicInteger(0);

        public float getSuccessRate() {
            int total = ok.get() + ko.get();
            if (total == 0)
                return 1;
            return ok.get() / total;
        }

        public int getOk() {
            return ok.get();
        }

        public Stats setOk(int ok) {
            this.ok.set(ok);
            return this;
        }

        public int getKo() {
            return ko.get();
        }

        public Stats setKo(int ko) {
            this.ko.set(ko);
            return this;
        }

    }

    public Proxy setStats(Stats stats) {
        this.stats = stats;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Proxy setUrl(String url) {
        this.url = url;
        return this;
    }

    public List<String> getProtocols() {
        return protocols;
    }

    public Proxy setProtocols(List<String> protocols) {
        this.protocols = protocols;
        return this;
    }

    public ProxyStatus getLastCheck() {
        return lastCheck;
    }

    public Proxy setLastCheck(ProxyStatus status) {
        this.lastCheck = status;
        return this;
    }

    public LocalDateTime getLastCheckDate() {
        return lastCheckDate;
    }

    public Proxy setLastCheckDate(LocalDateTime lastStatusUpdate) {
        this.lastCheckDate = lastStatusUpdate;
        return this;
    }

}
