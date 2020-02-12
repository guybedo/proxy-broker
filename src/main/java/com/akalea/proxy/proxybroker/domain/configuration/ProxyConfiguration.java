package com.akalea.proxy.proxybroker.domain.configuration;

import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ProxyConfiguration {

    public static ProxyProperties proxyProperties() {
        try {
            InputStream yaml =
                ProxyConfiguration.class
                    .getClassLoader()
                    .getResourceAsStream("proxybroker.yml");
            return new ObjectMapper(new YAMLFactory()).readValue(yaml, ProxyProperties.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
