package com.akalea.proxy.proxybroker.test.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.parser.IpAddressParser;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

public class IpAddressParserTest extends TestCase {

    @Test
    public void test() throws IOException {
        Set<String> expected =
            Sets.newHashSet(
                "209.126.119.176:80",
                "64.227.2.136:8080",
                "163.172.28.22:80",
                "201.54.5.117:3128",
                "88.198.33.232:1080",
                "37.235.65.76:8080",
                "178.57.115.124:8080",
                "85.10.219.96:1080",
                "150.242.182.98:80",
                "202.142.158.114:8080",
                "190.122.186.194:8080",
                "91.217.42.2:8080",
                "85.10.219.100:1080",
                "139.255.40.130:8080",
                "85.10.219.103:1080",
                "202.69.38.82:8080",
                "85.10.219.98:1080",
                "89.140.125.17:80",
                "186.47.83.126:8080",
                "93.190.253.50:80",
                "101.4.136.34:81",
                "101.4.136.34:80",
                "183.91.87.36:3128",
                "94.200.195.218:8080",
                "180.210.201.54:3128",
                "85.26.146.169:80",
                "101.50.1.2:80",
                "202.154.190.234:8080",
                "74.143.193.83:3128",
                "175.41.139.245:8080",
                "182.253.209.205:3128",
                "183.91.33.41:8090",
                "188.94.164.178:8080",
                "115.78.160.247:8080",
                "70.65.233.174:8080",
                "202.138.127.66:80",
                "159.224.83.100:8080",
                "183.91.33.41:83",
                "183.91.33.41:84",
                "104.244.75.218:8080",
                "85.10.219.99:1080",
                "101.4.136.34:8080",
                "91.221.252.18:8080",
                "103.9.124.210:8080",
                "182.253.197.60:8080",
                "121.40.108.76:80",
                "190.122.186.226:8080",
                "85.10.219.102:1080",
                "218.27.136.169:8085",
                "122.154.66.193:8080");

        List<Proxy> proxies =
            new IpAddressParser().parse(
                FileUtils.readFileToString(
                    new File(
                        this.getClass()
                            .getClassLoader()
                            .getResource("ip-address.html")
                            .getFile())));
        Set<String> actual =
            proxies
                .stream()
                .map(p -> p.getUrl())
                .collect(Collectors.toSet());
        System.out.println(actual);
        Assert.assertEquals(expected, actual);

    }

}
