package com.example.musicplayer.player;

import com.example.musicplayer.model.Song;

public interface PlaybackListener {
    void onPlaybackChanged(Song currentSong, boolean isPlaying);

    void onProgressChanged(int positionMs, int durationMs);
}
