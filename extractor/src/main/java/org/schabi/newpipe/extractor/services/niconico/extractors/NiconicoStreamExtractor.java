package org.schabi.newpipe.extractor.services.niconico.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.services.niconico.NiconicoService;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NiconicoStreamExtractor extends StreamExtractor {
    private JsonObject watch;

    public NiconicoStreamExtractor(final StreamingService service,
                                   final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public long getViewCount() throws ParsingException {
        return watch.getObject("video").getObject("count").getLong("view");
    }

    @Override
    public long getLength() throws ParsingException {
        return watch.getObject("video").getLong("duration");
    }

    @Override
    public long getLikeCount() throws ParsingException {
        return  watch.getObject("video").getObject("count").getLong("like");
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        return new Description(watch.getObject("video").getString("description"), 1);
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        return watch.getObject("video").getObject("thumbnail").getString("url");
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        if (isChannel()) {
            return NiconicoService.CHANNEL_URL
                    + watch.getObject("channel").getString("id");
        }
        return NiconicoService.USER_URL + watch.getObject("owner").getLong("id");
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        if (isChannel()) {
            return watch.getObject("channel").getString("name");
        }
        return watch.getObject("owner").getString("nickname");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        if (isChannel()) {
            return  watch.getObject("channel")
                    .getObject("thumbnail").getString("url");
        }
        return watch.getObject("owner").getString("iconUrl");
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        final List<VideoStream> videoStreams = new ArrayList<>();

        final JsonObject session
                = watch.getObject("media").getObject("delivery").getObject("movie");

        final String dmc
                = session.getObject("session").getArray("urls")
                .getObject(0).getString("url") + "?_format=json";

        final String s = NiconicoDMCPayloadBuilder.buildJSON(session.getObject("session"));

        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", Collections.singletonList("application/json"));

        final Response response = getDownloader().post(
                dmc, headers, s.getBytes(StandardCharsets.UTF_8), NiconicoService.LOCALE);

        try {
            final JsonObject content = JsonParser.object().from(response.responseBody());

            final String contentURL = content.getObject("data").getObject("session")
                    .getString("content_uri");
            //videoStreams.add(new VideoStream(contentURL, MediaFormat.MPEG_4, "360p"));
            videoStreams.add(new VideoStream.Builder()
                        .setId(contentURL)
                        .setContent(contentURL, true)
                        .setIsVideoOnly(false)
                        .setMediaFormat(MediaFormat.MPEG_4)
                        .setResolution("360p")
                        .build());

        } catch (final JsonParserException e) {
            throw new ExtractionException("could not get video contents.");
        }

        return  videoStreams;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Nonnull
    @Override
    public List<String> getTags() throws ParsingException {
        final List<String> tags = new ArrayList<>();
        final JsonArray items = watch.getObject("tag").getArray("items");
        for (int i = 0; i < items.size(); i++) {
            tags.add(items.getObject(i).getString("name"));
        }

        return tags;
    }

    @Nullable
    @Override
    public InfoItemsCollector<? extends InfoItem, ? extends InfoItemExtractor> getRelatedItems()
            throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(
                getServiceId());

        final String url = NiconicoService.RELATION_URL + getId();
        final Document response = Jsoup.parse(
                getDownloader().get(url, NiconicoService.LOCALE).responseBody());

        final Elements videos = response.getElementsByTag("video");

        for (final Element e: videos) {
            collector.commit(new NiconicoRelationVideoExtractor(e));
        }

        return collector;
    }

    @Override
    public void onFetchPage(final @Nonnull Downloader downloader)
            throws IOException, ExtractionException {
        final String url = getLinkHandler().getUrl();
        final Response response = downloader.get(url, null, NiconicoService.LOCALE);
        final Document page = Jsoup.parse(response.responseBody());
        try {
            watch = JsonParser.object().from(
                    page.getElementById("js-initial-watch-data").attr("data-api-data"));
        } catch (final JsonParserException e) {
            throw new ExtractionException("could not extract watching page");
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return watch.getObject("video").getString("title");
    }

    private Boolean isChannel() {
        return watch.isNull("owner");
    }
}