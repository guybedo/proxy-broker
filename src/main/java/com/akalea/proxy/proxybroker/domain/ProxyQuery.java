package com.akalea.proxy.proxybroker.domain;

import java.time.Duration;

public class ProxyQuery {

    private Integer     count;
    private ProxyStatus status = ProxyStatus.ok;
    private Float       minSuccessRate;

    private Boolean  wait    = true;
    private Duration maxWait = Duration.ofHours(1);

    public ProxyStatus getStatus() {
        return status;
    }

    public ProxyQuery setStatus(ProxyStatus status) {
        this.status = status;
        return this;
    }

    public void wait(Duration maxWait) {
        this.wait = true;
        this.maxWait = maxWait;
    }

    public ProxyQuery noWait() {
        return setWait(false).setMaxWait(null);
    }

    public Integer getCount() {
        return count;
    }

    public ProxyQuery setCount(Integer count) {
        this.count = count;
        return this;
    }

    public Float getMinSuccessRate() {
        return minSuccessRate;
    }

    public ProxyQuery setMinSuccessRate(Float minSuccessRate) {
        this.minSuccessRate = minSuccessRate;
        return this;
    }

    public Boolean getWait() {
        return wait;
    }

    public ProxyQuery setWait(Boolean wait) {
        this.wait = wait;
        return this;
    }

    public Duration getMaxWait() {
        return maxWait;
    }

    public ProxyQuery setMaxWait(Duration maxWait) {
        this.maxWait = maxWait;
        return this;
    }

}
