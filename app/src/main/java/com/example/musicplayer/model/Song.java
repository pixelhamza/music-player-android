package com.example.musicplayer.model;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.Serializable;

public class Song implements Serializable {
    private final long id;
    private final String title;
    private final String artist;
    private final String album;
    private final String dataPath;
    private final long duration;

    public Song(long id, String title, String artist, String album, String dataPath, long duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.dataPath = dataPath;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getDataPath() {
        return dataPath;
    }

    public long getDuration() {
        return duration;
    }

    public Uri getContentUri() {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }
}
