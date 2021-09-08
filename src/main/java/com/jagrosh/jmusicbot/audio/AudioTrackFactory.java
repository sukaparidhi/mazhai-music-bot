package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.audio.data.TrackData;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AudioTrackFactory {

    public List<AudioTrack> getAudioTracks(List<TrackData> trackDataList) {
        return trackDataList.stream()
                .map(this::getAudioTrack)
                .collect(Collectors.toList());
    }

    public AudioTrack getAudioTrack(TrackData trackData) {
        final YoutubeSearchProvider searchProvider = new YoutubeSearchProvider();
        String identifier = null;
        try {
            final StringBuilder query = new StringBuilder();
            query.append(trackData.getArtists().isEmpty() ? "" : trackData.getArtists().get(0));
            query.append(trackData.getTitle());

            final AudioItem rawResult =  searchProvider.loadSearchResult(query.toString(),
                    audioTrackInfo -> new YoutubeAudioTrack(audioTrackInfo, new YoutubeAudioSourceManager()));
            if (rawResult instanceof AudioPlaylist) {
                identifier = ((AudioPlaylist) rawResult).getTracks().get(0).getIdentifier();
            }
            AudioTrackInfo audioTrackInfo = new AudioTrackInfo(
                    trackData.getTitle(),
                    trackData.getArtists().get(0),
                    trackData.getDuration(),
                    identifier,
                    false,
                    trackData.getUrl()
            );

        return new YoutubeAudioTrack(audioTrackInfo, new YoutubeAudioSourceManager());
        } catch (Exception e){
            log.info("{}", e);
            return null;
        }
    }
}
