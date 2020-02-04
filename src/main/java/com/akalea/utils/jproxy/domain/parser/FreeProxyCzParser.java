package com.akalea.utils.jproxy.domain.parser;

import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.utils.jproxy.domain.Proxy;

public class FreeProxyCzParser implements ProxyListParser {

    private final static Logger logger = LoggerFactory.getLogger(FreeProxyCzParser.class);

    private Pattern pattern =
        Pattern.compile(
            "<script type=\"text/javascript\">document.write\\(Base64.decode\\(\"(.*)\"\\)\\)</script>");

    private String parserId = "free-proxy.cz";

    @Override
    public List<Proxy> parse(String content) {
        String preprocessed = preprocessContent(content);
        return new HtmlTableParser().parse(preprocessed);
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

    public String getParserId() {
        return parserId;
    }

}