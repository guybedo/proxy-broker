package com.akalea.proxy.proxybroker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.parser.FreeProxyListsParser;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

public class FreeProxyListsParserTest extends TestCase {

    @Test
    public void test() throws IOException {
        Set<String> expected =
            Sets.newHashSet(
                "81.23.32.47:443",
                "183.91.33.41:8081",
                "223.25.101.84:54795",
                "101.50.1.2:80",
                "93.87.17.1:53281",
                "94.41.247.53:8080",
                "180.183.16.206:8080",
                "181.30.28.12:3128",
                "190.216.147.70:41310",
                "203.202.245.58:80",
                "134.209.85.26:8080",
                "177.105.242.70:20183",
                "180.180.124.248:60098",
                "176.99.75.39:8080",
                "203.128.79.18:43227",
                "177.92.67.230:53281",
                "200.89.178.209:3128",
                "124.158.167.18:8080",
                "193.106.130.249:8080",
                "190.214.52.226:53281",
                "95.0.66.11:9090",
                "125.25.33.86:8080",
                "95.67.47.94:53281",
                "200.52.141.162:53281",
                "200.89.178.209:8080",
                "79.175.106.21:8080",
                "186.148.190.78:9991",
                "78.156.48.10:48665",
                "200.89.178.218:80",
                "200.89.178.208:8080",
                "124.41.240.43:47520",
                "167.71.212.154:80",
                "118.172.211.37:58728",
                "186.250.29.1:53281",
                "173.249.35.163:1448",
                "197.255.60.142:80",
                "124.158.175.2:8080",
                "34.76.12.26:80",
                "112.47.3.53:3128",
                "167.71.213.179:8080",
                "159.192.105.152:8080",
                "36.66.206.74:80",
                "190.142.145.83:8080",
                "101.99.53.133:8888",
                "61.7.141.34:8080",
                "159.89.95.24:8080",
                "125.133.65.207:80",
                "167.71.202.40:8080",
                "118.69.140.108:53281",
                "62.240.53.103:8080");

        List<Proxy> proxies =
            new FreeProxyListsParser().parse(
                FileUtils.readFileToString(
                    new File(
                        this.getClass()
                            .getClassLoader()
                            .getResource("freeproxylists.html")
                            .getFile())));
        Set<String> actual =
            proxies
                .stream()
                .map(p -> p.getUrl())
                .collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);
    }

}
