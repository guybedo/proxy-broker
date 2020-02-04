package com.akalea.utils.jproxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan({
    "com.akalea.utils.jproxy.test",
    "com.akalea.utils.jproxy.repository",
    "com.akalea.utils.jproxy.process",
    "com.akalea.utils.jproxy.service" })
public class TestConfiguration {

    @Autowired
    private Environment env;

}
