package com.jagrosh.jmusicbot.audio;

//import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
//import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
//import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
//import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
//import com.sedmelluq.discord.lavaplayer.track.*;
//import lombok.Getter;
//import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.jetbrains.annotations.NotNull;
//import org.json.JSONObject;
//
//import java.io.DataInput;
//import java.io.DataOutput;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.function.Consumer;
//import java.util.function.Function;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

@Slf4j
public class SpotifySourceManager { //implements AudioSourceManager {
//
//    private static final Pattern PLAYLIST_PATTERN = Pattern.compile("https?://.*\\.spotify\\.com/(users?/.*/)?playlists?/([^?/\\s]*)");
//    private static final Pattern TRACK_PATTERN = Pattern.compile("https?://.*\\.spotify\\.com/tracks?/([^?/\\s]*)");
//    private static final Pattern ALBUM_PATTERN = Pattern.compile("https?://.*\\.spotify\\.com/albums?/([^?/\\s]*)");
//    private static final Pattern TOPTEN_ARTIST_PATTERN = Pattern.compile("https?://.*\\.spotify\\.com/artists?/([^?/\\s]*)");
//    private static final String SERVICE_BASE_URL = GroovyBot.getInstance().getConfig().getJSONObject("kereru").getString("host");
//
//    @Getter
//    private final OkHttpClient httpClient;
//    @Getter
//    private final AudioTrackFactory audioTrackFactory;
//    @Getter
//    @Setter
//    private MusicPlayer player;
//
//    private int playlistRequestExecutionCount = 0;
//    private int filteredLocalTracks = 0;
//
//    public SpotifySourceManager() {
//        this.httpClient = new OkHttpClient.Builder().build();
//        this.audioTrackFactory = new AudioTrackFactory();
//    }
//
//    @Override
//    public String getSourceName() {
//        return "SpotifySourceManager";
//    }
//
//    @Override
//    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
//        if (reference.identifier.startsWith("ytsearch:") || reference.identifier.startsWith("scsearch:")) return null;
//
//        try {
//            URL url = new URL(reference.identifier);
//            if (!url.getHost().equalsIgnoreCase("open.spotify.com"))
//                return null;
//            String rawUrl = url.toString();
//
//            AudioItem audioItem = null;
//            if (TRACK_PATTERN.matcher(rawUrl).matches())
//                audioItem = buildTrack(rawUrl);
//            if (PLAYLIST_PATTERN.matcher(rawUrl).matches())
//                audioItem = buildPlaylist(rawUrl);
//            if (ALBUM_PATTERN.matcher(rawUrl).matches())
//                audioItem = buildAlbum(rawUrl);
//            if (TOPTEN_ARTIST_PATTERN.matcher(rawUrl).matches())
//                audioItem = buildTopTenPlaylist(rawUrl);
//            return audioItem;
//        } catch (MalformedURLException e) {
//            log.error("Failed to load the item!", e);
//            return null;
//        }
//    }
//
//    private AudioTrack buildTrack(String url) {
//        String trackId = parseTrackPattern(url);
//
//        JSONObject jsonTrack = getTrackById(trackId);
//        TrackData trackData = getTrackData(Objects.requireNonNull(jsonTrack));
//        return this.audioTrackFactory.getAudioTrack(trackData);
//    }
//
//    private AudioPlaylist buildPlaylist(String url) {
//        PlaylistKey playlistKey = parsePlaylistPattern(url);
//
//        JSONObject jsonPlaylist = getPlaylistById(playlistKey);
//        PlaylistData playlistData = getPlaylistData(Objects.requireNonNull(jsonPlaylist));
//        List<TrackData> trackDataList = playlistData.getTracks();
//        List<AudioTrack> audioTracks = this.audioTrackFactory.getAudioTracks(trackDataList);
//        return new BasicAudioPlaylist(playlistData.getName(), audioTracks, null, false);
//    }
//
//    private AudioPlaylist buildAlbum(String url) {
//        AlbumKey albumKey = parseAlbumPattern(url);
//
//        JSONObject jsonAlbum = getAlbumById(albumKey);
//        AlbumData albumData = getAlbumData(Objects.requireNonNull(jsonAlbum));
//        List<TrackData> trackDataList = albumData.getTracks();
//        List<AudioTrack> audioTracks = this.audioTrackFactory.getAudioTracks(trackDataList);
//        return new BasicAudioPlaylist(albumData.getName(), audioTracks, null, false);
//    }
//
//    private AudioPlaylist buildTopTenPlaylist(String url) {
//        ArtistKey artistKey = parseArtistPattern(url);
//
//        JSONObject jsonArtist = getArtistById(artistKey);
//        ArtistData albumData = getArtistData(Objects.requireNonNull(jsonArtist));
//        List<TrackData> trackDataList = albumData.getTracks();
//        List<AudioTrack> audioTracks = this.audioTrackFactory.getAudioTracks(trackDataList);
//        return new BasicAudioPlaylist(albumData.getName(), audioTracks, null, false);
//    }
//
//    private TrackData getTrackData(@NotNull JSONObject jsonObject) {
//        JSONObject dataObject = jsonObject.has("data") ? jsonObject.getJSONObject("data") : jsonObject;
//        String name = dataObject.getString("name"),
//                url = dataObject.getString("url");
//        List<String> artists = new ArrayList<>();
//        dataObject.getJSONArray("artists").forEach(o -> {
//            JSONObject jsonArtist = (JSONObject) o;
//            artists.add(jsonArtist.getString("name"));
//        });
//        boolean local = dataObject.getBoolean("local"),
//                explicit = dataObject.getBoolean("explicit");
//        long duration = dataObject.getLong("durationTimeMillis");
//        return new TrackData(
//                name,
//                url,
//                artists,
//                duration,
//                local,
//                explicit
//        );
//    }
//
//    private PlaylistData getPlaylistData(@NotNull JSONObject jsonObject) {
//        JSONObject dataObject = jsonObject.has("data") ? jsonObject.getJSONObject("data") : jsonObject;
//        String name = dataObject.getString("name"),
//                url = dataObject.getString("url"),
//                owner = dataObject.getString("owner");
//        List<TrackData> tracks = new ArrayList<>();
//        dataObject.getJSONArray("tracks").forEach(o -> {
//            JSONObject jsonTrack = (JSONObject) o;
//            tracks.add(getTrackData(jsonTrack));
//        });
//        return new PlaylistData(
//                name,
//                url,
//                owner,
//                tracks
//        );
//    }
//
//    private AlbumData getAlbumData(@NotNull JSONObject jsonObject) {
//        JSONObject dataObject = jsonObject.has("data") ? jsonObject.getJSONObject("data") : jsonObject;
//        String name = dataObject.getString("name"),
//                url = dataObject.getString("url");
//        List<String> artists = new ArrayList<>();
//        dataObject.getJSONArray("artists").forEach(o -> {
//            JSONObject jsonArtist = (JSONObject) o;
//            artists.add(jsonArtist.getString("name"));
//        });
//        List<TrackData> tracks = new ArrayList<>();
//        dataObject.getJSONArray("tracks").forEach(o -> {
//            JSONObject jsonTrack = (JSONObject) o;
//            tracks.add(getTrackData(jsonTrack));
//        });
//        return new AlbumData(
//                name,
//                artists,
//                url,
//                tracks
//        );
//    }
//
//    private ArtistData getArtistData(@NotNull JSONObject jsonObject) {
//        JSONObject dataObject = jsonObject.has("data") ? jsonObject.getJSONObject("data") : jsonObject;
//        String name = dataObject.getString("name"),
//                url = dataObject.getString("url");
//        List<TrackData> tracks = new ArrayList<>();
//        dataObject.getJSONArray("topTracks").forEach(o -> {
//            JSONObject jsonTrack = (JSONObject) o;
//            tracks.add(getTrackData(jsonTrack));
//        });
//        return new ArtistData(
//                name,
//                url,
//                tracks
//        );
//    }
//
//    private JSONObject getTrackById(String id) {
//        JSONObject jsonObject = null;
//        Request request = new Request.Builder()
//                .url(SERVICE_BASE_URL + "/tracks/" + id)
//                .get()
//                .build();
//        try (Response response = this.httpClient.newCall(request).execute()) {
//            if (response.body() != null) {
//                jsonObject = new JSONObject(response.body().string());
//            }
//        } catch (IOException e) {
//            log.error("An error occurred while executing a GET request for looking up an track", e);
//            return null;
//        }
//        return jsonObject;
//    }
//
//    private JSONObject getPlaylistById(PlaylistKey playlistKey) {
//        JSONObject jsonObject = null;
//        Request request = new Request.Builder()
//                .url(SERVICE_BASE_URL + "/playlists/" + playlistKey.getPlaylistId())
//                .get()
//                .build();
//        try (Response response = this.httpClient.newCall(request).execute()) {
//            if (response.body() != null) {
//                jsonObject = new JSONObject(response.body().string());
//            }
//        } catch (IOException e) {
//            log.error("An error occurred while executing a GET request for looking up an playlist", e);
//            return null;
//        }
//        return jsonObject;
//    }
//
//    private JSONObject getAlbumById(AlbumKey albumKey) {
//        JSONObject jsonObject = null;
//        Request request = new Request.Builder()
//                .url(SERVICE_BASE_URL + "/albums/" + albumKey.getAlbumId())
//                .get()
//                .build();
//        try (Response response = this.httpClient.newCall(request).execute()) {
//            if (response.body() != null) {
//                jsonObject = new JSONObject(response.body().string());
//            }
//        } catch (IOException e) {
//            log.error("An error occurred while executing a GET request for looking up an album", e);
//            return null;
//        }
//        return jsonObject;
//    }
//
//    private JSONObject getArtistById(ArtistKey artistKey) {
//        JSONObject jsonObject = null;
//        Request request = new Request.Builder()
//                .url(SERVICE_BASE_URL + "/artists/" + artistKey.getArtistId())
//                .get()
//                .build();
//        try (Response response = this.httpClient.newCall(request).execute()) {
//            if (response.body() != null) {
//                jsonObject = new JSONObject(response.body().string());
//            }
//        } catch (IOException e) {
//            log.error("An error occurred while executing a GET request for looking up an artist", e);
//            return null;
//        }
//        return jsonObject;
//    }
//
//    private String parseTrackPattern(String identifier) {
//        final Matcher matcher = TRACK_PATTERN.matcher(identifier);
//        if (!matcher.find())
//            return "noTrackId";
//        return matcher.group(1);
//    }
//
//    private PlaylistKey parsePlaylistPattern(String identifier) {
//        final Matcher matcher = PLAYLIST_PATTERN.matcher(identifier);
//        if (!matcher.find())
//            return new PlaylistKey("noPlaylistId");
//        return new PlaylistKey(matcher.group(2));
//    }
//
//    private AlbumKey parseAlbumPattern(String identifier) {
//        final Matcher matcher = ALBUM_PATTERN.matcher(identifier);
//        if (!matcher.find())
//            return new AlbumKey("noAlbumId");
//        String userId = matcher.group(1);
//        return new AlbumKey(userId);
//    }
//
//    private ArtistKey parseArtistPattern(String identifier) {
//        final Matcher matcher = TOPTEN_ARTIST_PATTERN.matcher(identifier);
//        if (!matcher.find())
//            return new ArtistKey("noArtistId");
//        String userId = matcher.group(1);
//        return new ArtistKey(userId);
//    }
//
//    @Override
//    public boolean isTrackEncodable(AudioTrack track) {
//        return false;
//    }
//
//    @Override
//    public void encodeTrack(AudioTrack track, DataOutput output) {
//        throw new UnsupportedOperationException("encodeTrack is unsupported.");
//    }
//
//    @Override
//    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
//        throw new UnsupportedOperationException("decodeTrack is unsupported.");
//    }
//
//    @Override
//    public void shutdown() {
//    }
}
