package org.schabi.newpipe.extractor.services.bilibili.linkHandler;

import java.util.regex.Pattern;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bilibili.utils;

/*
General form of stream link url: https://m.bilibili.com/video/<ID> (mobile) and https://www.bilibili.com/video/<ID> (PC)
*/
public class BilibiliStreamLinkHandlerFactory extends LinkHandlerFactory{
    
    public static final String baseUrl = "https://www.bilibili.com/video/";
    public String p = "1";

    @Override
    public String getId(final String url) throws ParsingException {
        if(url.contains("p=")){
            p = url.split("p=")[1].split("&")[0];
        }
        if (url.split("/")[url.split("/").length-1].startsWith("BV")) {// TODO: https...../BV....../(see slash)
            String  parseResult = url.split(Pattern.quote("/BV"))[1].split("\\?")[0].split("/")[0];
            return "BV"+parseResult + "?p="+p;
        } else if (url.contains("bvid=")) {
            String  parseResult = url.split(Pattern.quote("bvid="))[1].split("&")[0];
            return parseResult+ "?p="+p;
        } else if (url.split("/")[url.split("/").length-1].startsWith("av")) {
            String  parseResult = url.split(Pattern.quote("av"))[1].split("\\?")[0];
            return new utils().av2bv(Long.parseLong(parseResult))+ "?p="+p;
        }else if (url.contains("aid=")) {
            String  parseResult = url.split(Pattern.quote("aid="))[1].split("&")[0];
            return new utils().av2bv(Long.parseLong(parseResult))+ "?p="+p;
        }
        else{
            throw new ParsingException("Not a bilibili video link.");
        }
    }

    @Override
    public String getUrl(final String id) {
        return "https://bilibili.com/" + id;
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        try {
            getId(url);
            return true;
        } catch (ParsingException e) {
            return false;
        }
    }

}