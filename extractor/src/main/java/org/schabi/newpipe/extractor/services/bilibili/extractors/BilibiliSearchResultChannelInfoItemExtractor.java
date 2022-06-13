package org.schabi.newpipe.extractor.services.bilibili.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class BilibiliSearchResultChannelInfoItemExtractor implements ChannelInfoItemExtractor {
    JsonObject json = new JsonObject();
    BilibiliSearchResultChannelInfoItemExtractor(JsonObject json){
        this.json = json;
    }
    @Override
    public String getName() throws ParsingException {
        return json.getString("uname");
    }

    @Override
    public String getUrl() throws ParsingException {
        return "https://api.bilibili.com/x/space/arc/search?pn=1&ps=10&mid="  +json.getLong("mid");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return "https:"+json.getString("upic");
    }

    @Override
    public String getDescription() throws ParsingException {
        return json.getString("usign");
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        return json.getLong("fans");
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return json.getLong("videos");
    }

    @Override
    public boolean isVerified() throws ParsingException {
        return false;
    }
}