package org.schabi.newpipe.extractor.services.rumble;

import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.schabi.newpipe.extractor.ServiceList.Rumble;

public final class RumbleParsingHelper {

    private RumbleParsingHelper() {
    }

    public static int parseDurationString(final String input) throws ParsingException {
        return parseDurationString(input, "(d|h|m|s)");
    }

    public static int parseDurationString(final String input, final String split)
            throws ParsingException, NumberFormatException {

        final String[] splitInput = input.split(split);
        String days = "0";
        String hours = "0";
        String minutes = "0";
        final String seconds;

        switch (splitInput.length) {
            case 4:
                days = splitInput[0];
                hours = splitInput[1];
                minutes = splitInput[2];
                seconds = splitInput[3];
                break;
            case 3:
                hours = splitInput[0];
                minutes = splitInput[1];
                seconds = splitInput[2];
                break;
            case 2:
                minutes = splitInput[0];
                seconds = splitInput[1];
                break;
            case 1:
                seconds = splitInput[0];
                break;
            default:
                throw new ParsingException("Error duration string with unknown format: " + input);
        }

        return ((Integer.parseInt(Utils.removeNonDigitCharacters(days)) * 24
                + Integer.parseInt(Utils.removeNonDigitCharacters(hours))) * 60
                + Integer.parseInt(Utils.removeNonDigitCharacters(minutes))) * 60
                + Integer.parseInt(Utils.removeNonDigitCharacters(seconds));
    }

    /**
     * @param shouldThrowOnError if true a ParsingException is thrown on error
     * @param msg                in case of Exception the error message that is passed
     * @param function           the function that extract the desired string
     * @return the extracted string or null if shouldThrowOnError is set to false
     * @throws ParsingException
     */
    public static String extractSafely(final boolean shouldThrowOnError, final String msg,
                                       final ExtractFunction function) throws ParsingException {
        String retValue = null;
        try {
            retValue = function.run();
        } catch (final Exception e) {
            if (shouldThrowOnError) {
                throw new ParsingException(msg + ": " + e);
            }
        }
        return retValue;
    }

    /**
     * interface for {@link #extractSafely} extractor function
     */
    public interface ExtractFunction {
        String run();
    }

    public static String totalMessMethodToGetUploaderThumbnailUrl(final String classStr,
                                                                  final Document doc)
            throws ParsingException {
        return extractThumbnail(doc, classStr,
                () -> {
                    // extract checksum to use as identifier
                    final Pattern matchChecksum = Pattern.compile("([a-fA-F0-9]{32})");
                    final Matcher match2 = matchChecksum.matcher(classStr);
                    if (match2.find()) {
                        final String chkSum = match2.group(1);
                        return chkSum;
                    } else {
                        return null;
                    }
                });
    }

    /**
     * TODO implement a faster/easier way to achive same goals
     *
     * @param classStr
     * @return null if there was a letter and not a image, xor url with the uploader thumbnail
     * @throws ParsingException
     */
    public static String extractThumbnail(final Document document,
                                          final String classStr,
                                          final ExtractFunction function) throws ParsingException {

        if (classStr.contains("user-image--letter")) {
            return null;
        }

        final String thumbIdentifier = function.run();
        if (thumbIdentifier == null) {
            return null;
        }

        final String matchThat = document.toString();
        final int pos = matchThat.indexOf(thumbIdentifier);
        final String preciselyMatchHere = matchThat.substring(pos);

        final Pattern channelThumbUrl =
                Pattern.compile("\\W+background-image:\\W+url(?:\\()([^)]*)(?:\\));");
        final Matcher match = channelThumbUrl.matcher(preciselyMatchHere);
        if (match.find()) {
            return match.group(1);
        }
        throw new ParsingException("Could not extract thumbUrl: " + thumbIdentifier);
    }

    public static String moreTotalMessMethodToGenerateUploaderUrl(final String classStr,
                                                                  final Document doc,
                                                                  final String uploaderName)
            throws ParsingException, MalformedURLException {

        final String thumbnailUrl = totalMessMethodToGetUploaderThumbnailUrl(classStr, doc);
        if (thumbnailUrl == null) {
            final String uploaderUrl = Rumble.getBaseUrl() + "/user/" + uploaderName;
            return uploaderUrl;
        }

        final URL url = Utils.stringToURL(thumbnailUrl);
        if (!url.getAuthority().contains("rmbl.ws")) {
            final String uploaderUrl = Rumble.getBaseUrl() + "/user/" + uploaderName;
            return uploaderUrl;
        }

        final String path = thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1);
        final String[] splitPath = path.split("-", 0);
        final String theUploader = splitPath[1];

        final String uploaderUrl = Rumble.getBaseUrl() + "/user/" + theUploader;
        return uploaderUrl;
    }
}