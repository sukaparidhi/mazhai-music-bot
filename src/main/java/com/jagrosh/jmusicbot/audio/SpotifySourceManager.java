package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.BotConfig;
import com.jagrosh.jmusicbot.audio.data.AlbumData;
import com.jagrosh.jmusicbot.audio.data.ArtistData;
import com.jagrosh.jmusicbot.audio.data.PlaylistData;
import com.jagrosh.jmusicbot.audio.data.TrackData;
import com.jagrosh.jmusicbot.audio.data.keys.AlbumKey;
import com.jagrosh.jmusicbot.audio.data.keys.ArtistKey;
import com.jagrosh.jmusicbot.audio.data.keys.PlaylistKey;
import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsTopTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.hc.core5.http.ParseException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class SpotifySourceManager implements AudioSourceManager {
    private static final Pattern PLAYLIST_PATTERN = Pattern.compile("https?://.*\\.spotify\\.com/(users?/.*/)?playlists?/([^?/\\s]*)");
    private static final Pattern TRACK_PATTERN = Pattern.compile("https?://.*\\.spotify\\.com/tracks?/([^?/\\s]*)");
    private static final Pattern ALBUM_PATTERN = Pattern.compile("https?://.*\\.spotify\\.com/albums?/([^?/\\s]*)");
    private static final Pattern TOPTEN_ARTIST_PATTERN = Pattern.compile("https?://.*\\.spotify\\.com/artists?/([^?/\\s]*)");

    @Getter
    private final SpotifyApi spotifyApi;
    private final ClientCredentialsRequest clientCredentialsRequest;
    @Getter
    private final AudioTrackFactory audioTrackFactory;
    @Getter
    OkHttpClient httpClient;

    private int playlistRequestExecutionCount = 0;
    private int filteredLocalTracks = 0;

    public SpotifySourceManager(BotConfig botConfig) {
        this.httpClient = new OkHttpClient.Builder().build();
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(botConfig.getSpotifyClientId())
                .setClientSecret(botConfig.getSpotifyClientSecret())
                .build();
        this.clientCredentialsRequest = spotifyApi.clientCredentials().build();
        this.audioTrackFactory = new AudioTrackFactory();
    }

    @Override
    public String getSourceName() {
        return "SpotifySourceManager";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (reference.identifier.startsWith("ytsearch:") || reference.identifier.startsWith("scsearch:")) return null;

        try {
            URL url = new URL(reference.identifier);
            if (!url.getHost().equalsIgnoreCase("open.spotify.com"))
                return null;
            String rawUrl = url.toString().split("\\?")[0];

            AudioItem audioItem = null;
            if (TRACK_PATTERN.matcher(rawUrl).matches())
                audioItem = buildTrack(rawUrl);
            if (PLAYLIST_PATTERN.matcher(rawUrl).matches())
                audioItem = buildPlaylist(rawUrl);
            if (ALBUM_PATTERN.matcher(rawUrl).matches())
                audioItem = buildAlbum(rawUrl);
            if (TOPTEN_ARTIST_PATTERN.matcher(rawUrl).matches())
                audioItem = buildTopTenPlaylist(rawUrl);
            return audioItem;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private AudioTrack buildTrack(String url) {
        String trackId = parseTrackPattern(url);
        TrackData trackData = getTrackById(trackId);
        return this.audioTrackFactory.getAudioTrack(trackData);
    }

    private AudioPlaylist buildPlaylist(String url) {
        PlaylistKey playlistKey = parsePlaylistPattern(url);
        PlaylistData playlistData = getPlaylistById(playlistKey);
        assert playlistData != null;
        List<TrackData> trackDataList = playlistData.getTracks();
        List<AudioTrack> audioTracks = this.audioTrackFactory.getAudioTracks(trackDataList);
        return new BasicAudioPlaylist(playlistData.getName(), audioTracks, null, false);
    }

    private AudioPlaylist buildAlbum(String url) {
        AlbumKey albumKey = parseAlbumPattern(url);
        AlbumData albumData = getAlbumById(albumKey);
        assert albumData != null;
        List<TrackData> trackDataList = albumData.getTracks();
        List<AudioTrack> audioTracks = this.audioTrackFactory.getAudioTracks(trackDataList);
        return new BasicAudioPlaylist(albumData.getName(), audioTracks, null, false);
    }

    private AudioPlaylist buildTopTenPlaylist(String url) {
        ArtistKey artistKey = parseArtistPattern(url);
        ArtistData albumData = getArtistById(artistKey);
        assert albumData != null;
        List<TrackData> trackDataList = albumData.getTracks();
        List<AudioTrack> audioTracks = this.audioTrackFactory.getAudioTracks(trackDataList);
        return new BasicAudioPlaylist(albumData.getName(), audioTracks, null, false);
    }

    private TrackData getTrackById(String id) {
        final ClientCredentials clientCredentials;
        try {
            clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            GetTrackRequest trackRequest = spotifyApi.getTrack(id).build();
            Track track = trackRequest.execute();
            // [todo: add a track null check]
            String name = track.getName();
            String url = track.getUri();
            List<String> artists = new ArrayList<>();
            Arrays.stream(track.getArtists()).forEach(artist -> artists.add(artist.getName()));
            boolean local = !track.getIsExplicit();
            boolean explicit = track.getIsExplicit();
            long duration = track.getDurationMs();
            return new TrackData(
                    name,
                    url,
                    artists,
                    duration,
                    local,
                    explicit
            );

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private PlaylistData getPlaylistById(PlaylistKey playlistKey) {
        final ClientCredentials clientCredentials;
        try {
            clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            GetPlaylistRequest playlistRequest = spotifyApi.getPlaylist(playlistKey.getPlaylistId()).build();
            Playlist playlist = playlistRequest.execute();
            // [todo: add a playlist null check]
            String name = playlist.getName();
            String url = playlist.getUri();
            String owner = playlist.getOwner().getDisplayName();
            List<TrackData> tracks = new ArrayList<>();

            Arrays.stream(playlist.getTracks().getItems()).forEach(track -> tracks.add(new TrackData(track.getTrack().getName(), track.getTrack().getUri(),
                    Collections.singletonList(""),track.getTrack().getDurationMs()
            )));

            return new PlaylistData(
                    name,
                    url,
                    owner,
                    tracks
            );
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private AlbumData getAlbumById(AlbumKey albumKey) {
        final ClientCredentials clientCredentials;
        try {
            clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            GetAlbumRequest albumsTracksRequest = spotifyApi.getAlbum(albumKey.getAlbumId()).build();
            Album album = albumsTracksRequest.execute();
            // [todo: add a album null check]
            String name = album.getName();
            String url = album.getUri();
            List<String> artists = new ArrayList<>();
            Arrays.stream(album.getArtists()).forEach(x -> artists.add(x.getName()));
            List<TrackData> tracks = new ArrayList<>();
            Arrays.stream(album.getTracks().getItems()).forEach(x -> tracks.add(new TrackData(x.getName(), x.getUri(), Arrays.stream(x.getArtists()).map(ArtistSimplified::getName).collect(Collectors.toList()), x.getDurationMs())));
            return new AlbumData(
                    name,
                    artists,
                    url,
                    tracks
            );

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArtistData getArtistById(ArtistKey artistKey) {
        final ClientCredentials clientCredentials;
        try {
            clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            GetArtistRequest artistRequest = spotifyApi.getArtist(artistKey.getArtistId()).build();
            Artist artist = artistRequest.execute();
            GetArtistsTopTracksRequest artistsTopTracksRequest = spotifyApi.getArtistsTopTracks(artistKey.getArtistId(), CountryCode.IN).build();
            Track[] artistTracks = artistsTopTracksRequest.execute();
            // [todo: add a album null check]
            String name = artist.getName();
            String url = artist.getUri();
            List<TrackData> tracks = new ArrayList<>();
            Arrays.stream(artistTracks).forEach(artistTrack -> tracks.add(
                    new TrackData(
                            artistTrack.getName(),
                            artistTrack.getUri(),
                            Arrays.stream(artistTrack.getArtists()).map(ArtistSimplified::getName).collect(Collectors.toList()),
                            artistTrack.getDurationMs(),
                            !artistTrack.getIsExplicit(),
                            artistTrack.getIsExplicit()
                    )
            ));

            return new ArtistData(
                    name,
                    url,
                    tracks
            );

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String parseTrackPattern(String identifier) {
        final Matcher matcher = TRACK_PATTERN.matcher(identifier);
        if (!matcher.find())
            return "noTrackId";
        return matcher.group(1);
    }

    private PlaylistKey parsePlaylistPattern(String identifier) {
        final Matcher matcher = PLAYLIST_PATTERN.matcher(identifier);
        if (!matcher.find())
            return new PlaylistKey("noPlaylistId");

        log.info("playlistId: {}", matcher.group(2));
        return new PlaylistKey(matcher.group(2));
    }

    private AlbumKey parseAlbumPattern(String identifier) {
        final Matcher matcher = ALBUM_PATTERN.matcher(identifier);
        if (!matcher.find())
            return new AlbumKey("noAlbumId");
        String userId = matcher.group(1);
        return new AlbumKey(userId);
    }

    private ArtistKey parseArtistPattern(String identifier) {
        final Matcher matcher = TOPTEN_ARTIST_PATTERN.matcher(identifier);
        if (!matcher.find())
            return new ArtistKey("noArtistId");
        String userId = matcher.group(1);
        return new ArtistKey(userId);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        throw new UnsupportedOperationException("encodeTrack is unsupported.");
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        throw new UnsupportedOperationException("decodeTrack is unsupported.");
    }

    @Override
    public void shutdown() {
    }
}
