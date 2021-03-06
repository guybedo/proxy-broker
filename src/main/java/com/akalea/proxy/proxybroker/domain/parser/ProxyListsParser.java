package com.akalea.proxy.proxybroker.domain.parser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;

public class ProxyListsParser implements ProxyDataParser {

    private final static Logger logger = LoggerFactory.getLogger(ProxyListsParser.class);

    public static String format = "proxylists.net";

    private Pattern pattern      =
        Pattern.compile("<script type='text/javascript'>eval\\(unescape\\('(.*)'\\)\\);</script>(<noscript>Please enable javascript</noscript>)?");
    private Pattern ipRefPattern =
        Pattern.compile("self.document.writeln\\(\"([0-9.]*)\"\\);");

    @Override
    public List<Proxy> parse(String content) {
        String preprocessed = preprocessContent(content);
        return new HtmlTableRegexParser().parse(preprocessed);
    }

    private String preprocessContent(String content) {
        Matcher matcher = pattern.matcher(content);
        StringBuffer output = new StringBuffer();
        int currentPos = 0;
        while (matcher.find()) {
            output.append(content.subSequence(currentPos, matcher.start()));
            String replacement = decodeIpAddress(matcher.group(1));
            output.append(replacement);
            currentPos = matcher.end();
        }
        output.append(content.subSequence(currentPos, content.length()));
        return output.toString();
    }

    private String decodeIpAddress(String encoded) {
        try {
            String ref =
                new String(
                    Hex.decodeHex(encoded.replaceAll("%", "").toCharArray()),
                    "UTF-8");
            Matcher matcher = ipRefPattern.matcher(ref);
            matcher.find();
            return matcher.group(1);
        } catch (Exception e) {
            logger.debug(
                String.format("Could not decoded ip address from encoded form %s", encoded));
            return "n/a";
        }
    }

}