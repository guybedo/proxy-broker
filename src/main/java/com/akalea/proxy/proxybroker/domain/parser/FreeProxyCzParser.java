package com.akalea.proxy.proxybroker.domain.parser;

import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.proxy.proxybroker.domain.Proxy;

public class FreeProxyCzParser implements ProxyDataParser {

    private final static Logger logger = LoggerFactory.getLogger(FreeProxyCzParser.class);

    public static String format = "free-proxy.cz";

    private Pattern pattern =
        Pattern.compile(
            "<script type=\"text/javascript\">document.write\\(Base64.decode\\(\"([a-zA-Z0-9=]*)\"\\)\\)</script>");

    @Override
    public List<Proxy> parse(String content) {
        String preprocessed = preprocessContent(content);
        return new HtmlTableJsoupParser("#proxy_list tbody tr").parse(preprocessed);
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
            return new String(Base64.getDecoder().decode(encoded), "UTF-8");
        } catch (Exception e) {
            logger.debug(
                String.format("Could not decoded ip address from encoded form %s", encoded));
            return "n/a";
        }
    }

}