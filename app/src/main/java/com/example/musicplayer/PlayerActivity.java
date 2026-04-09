package com.example.musicplayer;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.model.Song;
import com.example.musicplayer.player.PlaybackListener;
import com.example.musicplayer.player.PlaybackManager;
import com.example.musicplayer.util.UiUtils;
import com.google.android.material.button.MaterialButton;

public class PlayerActivity extends AppCompatActivity implements PlaybackListener {
    private PlaybackManager playbackManager;

    private ImageView imgArtwork;
    private TextView tvSongTitle;
    private TextView tvSongArtist;
    private TextView tvCurrentTime;
    private TextView tvDuration;
    private SeekBar seekBar;
    private MaterialButton btnPlayPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playbackManager = PlaybackManager.getInstance(this);

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

    private void render(Song song) {
        imgArtwork.setImageDrawable(null);
        if (song == null) {
            tvSongTitle.setText(R.string.mini_player_default);
            tvSongArtist.setText(R.string.player_subtitle);
            btnPlayPause.setIconResource(android.R.drawable.ic_media_play);
            return;
        }
        tvSongTitle.setText(song.getTitle());
        tvSongArtist.setText(song.getArtist());
        btnPlayPause.setIconResource(
                playbackManager.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play
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
