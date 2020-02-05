package com.akalea.proxy.proxybroker.test.fetcher;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyProperties;
import com.akalea.proxy.proxybroker.service.ProxyBroker;
import com.akalea.proxy.proxybroker.service.ProxyChecker;
import com.akalea.proxy.proxybroker.service.ProxyFetcher;

import junit.framework.TestCase;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Unirest.class)
public class PageFetchingTest extends TestCase {

    @Test
    public void test() throws IOException {
        ProxyProperties proxyProperties = ProxyConfiguration.proxyProperties();
        ProxyBroker broker = new ProxyBroker();
        ProxyFetcher fetcher = new ProxyFetcher(proxyProperties, broker, new ProxyChecker(broker));

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
        List<String> pages = fetcher.fetchPages(urlPattern);
        Assert.assertEquals(3, pages.size());
    }

}
