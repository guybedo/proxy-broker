package com.akalea.utils.jproxy;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.akalea.utils.jproxy.domain.Proxy;
import com.akalea.utils.jproxy.domain.parser.FreeProxyCzParser;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

public class FreeProxyCzParserTest extends TestCase {

    @Test
    public void test() throws IOException {
        Set<String> expected =
            Sets.newHashSet(
                "138.197.150.132:8080",
                "151.80.199.89:3128",
                "165.227.248.222:3128",
                "163.172.180.18:8811",
                "104.218.60.89:4145",
                "165.22.36.112:3128",
                "193.70.96.193:80",
                "12.109.102.86:64312",
                "193.70.96.195:80",
                "51.15.117.119:8080",
                "51.91.143.235:80",
                "51.91.212.159:3128",
                "5.135.181.83:80",
                "168.169.146.12:8080",
                "3.10.140.162:3128",
                "163.172.190.160:8811",
                "139.162.235.24:443",
                "165.234.102.177:8080",
                "159.89.245.69:3128",
                "167.172.237.113:3128",
                "51.158.123.35:8811",
                "50.253.211.61:32100",
                "178.128.39.39:8080",
                "195.154.173.28:80",
                "66.232.217.154:8080",
                "64.251.21.59:80",
                "198.23.143.5:1080",
                "162.243.25.182:32578",
                "159.89.95.24:8080",
                "24.181.205.134:54321");

        List<Proxy> proxies =
            new FreeProxyCzParser().parse(
                FileUtils.readFileToString(new ClassPathResource("free-proxy-cz.html").getFile()));
        Set<String> actual =
            proxies
                .stream()
                .map(p -> p.getUrl())
                .collect(Collectors.toSet());
        System.out.println(actual);
        Assert.assertEquals(expected, actual);
    }

}
