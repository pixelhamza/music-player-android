package com.example.musicplayer.model;

public class Playlist {
    private final String name;
    private final String subtitle;
    private final int songCount;

    public Playlist(String name, String subtitle, int songCount) {
        this.name = name;
        this.subtitle = subtitle;
        this.songCount = songCount;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getSongCount() {
        return songCount;
    }
}
