package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.data.FavoritesManager;
import com.example.musicplayer.data.PlaylistManager;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.player.PlaybackListener;
import com.example.musicplayer.player.PlaybackManager;
import com.example.musicplayer.util.UiUtils;

import java.util.List;

public class PlayerActivity extends AppCompatActivity implements PlaybackListener {
    private PlaybackManager playbackManager;
    private FavoritesManager favoritesManager;
    private PlaylistManager playlistManager;

    private ImageView imgArtwork;
    private TextView tvSongTitle;
    private TextView tvSongArtist;
    private TextView tvCurrentTime;
    private TextView tvDuration;
    private SeekBar seekBar;
    private ImageButton btnFavorite;
    private ImageButton btnPlayPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playbackManager = PlaybackManager.getInstance(this);
        favoritesManager = new FavoritesManager(this);
        playlistManager = new PlaylistManager(this);

        bindViews();
        setupActions();
        render(playbackManager.getCurrentSong());
    }

    @Override
    protected void onResume() {
        super.onResume();
        playbackManager.addListener(this);
        render(playbackManager.getCurrentSong());
        updateProgress(playbackManager.getCurrentPosition(), playbackManager.getDuration());
    }

    @Override
    protected void onPause() {
        super.onPause();
        playbackManager.removeListener(this);
    }

    private void bindViews() {
        imgArtwork = findViewById(R.id.imgArtwork);
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvSongArtist = findViewById(R.id.tvSongArtist);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvDuration = findViewById(R.id.tvDuration);
        seekBar = findViewById(R.id.seekBar);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnPlayPause = findViewById(R.id.btnPlayPause);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnPrevious).setOnClickListener(v -> playbackManager.playPrevious());
        findViewById(R.id.btnNext).setOnClickListener(v -> playbackManager.playNext());
        btnPlayPause.setOnClickListener(v -> {
            playbackManager.togglePlayPause();
            render(playbackManager.getCurrentSong());
        });
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        findViewById(R.id.btnAddToPlaylist).setOnClickListener(v -> showPlaylistChooser());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(UiUtils.formatDuration(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playbackManager.seekTo(seekBar.getProgress());
            }
        });
    }

    private void toggleFavorite() {
        Song currentSong = playbackManager.getCurrentSong();
        if (currentSong == null) {
            return;
        }
        boolean favorite = favoritesManager.toggleFavorite(currentSong);
        btnFavorite.setImageResource(
                favorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
        );
        Toast.makeText(this,
                favorite ? R.string.liked_song_saved : R.string.liked_song_removed,
                Toast.LENGTH_SHORT).show();
    }

    private void showPlaylistChooser() {
        Song currentSong = playbackManager.getCurrentSong();
        if (currentSong == null) {
            return;
        }
        List<String> names = playlistManager.getPlaylistNames();
        if (names.isEmpty()) {
            Toast.makeText(this, "Create a playlist first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PlaylistActivity.class));
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_to_playlist)
                .setItems(names.toArray(new String[0]), (dialog, which) -> {
                    playlistManager.addSongToPlaylist(names.get(which), currentSong);
                    Toast.makeText(this, R.string.song_added_to_playlist, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void render(Song song) {
        imgArtwork.setImageResource(R.drawable.bg_album_art);
        if (song == null) {
            tvSongTitle.setText(R.string.mini_player_default);
            tvSongArtist.setText(R.string.player_subtitle);
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            return;
        }
        tvSongTitle.setText(song.getTitle());
        tvSongArtist.setText(song.getArtist());
        btnPlayPause.setImageResource(
                playbackManager.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play
        );
        btnFavorite.setImageResource(
                favoritesManager.isFavorite(song.getId())
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );
        updateProgress(playbackManager.getCurrentPosition(), playbackManager.getDuration());
    }

    private void updateProgress(int positionMs, int durationMs) {
        seekBar.setMax(Math.max(durationMs, 1));
        seekBar.setProgress(positionMs);
        tvCurrentTime.setText(UiUtils.formatDuration(positionMs));
        tvDuration.setText(UiUtils.formatDuration(durationMs));
    }

    @Override
    public void onPlaybackChanged(Song currentSong, boolean isPlaying) {
        render(currentSong);
    }

    @Override
    public void onProgressChanged(int positionMs, int durationMs) {
        updateProgress(positionMs, durationMs);
    }
}
