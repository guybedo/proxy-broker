package com.akalea.proxy.proxybroker.test.fetcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyProvider;
import com.akalea.proxy.proxybroker.domain.ProxyStatus;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties;
import com.akalea.proxy.proxybroker.repository.ProxyProviderRepository;
import com.akalea.proxy.proxybroker.service.ProxyBroker;
import com.akalea.proxy.proxybroker.service.ProxyChecker;
import com.akalea.proxy.proxybroker.service.ProxyFetcher;
import com.google.common.collect.Sets;

import junit.framework.TestCase;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Unirest.class)
public class ProxyProviderRepositoryTest extends TestCase {

    @Test
    public void test() throws IOException {
        ProxyProviderRepository repo = new ProxyProviderRepository();

        PowerMockito.mockStatic(Unirest.class);

        GetRequest ok = Mockito.mock(GetRequest.class);
        HttpResponse okResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(okResponse.isSuccess()).thenReturn(true);
        Mockito.when(okResponse.getBody()).thenReturn("");
        Mockito.when(ok.asString()).thenReturn(okResponse);

        GetRequest ko = Mockito.mock(GetRequest.class);
        HttpResponse koResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(koResponse.isSuccess()).thenReturn(false);
        Mockito.when(koResponse.getBody()).thenReturn("");
        Mockito.when(ko.asString()).thenReturn(koResponse);

        Mockito.when(Unirest.get(ArgumentMatchers.eq("http://test.com?page=0"))).thenReturn(ok);
        Mockito.when(Unirest.get(ArgumentMatchers.eq("http://test.com?page=1"))).thenReturn(ok);
        Mockito.when(Unirest.get(ArgumentMatchers.eq("http://test.com?page=2"))).thenReturn(ok);
        Mockito.when(Unirest.get(ArgumentMatchers.eq("http://test.com?page=3"))).thenReturn(ko);
        String urlPattern = "http://test.com?page={page}";
        List<String> pages = repo.fetchPages(urlPattern);
        Assert.assertEquals(3, pages.size());
    }

    @Test
    public void testFetchProviderProxies() throws IOException {
        ProxyProvider provider =
            new ProxyProvider()
                .setUrl("http://www.freeproxylists.net")
                .setPageUrl("http://www.freeproxylists.net/?page={page}")
                .setFormat("freeproxylists.net");

        String firstPage =
            FileUtils.readFileToString(
                new File(
                    this.getClass()
                        .getClassLoader()
                        .getResource("freeproxylists.html")
                        .getFile()));

        PowerMockito.mockStatic(Unirest.class);
        GetRequest ok = Mockito.mock(GetRequest.class);
        HttpResponse okResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(okResponse.isSuccess()).thenReturn(true);
        Mockito.when(okResponse.getBody()).thenReturn(firstPage);
        Mockito.when(ok.asString()).thenReturn(okResponse);

        GetRequest ko = Mockito.mock(GetRequest.class);
        HttpResponse koResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(koResponse.isSuccess()).thenReturn(false);
        Mockito.when(koResponse.getBody()).thenReturn("");
        Mockito.when(ko.asString()).thenReturn(koResponse);

        Mockito.when(Unirest.get(ArgumentMatchers.eq("http://www.freeproxylists.net/?page=0")))
            .thenReturn(ok);
        Mockito.when(Unirest.get(ArgumentMatchers.eq("http://www.freeproxylists.net/?page=1")))
            .thenReturn(ko);

        ProxyProviderRepository repo = new ProxyProviderRepository();
        List<Proxy> proxies = repo.fetchProviderProxies(provider);

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

        Set<String> actual =
            proxies
                .stream()
                .map(p -> p.getUrl())
                .collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);

        Assert.assertEquals(ProxyStatus.ok, provider.getStatus());
        Assert.assertNotNull(provider.getLastStatusUpdate());
    }

}
