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
import com.akalea.proxy.proxybroker.domain.parser.ProxyListsParser;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

public class ProxyListsParserTest extends TestCase {

    @Test
    public void test() throws IOException {
        Set<String> expected =
            Sets.newHashSet(
                "207.118.169.6:61882",
                "67.235.0.205:54508",
                "67.234.55.1:58145",
                "207.118.151.80:53228",
                "67.235.42.130:65000",
                "67.235.14.159:55998",
                "67.235.14.12:55853",
                "67.235.6.31:53822",
                "67.235.34.153:63160",
                "67.235.2.56:54809");

        List<Proxy> proxies =
            new ProxyListsParser().parse(
                FileUtils.readFileToString(
                    new File(
                        this.getClass()
                            .getClassLoader()
                            .getResource("proxylists.html")
                            .getFile())));
        Set<String> actual =
            proxies
                .stream()
                .map(p -> p.getUrl())
                .collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);
    }

}
