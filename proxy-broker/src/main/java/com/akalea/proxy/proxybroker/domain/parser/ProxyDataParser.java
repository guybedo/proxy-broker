package com.akalea.proxy.proxybroker.domain.parser;

import java.util.List;

import com.akalea.proxy.proxybroker.domain.Proxy;

public interface ProxyDataParser {

    List<Proxy> parse(String content);

    String getParserId();
}
