package com.akalea.utils.jproxy.domain.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.akalea.utils.jproxy.domain.Proxy;
import com.google.common.collect.Lists;

public class HtmlTableJsoupParser implements ProxyListParser {

    private Pattern ipPattern   =
        Pattern.compile("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}");
    private Pattern portPattern = Pattern.compile("[1-9][0-9]{1,4}");

    private String parserId = "html";

    private String tableRowSelector;

    public HtmlTableJsoupParser(String tableSelector) {
        super();
        this.tableRowSelector = tableSelector;
    }

    @Override
    public List<Proxy> parse(String content) {
        Document doc = Jsoup.parse(content);
        Elements rows = doc.select(tableRowSelector);
        return rows
            .stream()
            .map(r -> {
                Elements cols = r.select("td");
                for (int idx = 0; idx < cols.size() - 1; idx++) {
                    Element col = cols.get(idx);
                    Element nextCol = cols.get(idx + 1);
                    Matcher ipMatcher = ipPattern.matcher(col.text());
                    Matcher portMatcher = portPattern.matcher(nextCol.text());
                    if (ipMatcher.matches() && portMatcher.matches()) {
                        return new Proxy()
                            .setUrl(
                                String.format(
                                    "%s:%s",
                                    ipMatcher.group(),
                                    portMatcher.group()));
                    }
                }
                return null;
            })
            .filter(p -> p != null)
            .collect(Collectors.toList());
    }

    public String getParserId() {
        return parserId;
    }

}