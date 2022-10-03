package org.schabi.newpipe.extractor.services.bilibili.extractors;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

public class BilibiliChannelInfoItemExtractor implements StreamInfoItemExtractor{

    protected final JsonObject item;
    public String name;
    public String face;
    public BilibiliChannelInfoItemExtractor(final JsonObject json, String name, String face) {
        item = json;
        this.name = name;
        this.face = face;
    }
    @Override
    public String getName() throws ParsingException {
        return item.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return "https://bilibili.com/"+item.getString("bvid");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return item.getString("pic").replace("http:", "https:");
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
        if(item.getLong("duration") != 0){
            return item.getLong("duration");
        }
        String duration = item.getString("length");
        long result = 0;
        int len = duration.split(":").length;
        try {
            result += Integer.parseInt(duration.split(":")[len-1]);
            result += Integer.parseInt(duration.split(":")[len-2]) * 60;
            result += Integer.parseInt(duration.split(":")[len-3]) * 3600;


        } catch (Exception e){
            e.printStackTrace();
        }
        return  result;
    }

    @Override
    public long getViewCount() throws ParsingException {
        return Optional.of(item.getLong("play")).orElse(item.getObject("stat").getLong("view"));
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return name;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return null;
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return face;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        if(item.getInt("created") == 0){
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((item.getInt("pubdate") )*1000L));
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((item.getInt("created") )*1000L));
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(LocalDateTime.parse(
                getTextualUploadDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atOffset(ZoneOffset.ofHours(+8)));
    }

}