package com.jagrosh.jmusicbot.audio;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequest;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.jagrosh.jmusicbot.Bot;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
public class YoutubeUtil {

    private final YouTube client;

    private YoutubeUtil(Bot bot) throws GeneralSecurityException, IOException {
        this.client = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                httpRequest -> {
                    // Do not so much
                })
                .setApplicationName("JMusicBot-discord")
                .setYouTubeRequestInitializer(new RequestInitializer(bot.getConfig().getYoutubeAccessToken()))
                .build();
    }

    /**
     * Construcs a new Youtube util instance
     *
     * @param bot the current GroovyBot instance
     * @return a new YoutubeUtil instance
     */
    public static YoutubeUtil create(Bot bot) {
        try {
            return new YoutubeUtil(bot);
        } catch (GeneralSecurityException | IOException e) {
            log.error("[YouTube] Error while establishing connection to YouTube", e);
        }
        return null;
    }

    /**
     * Retrieves the next video for the autoplay function
     *
     * @param videoId the ID of the prevoius video
     * @return The new videos SearchResult
     * @throws IOException          when an IO error occurred
     * @throws NullPointerException When no video where found
     */
    public SearchResult retrieveRelatedVideos(String videoId) throws IOException, NullPointerException {
        YouTube.Search.List search = client.search().list("id,snippet")
                .setRelatedToVideoId(videoId)
                .setType("video")
                .setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
                .setMaxResults(1L);
        SearchListResponse searchResults = search.execute();
        if (searchResults.getItems().isEmpty())
            throw new NullPointerException("No videos were found");
        return searchResults.getItems().get(0);
    }

    public String getVideoId(String query) throws IOException {
        YouTube.Search.List search = client.search().list("id,snippet")
                .setType("video")
                .setFields("items(id/videoId)")
                .setQ(query);
        SearchListResponse response = search.execute();
        if (response.getItems().isEmpty())
            return "";
        return response.getItems().get(0).getId().getVideoId();
    }

    /**
     * Search for youtube Videos by it's ide
     *
     * @param videoId The id of the video
     * @return an VideoListResponse {@link com.google.api.services.youtube.model.VideoListResponse}
     * @throws IOException When YoutubeRequest returns an error
     */
    public VideoListResponse getVideoById(String videoId) throws IOException {
        return client.videos().list("snippet,localizations,contentDetails").setId(videoId).execute();
    }

    /**
     * Gets the first video from an VideoListResponse
     *
     * @param videoId The yotube video id
     * @return The first Video {@link com.google.api.services.youtube.model.Video} of the {@link com.google.api.services.youtube.model.VideoListResponse}
     * @throws IOException When YoutubeRequest returns an error
     * @see YoutubeUtil#getVideoById(String)
     */
    public Video getFirstVideoById(String videoId) throws IOException {
        return getVideoById(videoId).getItems().get(0);
    }

    private static class RequestInitializer extends YouTubeRequestInitializer {
        private String accessToken;
        public RequestInitializer(String accessToken){
            this.accessToken = accessToken;
        }
        @Override
        protected void initializeYouTubeRequest(YouTubeRequest<?> youTubeRequest) {
            youTubeRequest.setKey(accessToken);
        }
    }
}
