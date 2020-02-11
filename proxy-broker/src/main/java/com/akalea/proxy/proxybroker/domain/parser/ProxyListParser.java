package com.akalea.proxy.proxybroker.domain.parser;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class ProxyListParser implements ProxyDataParser {

    private final static Logger logger = LoggerFactory.getLogger(ProxyListParser.class);

    public static String format = "proxy-list";

    @Override
    public List<Proxy> parse(String content) {
        try {
            ObjectMapper mapper =
                new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<ProxyListResult> results =
                mapper.readValue(
                    content,
                    mapper
                        .getTypeFactory()
                        .constructCollectionType(List.class, ProxyListResult.class));
            return results
                .stream()
                .map(r -> r.getProxies())
                .flatMap(List::stream)
                .map(p -> new Proxy().setUrl(String.format("%s:%s", p.getIp(), p.getPort())))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error parsing ProxyList data", e);
            return Lists.newArrayList();
        }
    }

    public static class ProxyListResult {
        private List<ProxyListProxy> proxies = Lists.newArrayList();

        @JsonProperty("LISTA")
        public List<ProxyListProxy> getProxies() {
            return proxies;
        }

        public ProxyListResult setProxies(List<ProxyListProxy> proxies) {
            this.proxies = proxies;
            return this;
        }

    }

    public static class ProxyListProxy {
        private String ip;
        private String port;

        @JsonProperty("IP")
        public String getIp() {
            return ip;
        }

        public ProxyListProxy setIp(String ip) {
            this.ip = ip;
            return this;
        }

        @JsonProperty("PORT")
        public String getPort() {
            return port;
        }

        public ProxyListProxy setPort(String port) {
            this.port = port;
            return this;
        }

    }

}