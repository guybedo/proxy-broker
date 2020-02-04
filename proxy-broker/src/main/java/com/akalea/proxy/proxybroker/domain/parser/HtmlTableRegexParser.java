package com.akalea.proxy.proxybroker.domain.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.google.common.collect.Lists;

public class HtmlTableRegexParser implements ProxyDataParser {

    private Pattern pattern =
        Pattern.compile(
            "<td[^<>]*>([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3})</td>"
                + "<td[^<>]*>([1-9][0-9]{1,4})</td>");

    private String parserId = "html";

    @Override
    public List<Proxy> parse(String content) {
        ArrayList<Proxy> proxies = Lists.newArrayList();
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            proxies.add(
                new Proxy()
                    .setUrl(String.format("%s:%s", matcher.group(1), matcher.group(2))));
        }
        return proxies;

    }

    public String getParserId() {
        return parserId;
    }

}