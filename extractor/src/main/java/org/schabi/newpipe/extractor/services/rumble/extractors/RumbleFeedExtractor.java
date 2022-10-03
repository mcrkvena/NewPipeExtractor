package org.schabi.newpipe.extractor.services.rumble.extractors;

import java.io.IOException;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

public class RumbleFeedExtractor extends KioskExtractor<StreamInfoItem>{

    private RumbleCommonCodeTrendingAndChannel sharedTrendingAndChannelCode;
    private Document info;

    public RumbleFeedExtractor(StreamingService streamingService, ListLinkHandler linkHandler, String kioskId) {
        super(streamingService, linkHandler, kioskId);
        try {
            sharedTrendingAndChannelCode = new RumbleCommonCodeTrendingAndChannel(getServiceId(), getUrl());
        } catch (final ParsingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() throws ParsingException {
        return null;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return sharedTrendingAndChannelCode.extractAndGetInfoItemsFromPage(info);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(Page page) throws IOException, ExtractionException {
        return sharedTrendingAndChannelCode.extractAndGetInfoItemsFromPage(info);
    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        info = Jsoup.parse(getDownloader().get(getUrl()).responseBody());
    }
    
}