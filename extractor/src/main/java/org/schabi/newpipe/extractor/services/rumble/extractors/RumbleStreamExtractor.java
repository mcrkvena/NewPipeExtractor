package org.schabi.newpipe.extractor.services.rumble.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.rumble.RumbleParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamSegment;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.schabi.newpipe.extractor.stream.Stream.ID_UNKNOWN;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class RumbleStreamExtractor extends StreamExtractor {

    private final String videoUploaderJsonKey = "author";
    private final String videoTitleJsonKey = "title";
    private final String videoCoverImageJsonKey = "i";
    private final String videoDateJsonKey = "pubDate";
    private final String videoDurationJsonKey = "duration";

    private final String videoViewerCountHtmlKey = "span.media-heading-info";

    private Document doc;
    JsonObject embedJsonStreamInfoObj;
    private String realVideoId;

    private int ageLimit = -1;
    private List<VideoStream> videoStreams;
    private String hlsUrl = "";
    
    public RumbleStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        assertPageFetched();
        final String thumbUrl = embedJsonStreamInfoObj.getString(videoCoverImageJsonKey);
        return thumbUrl;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        assertPageFetched();
        return embedJsonStreamInfoObj.getObject(videoUploaderJsonKey).getString("url");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        assertPageFetched();
        final String uploaderName = embedJsonStreamInfoObj.getObject(videoUploaderJsonKey).getString("name");
        return uploaderName;
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        if (videoStreams == null) {
            videoStreams = extractVideoStreams();
        }
        return videoStreams;
    }

    private List<VideoStream> extractVideoStreams() throws ExtractionException {
        assertPageFetched();

        final List<VideoStream> videoStreamsList = new ArrayList<>();
        final String videoAlternativesKey = "ua";
        final String videoMetaKey = "meta";
        final String videoUrlKey = "url";

        final Set<String> formatKeys =
                embedJsonStreamInfoObj.getObject(videoAlternativesKey).keySet();
        for (final String formatKey : formatKeys) {
            final JsonObject formatObj =
                    embedJsonStreamInfoObj.getObject(videoAlternativesKey).getObject(formatKey);
            final Set<String> resolutionKeys = formatObj.keySet();
            for (final String res : resolutionKeys) {
                final JsonObject metadata =
                        formatObj.getObject(res).getObject(videoMetaKey);
                final String videoUrl =
                        formatObj.getObject(res).getString(videoUrlKey); 
                final MediaFormat format = MediaFormat.getFromSuffix(formatKey);
                final VideoStream.Builder builder = new VideoStream.Builder()
                        .setId(ID_UNKNOWN)
                        .setIsVideoOnly(false)
                        .setResolution(res + "p")
                        .setContent(videoUrl, true)
                        .setMediaFormat(format);

                videoStreamsList.add(builder.build());
            }
        }

        return videoStreamsList;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
final Response response = downloader.get(getUrl());
        doc = Jsoup.parse(response.responseBody(), getUrl());

        final String jsonString =
                doc.getElementsByAttributeValueContaining("type", "application/ld+json").first()
                        .childNodes().get(0).toString();
        final JsonArray jsonObj;
        try {
            jsonObj = JsonParser.array().from(jsonString);
            final String embedUrl = jsonObj.getObject(0).getString("embedUrl");


            final URL url = Utils.stringToURL(embedUrl);
            final String[] splitPaths = url.getPath().split("/");

            if (splitPaths.length == 3 && splitPaths[1].equalsIgnoreCase("embed")) {
                realVideoId = splitPaths[2];
            }

        } catch (final JsonParserException e) {
            e.printStackTrace();
        }

        final String queryUrl = "https://rumble.com/embedJS/u3/?request=video&ver=2&v="
                + realVideoId;

        final Response response2 = downloader.get(
                queryUrl);
                
        try {
            embedJsonStreamInfoObj = JsonParser.object().from(response2.responseBody());
        } catch (final JsonParserException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() throws ParsingException {
        assertPageFetched();
        final String title =
                Parser.unescapeEntities(embedJsonStreamInfoObj.getString(videoTitleJsonKey), true);
        return title;
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        final String textualDate = embedJsonStreamInfoObj.getString(videoDateJsonKey);
        return textualDate;
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final String textualUploadDate = getTextualUploadDate();
        if (isNullOrEmpty(textualUploadDate)) {
            return null;
        }
        return new DateWrapper(YoutubeParsingHelper.parseDateFrom(textualUploadDate), false);
    }

    @Override
    public long getLength(){
        assertPageFetched();
        final Number duration = embedJsonStreamInfoObj.getNumber(videoDurationJsonKey);
        return duration.longValue();
    }

    @Override
    public String getUploaderAvatarUrl () throws ParsingException {
        assertPageFetched();
        final Elements elems = doc.getElementsByClass("media-by--a");
        final String theUserPathToHisAvatar =
                elems.get(0).getElementsByTag("i").first().attributes().get("class");
        try {
            final String thumbnailUrl = RumbleParsingHelper
                    .totalMessMethodToGetUploaderThumbnailUrl(theUserPathToHisAvatar, doc);
            return thumbnailUrl;
        } catch (final Exception e) {
            throw new ParsingException(
                    "Could not extract the avatar url: " + theUserPathToHisAvatar);
        }
    }

    @Override
    public String getHlsUrl() {
        try {
            this.hlsUrl = embedJsonStreamInfoObj.getObject("ua").getObject("hls")
                    .getObject("auto").getString("url", "");

        } catch (final Exception e) {

        }
        return this.hlsUrl;
    }

    @Override
    public Description getDescription(){
        assertPageFetched();
        final List<Node> nodes = doc.select("p.media-description").first().childNodes();
        String description = "";
        for (final Node node : nodes) {
            if (node instanceof TextNode) {
                if (!((TextNode) node).isBlank()) {
                    description += node.toString();
                }
            }
        }
        return new Description(Parser.unescapeEntities(description, false), Description.PLAIN_TEXT);
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        if (ageLimit == -1) {
            ageLimit = NO_AGE_LIMIT;
        }

        return ageLimit;
    }

    @Override
    public long getViewCount() throws ParsingException {
        assertPageFetched();
        final Elements viewCountData = doc.select(videoViewerCountHtmlKey);
        if (viewCountData.size() == 0) {
            throw new ParsingException("The viewCount match element is missing");
        }
        for (final Element maybeViewCount : viewCountData) {
            if (maybeViewCount.text().contains("Views")) {
                final String views = maybeViewCount.text();
                return Long.parseLong(Utils.removeNonDigitCharacters(views));
            }
        }
        return -1;
    }

    @Override
    public long getLikeCount(){
        return -1;
    }
}