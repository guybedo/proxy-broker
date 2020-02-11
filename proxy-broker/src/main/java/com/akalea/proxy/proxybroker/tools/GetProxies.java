package com.akalea.proxy.proxybroker.tools;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyQuery;
import com.akalea.proxy.proxybroker.service.ProxyBroker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;

public class GetProxies {

    public void run() {
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(logCtx);
        logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
        logEncoder.start();

        ConsoleAppender logConsoleAppender = new ConsoleAppender();
        logConsoleAppender.setContext(logCtx);
        logConsoleAppender.setName("console");
        logConsoleAppender.setEncoder(logEncoder);
        logConsoleAppender.start();

        ch.qos.logback.classic.Logger log =
            (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        log.setLevel(Level.DEBUG);
        log.addAppender(logConsoleAppender);

        ProxyBroker proxyBroker = new ProxyBroker();
        List<Proxy> proxies = proxyBroker.getProxies(new ProxyQuery().setCount(10).setWait(true));
        System.out.println(String.format("Found %d proxies", proxies.size()));
    }

    public static void main(String[] args) {
        new GetProxies().run();
    }

}
