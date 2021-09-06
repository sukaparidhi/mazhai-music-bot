package com.jagrosh.jmusicbot.audio;


//import com.jagrosh.jmusicbot.audio.data.TrackData;
//import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
//import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
//import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
//import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.extern.slf4j.Slf4j;


//import java.io.IOException;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;

@Slf4j
public class AudioTrackFactory {
//
//    public List<AudioTrack> getAudioTracks(List<TrackData> trackDataList) {
//        return trackDataList.stream()
//                .map(this::getAudioTrack)
//                .collect(Collectors.toList());
//    }
//
//    public AudioTrack getAudioTrack(TrackData trackData) {
//        // GroovyBot.getInstance().getYoutubeClient().getVideoId(trackData.getArtists().get(0) + " " + trackData.getTitle());
//        AudioTrackInfo audioTrackInfo = null;
//        try {
//            String identifier = "w9L23p4PhwQ";
//            audioTrackInfo = new AudioTrackInfo(
//                    trackData.getTitle(),
//                    trackData.getArtists().get(0),
//                    trackData.getDuration(),
//                    identifier, false,
//                    trackData.getUrl()
//            );
//        } catch (Exception e){
//            log.info("{}", e);
//        }
//        return new YoutubeAudioTrack(audioTrackInfo, new YoutubeAudioSourceManager());
//    }
}
