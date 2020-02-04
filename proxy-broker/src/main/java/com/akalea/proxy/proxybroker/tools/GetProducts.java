package com.akalea.proxy.proxybroker.tools;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.google.common.collect.Lists;

@Import(ProxyConfiguration.class)
@EnableAutoConfiguration
public class GetProducts {

    private final static String url =
        "http://app03.kizago.com:8081/amazon-proxy-service/products/amazon";

    public void run() {
        List<String> uuids =
            Lists.newArrayList("B000I0FB1U", "B0035A53I6", "B00005JNBQ");

        RestTemplate rest = new RestTemplate();
        List products =
            (List) rest.postForEntity(
                url,
                uuids,
                List.class)
                .getBody()
                .stream()
                .filter(p -> p != null)
                .collect(Collectors.toList());
        System.out.println(String.format("Created %d products", products.size()));
    }

    public static void main(String[] args) {
        System.setProperty("spring.main.web-application-type", "NONE");
        System.setProperty(
            "spring.profiles.active",
            Optional
                .ofNullable(System.getProperty("spring.profiles.active"))
                .orElse("preprod"));
        System.out.println(
            String.format("Profile %s", System.getProperty("spring.profiles.active")));
        ConfigurableApplicationContext context =
            SpringApplication.run(GetProducts.class, args);
        context
            .getBean(GetProducts.class)
            .run();

    }

}
