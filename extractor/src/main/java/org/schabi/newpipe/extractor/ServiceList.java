package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.services.bandcamp.BandcampService;
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService;
import org.schabi.newpipe.extractor.services.peertube.PeertubeService;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService;
import org.schabi.newpipe.extractor.services.youtube.YoutubeService;
import org.schabi.newpipe.extractor.services.bilibili.BilibiliService;
import org.schabi.newpipe.extractor.services.niconico.NiconicoService;
import org.schabi.newpipe.extractor.services.dailymotion.DailymotionService;
import org.schabi.newpipe.extractor.services.rumble.RumbleService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A list of supported services.
 */
@SuppressWarnings({"ConstantName", "InnerAssignment"}) // keep unusual names and inner assignments
public final class ServiceList {
    private ServiceList() {
        //no instance
    }

    public static final YoutubeService YouTube;
    public static final SoundcloudService SoundCloud;
    public static final MediaCCCService MediaCCC;
    public static final PeertubeService PeerTube;
    public static final BandcampService Bandcamp;
    public static final BilibiliService Bilibili;
    public static final NiconicoService Niconico;
    //public static final DailymotionService Dailymotion;
    public static final RumbleService Rumble;

    /**
     * When creating a new service, put this service in the end of this list,
     * and give it the next free id.
     */
    private static final List<StreamingService> SERVICES = Collections.unmodifiableList(
            Arrays.asList(
                    YouTube = new YoutubeService(0),
                    SoundCloud = new SoundcloudService(1),
                    MediaCCC = new MediaCCCService(2),
                    PeerTube = new PeertubeService(3),
                    Bandcamp = new BandcampService(4),
                    Bilibili = new BilibiliService(5),
                    Niconico = new NiconicoService(6),
                    Rumble = new RumbleService(7)
                    //Dailymotion = new DailymotionService(8)
            ));

    /**
     * Get all the supported services.
     *
     * @return a unmodifiable list of all the supported services
     */
    public static List<StreamingService> all() {
        return SERVICES;
    }
}
