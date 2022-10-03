package org.schabi.newpipe.extractor.services.bilibili.extractors;

import static org.schabi.newpipe.extractor.services.bilibili.BilibiliService.getHeaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.services.bilibili.linkHandler.BilibiliChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bilibili.linkHandler.BilibiliStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bilibili.utils;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import javax.annotation.Nonnull;

public class BilibiliStreamExtractor extends StreamExtractor {

    private JsonObject watch;
    int cid = 0;
    int duration = 0;
    String id = "";
    JsonObject page = null;
    public BilibiliStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return watch.getString("pic").replace("http:", "https:");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return BilibiliChannelLinkHandlerFactory.baseUrl  +watch.getObject("owner").getLong("mid");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return watch.getObject("owner").getString("name");
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
         final List<AudioStream> audioStreams = new ArrayList<>();
         String bvid = watch.getString("bvid");
         String response = getDownloader().get("https://api.bilibili.com/x/player/playurl"+"?cid="+cid+"&bvid="+bvid+"&fnval=16", getHeaders()).responseBody();
         JsonObject responseJson = new JsonObject();
         try {
             responseJson =  JsonParser.object().from(response);
         } catch (JsonParserException e) {
             e.printStackTrace();
         }
         JsonArray audioArray =responseJson.getObject("data").getObject("dash").getArray("audio") ;
         String url = audioArray.getObject(0).getString("baseUrl");
         audioStreams.add(new AudioStream.Builder().setId("bilibili-"+bvid+"-audio").setContent(url,true).setMediaFormat(MediaFormat.M4A).setAverageBitrate(192000).build());
         return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        final List<VideoStream> videoStreams = new ArrayList<>();
        String response = getDownloader().get("https://api.live.bilibili.com/room/v1/Room/playUrl?qn=10000&platform=h5&cid=" + getId(), getHeaders()).responseBody();
        try {
            String url = JsonParser.object().from(response).getObject("data").getArray("durl").getObject(0).getString("url");
            videoStreams.add(new VideoStream.Builder().setContent(url,true).setId("bilibili-"+watch.getLong("uid") +"-live").setIsVideoOnly(false).setResolution("720p").setDeliveryMethod(DeliveryMethod.HLS).build());
        } catch (JsonParserException e) {
            e.printStackTrace();
        }
        return videoStreams;
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        String url = "";
        try {
        String response = getDownloader().get("https://api.live.bilibili.com/room/v1/Room/playUrl?qn=80&platform=h5&cid=" + getId(), getHeaders()).responseBody();

            url = JsonParser.object().from(response).getObject("data").getArray("durl").getObject(0).getString("url");
        } catch (JsonParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ReCaptchaException e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        final List<VideoStream> videoStreams = new ArrayList<>();
         String bvid = watch.getString("bvid");
         String response = getDownloader().get("https://api.bilibili.com/x/player/playurl"+"?cid="+cid+"&bvid="+bvid+"&fnval=16", getHeaders()).responseBody();
         JsonObject responseJson = new JsonObject();
         try {
             responseJson =  JsonParser.object().from(response);
         } catch (JsonParserException e) {
             e.printStackTrace();
         }
         String url = "";
         String desc ="";
         JsonArray videoArray =responseJson.getObject("data").getObject("dash").getArray("video") ;
         for(int i=0; i< videoArray.size(); i++){
             if(videoArray.getObject(i).getInt("id") > 64){
                 continue;
             }
            url = videoArray.getObject(i).getString("baseUrl");
         }
         videoStreams.add(new VideoStream.Builder().setContent(url,true).setMediaFormat( MediaFormat.MPEG_4).setId("bilibili-"+bvid+"-video").setIsVideoOnly(true).setResolution("720p").build());
        return videoStreams;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        String url = getLinkHandler().getOriginalUrl();
        id =  utils.getPureBV(getId());
        url = utils.getUrl(url, id);
        final String response = downloader.get(url).responseBody();
        try {
            watch = JsonParser.object().from(response).getObject("data");
        } catch (JsonParserException e) {
            e.printStackTrace();
        }
        page = watch.getArray("pages").getObject(Integer.parseInt(getLinkHandler().getUrl().split("p=")[1].split("&")[0])-1);
        cid = page.getInt("cid");
        duration = page.getInt("duration");
    }

    @Override
    public String getName() throws ParsingException {
        String title = watch.getString("title");
        return title;
    }
    @Override
    public long getLength() throws ParsingException {
        return duration;
    }
    @Override
    public String getUploaderAvatarUrl () throws ParsingException {
        return watch.getObject("owner").getString("face").replace("http:", "https:");
    }
    @Override
    public Description getDescription() throws ParsingException {
        return new Description(watch.getString("desc"), Description.PLAIN_TEXT);
    }


    @Override
    public long getViewCount() throws ParsingException {
        return watch.getObject("stat").getLong("view");
    }
    @Override
    public long getLikeCount() throws ParsingException {
        return watch.getObject("stat").getLong("coin");
    }

    @Nonnull
    @Override
    public List<String> getTags() throws ParsingException {
        List<String> tags = new ArrayList<>();
        try {
            JsonArray respArray = JsonParser.object().from(getDownloader().get("https://api.bilibili.com/x/tag/archive/tags?bvid=" + utils.getPureBV(getId()), getHeaders()).responseBody()).getArray("data");
            for(int i = 0; i< respArray.size(); i++){
                tags.add(respArray.getObject(i).getString("tag_name"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ReCaptchaException e) {
            e.printStackTrace();
        } catch (JsonParserException e) {
            e.printStackTrace();
        }
        return tags;
    }

    @Override
    public InfoItemsCollector<? extends InfoItem, ? extends InfoItemExtractor>getRelatedItems() throws ParsingException {
        InfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        String response = null;
        try {
            response = getDownloader().get("https://api.bilibili.com/x/player/pagelist?bvid="+id, getHeaders()).responseBody();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ReCaptchaException e) {
            e.printStackTrace();
        }
        try {
            JsonObject relatedJson = JsonParser.object().from(response);
            JsonArray relatedArray = relatedJson.getArray("data");
            if(relatedArray.size()== 1){
                response = getDownloader().get("https://api.bilibili.com/x/web-interface/archive/related?bvid="+ id, getHeaders()).responseBody();
                relatedJson = JsonParser.object().from(response);
                relatedArray = relatedJson.getArray("data");
                for(int i=0;i<relatedArray.size();i++){
                    collector.commit(new BilibiliRelatedInfoItemExtractor(relatedArray.getObject(i)));
                }
                return collector;
            }
            for(int i=0;i<relatedArray.size();i++){
                collector.commit(new BilibiliRelatedInfoItemExtractor(relatedArray.getObject(i), id, getThumbnailUrl(), String.valueOf(i+1)));
            }
        } catch (JsonParserException | ParsingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ReCaptchaException e) {
            e.printStackTrace();
        }
        return collector;
    }
}