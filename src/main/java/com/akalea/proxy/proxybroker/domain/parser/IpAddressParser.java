package com.akalea.proxy.proxybroker.domain.parser;

import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;

public class IpAddressParser implements ProxyDataParser {

    private final static Logger logger = LoggerFactory.getLogger(IpAddressParser.class);

    public static String format = "ipaddress.com";

    private String ipPattern   = "([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3})";
    private String portPattern = "([1-9][0-9]{1,4})";

    private Pattern proxyPattern = Pattern.compile(String.format("%s:%s", ipPattern, portPattern));

    private String tableRowSelector = "table.proxylist tbody tr";

    @Override
    public List<Proxy> parse(String content) {
        Document doc = Jsoup.parse(content);
        Elements rows = doc.select(tableRowSelector);
        return rows
            .stream()
            .map(r -> {
                Elements cols = r.select("td");
                for (int idx = 0; idx < cols.size(); idx++) {
                    Element col = cols.get(idx);
                    Matcher ipMatcher = proxyPattern.matcher(col.text());
                    if (ipMatcher.matches()) {
                        return new Proxy()
                            .setUrl(
                                String.format(
                                    "%s:%s",
                                    ipMatcher.group(1),
                                    ipMatcher.group(2)));
                    }
                }
                return null;
            })
            .filter(p -> p != null)
            .collect(Collectors.toList());
    }

}