package com.example.musicplayer.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.musicplayer.model.Playlist;
import com.example.musicplayer.model.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlaylistManager {
    private static final String PREFS = "musebox_playlists";
    private static final String KEY_NAMES = "playlist_names";
    private static final String PLAYLIST_PREFIX = "playlist_";

    private final SharedPreferences preferences;

    public PlaylistManager(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean createPlaylist(String name) {
        Set<String> names = getPlaylistNameSet();
        if (names.contains(name)) {
            return false;
        }
        names.add(name);
        preferences.edit()
                .putStringSet(KEY_NAMES, names)
                .putStringSet(getPlaylistKey(name), new HashSet<>())
                .apply();
        return true;
    }

    public void addSongToPlaylist(String playlistName, Song song) {
        Set<String> ids = getPlaylistSongIdSet(playlistName);
        ids.add(String.valueOf(song.getId()));
        preferences.edit().putStringSet(getPlaylistKey(playlistName), ids).apply();
    }

    public void addSongsToPlaylist(String playlistName, List<Song> songs) {
        Set<String> ids = getPlaylistSongIdSet(playlistName);
        for (Song song : songs) {
            ids.add(String.valueOf(song.getId()));
        }
        preferences.edit().putStringSet(getPlaylistKey(playlistName), ids).apply();
    }

    public void removeSongFromPlaylist(String playlistName, Song song) {
        Set<String> ids = getPlaylistSongIdSet(playlistName);
        ids.remove(String.valueOf(song.getId()));
        preferences.edit().putStringSet(getPlaylistKey(playlistName), ids).apply();
    }

    public void deletePlaylist(String playlistName) {
        Set<String> names = getPlaylistNameSet();
        if (!names.remove(playlistName)) {
            return;
        }
        preferences.edit()
                .putStringSet(KEY_NAMES, names)
                .remove(getPlaylistKey(playlistName))
                .apply();
    }

    public List<Playlist> getPlaylists(List<Song> library) {
        List<Playlist> playlists = new ArrayList<>();
        for (String name : getPlaylistNameSet()) {
            int size = getSongsForPlaylist(name, library).size();
            playlists.add(new Playlist(name, "Curated inside MuseBox", size));
        }
        return playlists;
    }

    public List<String> getPlaylistNames() {
        return new ArrayList<>(getPlaylistNameSet());
    }

    public List<Song> getSongsForPlaylist(String playlistName, List<Song> library) {
        List<Song> songs = new ArrayList<>();
        Set<String> ids = getPlaylistSongIdSet(playlistName);
        for (Song song : library) {
            if (ids.contains(String.valueOf(song.getId()))) {
                songs.add(song);
            }
        }
        return songs;
    }

    public boolean hasPlaylist(String playlistName) {
        return getPlaylistNameSet().contains(playlistName);
    }

    private String getPlaylistKey(String name) {
        return PLAYLIST_PREFIX + name;
    }

    private Set<String> getPlaylistNameSet() {
        Set<String> set = preferences.getStringSet(KEY_NAMES, new HashSet<>());
        return set == null ? new HashSet<>() : new HashSet<>(set);
    }

    private Set<String> getPlaylistSongIdSet(String playlistName) {
        Set<String> set = preferences.getStringSet(getPlaylistKey(playlistName), new HashSet<>());
        return set == null ? new HashSet<>() : new HashSet<>(set);
    }
}
