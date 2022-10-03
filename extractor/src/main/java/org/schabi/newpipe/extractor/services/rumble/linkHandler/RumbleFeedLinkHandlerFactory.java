package org.schabi.newpipe.extractor.services.rumble.linkHandler;

import java.util.List;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

public class RumbleFeedLinkHandlerFactory extends ListLinkHandlerFactory{

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        return "https://rumble.com/battle-leaderboard";
    }

    @Override
    public String getId(String url) throws ParsingException {
        return "Trending";
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        return url.equals("https://rumble.com/battle-leaderboard");
    }
    
}