package org.schabi.newpipe.extractor.services.bitchute.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.bitchute.BitchuteService.BITCHUTE_LINK;
import static org.schabi.newpipe.extractor.services.bitchute.linkHandler.BitchuteKioskLinkHandlerFactory.TRENDING_DAY;
import static org.schabi.newpipe.extractor.services.bitchute.linkHandler.BitchuteKioskLinkHandlerFactory.TRENDING_MONTH;
import static org.schabi.newpipe.extractor.services.bitchute.linkHandler.BitchuteKioskLinkHandlerFactory.TRENDING_WEEK;

public class BitchuteTrendingKioskExtractor extends KioskExtractor<StreamInfoItem> {

    private Document doc;

    public BitchuteTrendingKioskExtractor(final StreamingService streamingService,
                                          final ListLinkHandler linkHandler,
                                          final String kioskId) {
        super(streamingService, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        doc = Jsoup.parse(downloader.get(BITCHUTE_LINK, getExtractorLocalization()).responseBody(),
                BITCHUTE_LINK);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return getId();
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final String selector;
        switch (getId()) {
            case TRENDING_MONTH:
                selector = "#trending-month div.video-result-container";
                break;
            case TRENDING_WEEK:
                selector = "#trending-week div.video-result-container";
                break;
            case TRENDING_DAY:
            default:
                selector = "#trending-day div.video-result-container";
        }
        for (final Element e : doc.select(selector)) {
            collector.commit(new BitchuteTrendingStreamInfoItemExtractor(getTimeAgoParser(), e));
        }
        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        return null;
    }
}