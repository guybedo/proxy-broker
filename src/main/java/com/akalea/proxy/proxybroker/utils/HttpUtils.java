package com.akalea.proxy.proxybroker.utils;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.google.common.collect.Lists;

import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class HttpUtils {

    public static final List<String> userAgent =
        Lists.newArrayList(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.99 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.99 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246",
            "Mozilla/5.0 (X11; CrOS x86_64 8172.45.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.64 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/601.3.9 (KHTML, like Gecko) Version/9.0.2 Safari/601.3.9",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1",
            "Mozilla/5.0 (X11; Linux i686; rv:30.0) Gecko/20100101 Firefox/30.0",
            "Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0",
            "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:58.0) Gecko/20100101 Firefox/58.0",
            "Mozilla/5.0 (X11; Linux i686; rv:64.0) Gecko/20100101 Firefox/64.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:64.0) Gecko/20100101 Firefox/64.0",
            "Mozilla/5.0 (X11; Linux i586; rv:63.0) Gecko/20100101 Firefox/63.0",
            "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:63.0) Gecko/20100101 Firefox/63.0");

    public static final List<Header> headers =
        Lists.newArrayList(
            new BasicHeader(
                "accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"),
            new BasicHeader("accept-encoding", "gzip, deflate"),
            new BasicHeader("accept-language", "en,fr;q=0.9,es;q=0.8"),
            new BasicHeader("cache-control", "no-cache"),
            new BasicHeader("pragma", "no-cache"),
            new BasicHeader("referer", "https://www.google.com/"),
            new BasicHeader("upgrade-insecure-requests", "1"),
            new BasicHeader("sec-fetch-mode", "navigate"),
            new BasicHeader("sec-fetch-site", "same-origin"),
            new BasicHeader("sec-fetch-user", "?1"));

    private static Random random = new Random();

    public static String fetchData(String url) {
        return fetchData(url, null);
    }

    public static String fetchData(String url, Proxy proxy) {
        HttpResponse<String> resp = getRequest(url, proxy);
        if (resp.isSuccess()) {
            return resp.getBody();
        } else
            return null;
    }

    public static HttpResponse<String> getRequest(String url, Proxy proxy) {
        GetRequest getRequest = Unirest.get(url);
        Optional
            .ofNullable(proxy)
            .ifPresent(p -> getRequest.proxy(p.getHost(), p.getPort()));
        HttpUtils.headers
            .stream()
            .forEach(h -> getRequest.header(h.getName(), h.getValue()));
        getRequest.header(
            "User-Agent",
            HttpUtils.userAgent.get(random.nextInt(HttpUtils.userAgent.size())));
        return getRequest.asString();
    }
}
