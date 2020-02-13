package com.akalea.proxy.proxybroker.domain.parser;

import java.util.List;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.google.common.collect.Lists;

public interface ProxyDataParser {

    List<Proxy> parse(String content);

    public static ProxyDataParser getParser(String format) {
        try {
            List<Class<? extends ProxyDataParser>> parsers =
                Lists.newArrayList(
                    FreeProxyCzParser.class,
                    FreeProxyListsParser.class,
                    IpAddressParser.class,
                    ProxyListParser.class,
                    ProxyListsParser.class,
                    HtmlTableRegexParser.class);
            return parsers
                .stream()
                .filter(p -> {
                    try {
                        return format.equals(p.getField("format").get(null));
                    } catch (Exception e1) {
                        throw new RuntimeException(
                            String.format("Could not find a parser for format %s", format));
                    }
                })
                .findFirst()
                .map(p -> {
                    try {
                        return p.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(
                            String.format("Could not instantiate a parser for format %s", format));
                    }
                })
                .orElse(null);
        } catch (Exception e) {
            throw new RuntimeException(
                String.format("Could not find a parser for format %s", format));
        }
    }
}
