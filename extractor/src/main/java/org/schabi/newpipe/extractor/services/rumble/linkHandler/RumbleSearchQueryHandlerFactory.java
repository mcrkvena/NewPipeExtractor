package org.schabi.newpipe.extractor.services.rumble.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class RumbleSearchQueryHandlerFactory extends SearchQueryHandlerFactory {


    @Override
    public String getUrl(final String query, final List<String> contentFilter, final String sortFilter)
            throws ParsingException {
        try {
            return "https://rumble.com/search/video?q=" + URLEncoder.encode(query, "UTF-8");

        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("query \"" + query + "\" could not be encoded", e);
        }
    }
}