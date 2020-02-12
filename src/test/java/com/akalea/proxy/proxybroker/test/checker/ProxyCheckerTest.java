package com.akalea.proxy.proxybroker.test.checker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.akalea.proxy.proxybroker.domain.Proxy;
import com.akalea.proxy.proxybroker.domain.ProxyStatus;
import com.akalea.proxy.proxybroker.domain.configuration.ProxyConfiguration;
import com.akalea.proxy.proxybroker.repository.ProxyProviderRepository;
import com.akalea.proxy.proxybroker.service.ProxyBroker;
import com.akalea.proxy.proxybroker.service.ProxyChecker;
import com.akalea.proxy.proxybroker.service.ProxyChecker.ProxyCheckTask;

import junit.framework.TestCase;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Unirest.class)
public class ProxyCheckerTest extends TestCase {

    @Test
    public void test() throws IOException {
        ProxyProviderRepository repo = new ProxyProviderRepository();

        PowerMockito.mockStatic(Unirest.class);

        GetRequest ok = Mockito.mock(GetRequest.class);
        HttpResponse okResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(okResponse.isSuccess()).thenReturn(true);
        Mockito.when(okResponse.getBody()).thenReturn("");
        Mockito.when(ok.asString()).thenReturn(okResponse);

        List<String> validationUrls =
            ProxyConfiguration
                .proxyProperties()
                .getProxy()
                .getProxies()
                .getCheck()
                .getValidators()
                .stream()
                .map(v -> v.getUrl())
                .collect(Collectors.toList());
        validationUrls
            .stream()
            .forEach(
                url -> {
                    Mockito
                        .when(Unirest.get(ArgumentMatchers.eq(url)))
                        .thenReturn(ok);
                });

        ProxyChecker checker = new ProxyChecker(new ProxyBroker());
        Map<String, Proxy> checked = new HashMap<String, Proxy>();
        Consumer<Proxy> handler = (p) -> checked.put(p.getUrl(), p);
        ProxyCheckTask task =
            new ProxyCheckTask()
                .setProxy(new Proxy().setUrl("test.com"))
                .setHandler(handler);
        checker.checkProxy(task);
        Assert.assertEquals(ProxyStatus.ok, task.getProxy().getLastCheck());
        Assert.assertTrue(checked.containsKey(task.getProxy().getUrl()));

        // test ko
        GetRequest ko = Mockito.mock(GetRequest.class);
        GetRequest proxyKo = Mockito.mock(GetRequest.class);
        Mockito
            .when(ko.proxy(ArgumentMatchers.any(), ArgumentMatchers.anyInt()))
            .thenReturn(proxyKo);
        HttpResponse koResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(koResponse.isSuccess()).thenReturn(false);
        Mockito.when(koResponse.getBody()).thenReturn("");
        Mockito.when(proxyKo.asString()).thenReturn(koResponse);
        validationUrls
            .stream()
            .forEach(
                url -> {
                    Mockito
                        .when(Unirest.get(ArgumentMatchers.eq(url)))
                        .thenReturn(ko);
                });
        checker.checkProxy(task);
        Assert.assertEquals(ProxyStatus.ko, task.getProxy().getLastCheck());
    }

}
