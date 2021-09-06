package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.audio.data.TrackData;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;

import deezer.client.DeezerClient;
import deezer.model.Playlist;
import deezer.model.Track;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInput;
import java.io.DataOutput;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class DeezerSourceManager implements AudioSourceManager {
    private static final Pattern TRACK_PATTERN = Pattern.compile("https?://.*\\.deezer\\.com/.*/track/([0-9]*)");
    private static final Pattern PLAYLIST_PATTERN = Pattern.compile("https?://.*\\.deezer\\.com/.*/playlist/([0-9]*)");
    @Getter
    private final DeezerClient deezerClient;
    @Getter
    private final AudioTrackFactory audioTrackFactory;

    public DeezerSourceManager() {
        this.deezerClient = new DeezerClient();
        this.audioTrackFactory = new AudioTrackFactory();
    }

    @Override
    public String getSourceName() {
        return "DeezerSourceManager";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (!reference.identifier.matches("(https?://)?(.*)?deezer\\.com.*")) return null;

        try {
            URL url = new URL(reference.identifier);
            if (!url.getHost().equalsIgnoreCase("www.deezer.com"))
                return null;
            String rawUrl = url.toString();
            AudioItem audioItem = null;

            if (TRACK_PATTERN.matcher(rawUrl).matches())
                audioItem = buildTrack(rawUrl);
            if (PLAYLIST_PATTERN.matcher(rawUrl).matches())
                audioItem = buildPlaylist(rawUrl);
            return audioItem;
        } catch (MalformedURLException e) {
            log.error("Failed to load the item!", e);
            return null;
        }
    }

    private AudioTrack buildTrack(String url) {
        long trackId = Long.parseLong(parseTrackPattern(url));
        Track track = deezerClient.getTrack(trackId);
        TrackData trackData = getTrackData(track);
        return audioTrackFactory.getAudioTrack(trackData);
    }

    private AudioPlaylist buildPlaylist(String url) {
        long playlistId = Long.parseLong(parsePlaylistPattern(url));
        Playlist playlist = deezerClient.getPlaylist(playlistId);
        List<Track> playlistTracks = playlist.getTracks().getData();
        List<TrackData> trackDatas = getPlaylistTrackData(playlistTracks);
        List<AudioTrack> audioTracks = audioTrackFactory.getAudioTracks(trackDatas);
        return new BasicAudioPlaylist(playlist.getTitle(), audioTracks, null, false);
    }

    private List<TrackData> getPlaylistTrackData(List<Track> playlistTracks) {
        return playlistTracks.stream()
                .map(this::getTrackData)
                .collect(Collectors.toList());
    }

    private TrackData getTrackData(Track track) {
        return new TrackData(
                track.getTitle(),
                track.getLink().toString(),
                Collections.singletonList(track.getArtist().getName()),
                track.getDuration()
        );
    }

    private String parseTrackPattern(String identifier) {
        final Matcher matcher = TRACK_PATTERN.matcher(identifier);

        if (!matcher.find())
            return "noTrackId";
        return matcher.group(1);
    }

    private String parsePlaylistPattern(String identifier) {
        final Matcher matcher = PLAYLIST_PATTERN.matcher(identifier);

        if (!matcher.find())
            return "noPlaylistId";
        return matcher.group(1);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack audioTrack) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack audioTrack, DataOutput dataOutput) {
        throw new UnsupportedOperationException("encodeTrack is unsupported.");
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo audioTrackInfo, DataInput dataInput) {
        throw new UnsupportedOperationException("decodeTrack is unsupported.");
    }

    @Override
    public void shutdown() {
    }
}
