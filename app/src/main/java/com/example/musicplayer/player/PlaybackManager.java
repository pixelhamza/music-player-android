package com.example.musicplayer.player;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import com.example.musicplayer.model.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlaybackManager {
    private static PlaybackManager instance;

    private final Context appContext;
    private final Set<PlaybackListener> listeners = new HashSet<>();
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            notifyProgress();
            progressHandler.postDelayed(this, 1000L);
        }
    };

    private MediaPlayer mediaPlayer;
    private List<Song> queue = new ArrayList<>();
    private int currentIndex = -1;

    private PlaybackManager(Context context) {
        appContext = context.getApplicationContext();
        NotificationHelper.createChannel(appContext);
    }

    public static synchronized PlaybackManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlaybackManager(context);
        }
        return instance;
    }

    public void addListener(PlaybackListener listener) {
        listeners.add(listener);
        notifyState();
        notifyProgress();
    }

    public void removeListener(PlaybackListener listener) {
        listeners.remove(listener);
    }

    public void playQueue(List<Song> songs, int startIndex) {
        if (songs == null || songs.isEmpty() || startIndex < 0 || startIndex >= songs.size()) {
            return;
        }
        queue = new ArrayList<>(songs);
        currentIndex = startIndex;
        prepareAndStart(queue.get(currentIndex));
    }

    public void togglePlayPause() {
        if (mediaPlayer == null) {
            if (!queue.isEmpty() && currentIndex >= 0) {
                prepareAndStart(queue.get(currentIndex));
            }
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notifyState();
            NotificationHelper.showPlaybackNotification(appContext, getCurrentSong(), false);
        } else {
            mediaPlayer.start();
            notifyState();
            NotificationHelper.showPlaybackNotification(appContext, getCurrentSong(), true);
        }
    }

    public void playNext() {
        if (queue.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex + 1) % queue.size();
        prepareAndStart(queue.get(currentIndex));
    }

    public void playPrevious() {
        if (queue.isEmpty()) {
            return;
        }
        currentIndex = currentIndex <= 0 ? queue.size() - 1 : currentIndex - 1;
        prepareAndStart(queue.get(currentIndex));
    }

    public void seekTo(int positionMs) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(positionMs);
            notifyProgress();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public Song getCurrentSong() {
        if (queue.isEmpty() || currentIndex < 0 || currentIndex >= queue.size()) {
            return null;
        }
        return queue.get(currentIndex);
    }

    public int getCurrentPosition() {
        return mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer == null ? 0 : mediaPlayer.getDuration();
    }

    private void prepareAndStart(Song song) {
        releasePlayer();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
        try {
            if (song.getDataPath() != null && !song.getDataPath().trim().isEmpty()) {
                mediaPlayer.setDataSource(song.getDataPath());
            } else {
                mediaPlayer.setDataSource(appContext, song.getContentUri());
            }
            mediaPlayer.setOnPreparedListener(player -> {
                player.start();
                notifyState();
                startProgressLoop();
                NotificationHelper.showPlaybackNotification(appContext, song, true);
            });
            mediaPlayer.setOnCompletionListener(player -> playNext());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        progressHandler.removeCallbacks(progressRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void startProgressLoop() {
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.post(progressRunnable);
    }

    private void notifyState() {
        Song song = getCurrentSong();
        boolean playing = isPlaying();
        for (PlaybackListener listener : listeners) {
            listener.onPlaybackChanged(song, playing);
        }
    }

    private void notifyProgress() {
        int position = getCurrentPosition();
        int duration = getDuration();
        for (PlaybackListener listener : listeners) {
            listener.onProgressChanged(position, duration);
        }
    }
}
