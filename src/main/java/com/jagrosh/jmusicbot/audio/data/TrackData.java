package com.jagrosh.jmusicbot.audio.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class TrackData {

    private final String title;
    private final String url;
    private final List<String> artists;
    private final long duration;
    private boolean local;
    private boolean explicit;

    public TrackData(String title, String url, List<String> artists, long duration) {
        this.title = title;
        this.url = url;
        this.artists = artists;
        this.duration = duration;
    }
}