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
            Sets.newHashSet();

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
