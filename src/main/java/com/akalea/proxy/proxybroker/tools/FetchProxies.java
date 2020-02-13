package com.akalea.proxy.proxybroker.tools;

import java.util.List;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.parser.FreeProxyCzParser;
import com.akalea.proxy.proxybroker.domain.parser.IpAddressParser;
import com.akalea.proxy.proxybroker.utils.HttpUtils;

public class FetchProxies {

    public static void main(String[] args) {
        List<Proxy> proxies =
            new IpAddressParser()
                .parse(HttpUtils.fetchData("https://www.ipaddress.com/proxy-list/"));
        System.out.println(
            String.format("Found %d proxies from www.ipaddress.com", proxies.size()));

        proxies =
            new FreeProxyCzParser()
                .parse(HttpUtils.fetchData("http://free-proxy.cz/en/"));
        System.out.println(
            String.format("Found %d proxies from free-proxy.cz", proxies.size()));

    }

}
