package com.example.musicplayer.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.musicplayer.model.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesManager {
    private static final String PREFS = "musebox_favorites";
    private static final String KEY_FAVORITES = "favorite_song_ids";

    private final SharedPreferences preferences;

    public FavoritesManager(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isFavorite(long songId) {
        return getFavoriteIdSet().contains(String.valueOf(songId));
    }

    public boolean toggleFavorite(Song song) {
        Set<String> favorites = new HashSet<>(getFavoriteIdSet());
        String songId = String.valueOf(song.getId());
        boolean isFavorite;
        if (favorites.contains(songId)) {
            favorites.remove(songId);
            isFavorite = false;
        } else {
            favorites.add(songId);
            isFavorite = true;
        }
        preferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
        return isFavorite;
    }

    public int getFavoriteCount() {
        return getFavoriteIdSet().size();
    }

    public List<Song> getFavoriteSongs(List<Song> library) {
        List<Song> result = new ArrayList<>();
        Set<String> favorites = getFavoriteIdSet();
        for (Song song : library) {
            if (favorites.contains(String.valueOf(song.getId()))) {
                result.add(song);
            }
        }
        return result;
    }

    private Set<String> getFavoriteIdSet() {
        Set<String> set = preferences.getStringSet(KEY_FAVORITES, new HashSet<>());
        return set == null ? new HashSet<>() : new HashSet<>(set);
    }
}
