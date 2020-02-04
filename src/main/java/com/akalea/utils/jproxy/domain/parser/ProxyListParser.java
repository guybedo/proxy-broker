package com.akalea.utils.jproxy.domain.parser;

import java.util.List;

import com.akalea.utils.jproxy.domain.Proxy;

public interface ProxyListParser {

    List<Proxy> parse(String content);

    String getParserId();
}
