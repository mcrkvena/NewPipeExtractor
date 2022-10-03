package org.schabi.newpipe.extractor.services.rumble.extractors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.rumble.RumbleParsingHelper;

public class RumbleRelatedInfoItemExtractor implements StreamInfoItemExtractor{

    private final Element element;
    private final TimeAgoParser parser;
    private Document info;
    private String channelName;
    private String channelUrl;

    public RumbleRelatedInfoItemExtractor(final TimeAgoParser parser, final Element element) {
        this.element = element;
        this.parser = parser;
    }

    public RumbleRelatedInfoItemExtractor(final TimeAgoParser parser, final Node element, final Document info) {
        this.element = (Element) element;
        this.parser = parser;
        this.info = info;
    }

    public RumbleRelatedInfoItemExtractor(final TimeAgoParser parser, final Element element, final String channelName, final String channelUrl) {
        this.element = element;
        this.parser = parser;
        this.channelName = channelName;
        this.channelUrl = channelUrl;
    }

    @Override
    public String getName() throws ParsingException {
        try {
            final String title = element.select("h3.mediaList-heading").first().text();
            return title;
        } catch (final Exception e) {
            throw new ParsingException("Error parsing Stream title");
        }
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            final String url = element.select("a.mediaList-link").first().absUrl("href");
            return url;
        } catch (final Exception e) {
            throw new ParsingException("Error parsing video url");
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            final String thumbUrl = element.select("img.mediaList-image").first().attr("src");
            return thumbUrl;
        } catch (final Exception e) {
            throw new ParsingException("Error parsing thumbnail url");
        }
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public long getDuration() throws ParsingException {
        try {
            final Elements durationData = element.select("small.medialist-duration");
            if (durationData.isEmpty()) {
                throw new Exception("Could not extract video duration");
            }
            final String durationString = durationData.first().text();
            final long duration = RumbleParsingHelper.parseDurationString(durationString);
            return duration;

        } catch (final Exception e) {
            throw new ParsingException("Error parsing duration: " + e);
        }
    }

    @Override
    public long getViewCount() throws ParsingException {
        return 0;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            if (channelName == null) {
                channelName = element.select("h4.mediaList-by-heading").first().text();
            }
            return channelName;
        } catch (final Exception e) {
            throw new ParsingException("Error parsing uploader name");
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            if (channelUrl == null) {
                final String classStr =
                        element.getElementsByClass("user-image").first().attr("class");
                channelUrl = RumbleParsingHelper
                        .moreTotalMessMethodToGenerateUploaderUrl(classStr, info, getUploaderName());
            }
            return channelUrl;
        } catch (final Exception e) {
            throw new ParsingException(
                    "Error parsing uploader url: " + e.getMessage() + ". Cause:" + e.getCause());
        }
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return null;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return null;
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return null;
    }

}