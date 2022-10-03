package org.schabi.newpipe.extractor.services.rumble.linkHandler;

import java.util.regex.Pattern;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

public class RumbleStreamLinkHandlerFactory extends LinkHandlerFactory{
    
    public static final String baseUrl = "https://rumble.com";

    private static final String ID_REGEX = "^v[a-zA-Z0-9]{4,}-?";

    @Override
    public String getId(final String url) throws ParsingException {
        if(url.contains("rumble.com/")){
            String id = url.substring(url.lastIndexOf('/') + 1);
            return id;
        }
        else{
            throw new ParsingException("Not a rumble video link.");
        }
        //return Parser.matchGroup(ID_REGEX, url, 2);
    }

    @Override
    public String getUrl(final String id) {
        return "https://rumble.com/" + id;
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