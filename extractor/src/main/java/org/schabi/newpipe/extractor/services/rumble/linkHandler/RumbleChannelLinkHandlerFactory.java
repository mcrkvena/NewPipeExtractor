package org.schabi.newpipe.extractor.services.rumble.linkHandler;

import java.util.List;
import java.util.regex.Pattern;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

public class RumbleChannelLinkHandlerFactory extends ListLinkHandlerFactory{
    
    public static final String baseUrl = "https://rumble.com/";

    private static final RumbleChannelLinkHandlerFactory INSTANCE =
        new RumbleChannelLinkHandlerFactory();


    public static RumbleChannelLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId(final String url) throws ParsingException {
        if (url.contains("rumble.com/")) {
            String channel_id = url.substring(url.lastIndexOf('/') + 1);
            return channel_id;
        } else {
            throw new ParsingException("Not a rumble channel link.");
        }
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

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        return "https://rumble.com/" + id;
    }

}