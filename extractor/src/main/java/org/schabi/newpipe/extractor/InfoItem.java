package org.schabi.newpipe.extractor;

import java.io.Serializable;

public abstract class InfoItem implements Serializable {
    private final InfoType infoType;
    private final int serviceId;
    private final String url;
    private final String name;
    private String thumbnailUrl;

    public InfoItem(final InfoType infoType,
                    final int serviceId,
                    final String url,
                    final String name) {
        this.infoType = infoType;
        this.serviceId = serviceId;
        this.url = url;
        this.name = name;
    }

    public InfoType getInfoType() {
        return infoType;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public void setThumbnailUrl(final String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[url=\"" + url + "\", name=\"" + name + "\"]";
    }

    public enum InfoType {
        STREAM,
        PLAYLIST,
        CHANNEL,
        COMMENT
    }
}
