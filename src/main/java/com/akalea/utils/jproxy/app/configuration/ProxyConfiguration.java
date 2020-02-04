package com.akalea.utils.jproxy.app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@Configuration
public class ProxyConfiguration {

    @Bean
    public ProxyProperties proxyProperties() {
        try {
            return new ObjectMapper(new YAMLFactory())
                .readValue(
                    new ClassPathResource("proxy.yml").getInputStream(),
                    ProxyProperties.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
