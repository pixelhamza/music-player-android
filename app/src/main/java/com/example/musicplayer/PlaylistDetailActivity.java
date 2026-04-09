package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.adapter.SongAdapter;
import com.example.musicplayer.data.MusicRepository;
import com.example.musicplayer.data.PlaylistManager;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.player.PlaybackManager;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailActivity extends AppCompatActivity {
    private final List<Song> librarySongs = new ArrayList<>();
    private final List<Song> playlistSongs = new ArrayList<>();

    private String playlistName;
    private PlaylistManager playlistManager;
    private MusicRepository musicRepository;
    private PlaybackManager playbackManager;
    private SongAdapter songAdapter;
    private TextView tvEmptyPlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        playlistName = getIntent().getStringExtra("playlist_name");
        if (playlistName == null) {
            finish();
            return;
        }

        playlistManager = new PlaylistManager(this);
        musicRepository = new MusicRepository();
        playbackManager = PlaybackManager.getInstance(this);

        ((TextView) findViewById(R.id.tvPlaylistName)).setText(playlistName);
        tvEmptyPlaylist = findViewById(R.id.tvEmptyPlaylist);

        setupRecyclerView();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadLibrary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPlaylistSongs();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerPlaylistSongs);
        songAdapter = new SongAdapter(new ArrayList<>(), new SongAdapter.SongActionListener() {
            @Override
            public void onSongClicked(Song song, int position) {
                playbackManager.playQueue(songAdapter.getVisibleSongs(), position);
                startActivity(new Intent(PlaylistDetailActivity.this, PlayerActivity.class));
            }

            @Override
            public void onActionClicked(Song song) {
                playlistManager.removeSongFromPlaylist(playlistName, song);
                Toast.makeText(PlaylistDetailActivity.this, R.string.song_removed_from_playlist, Toast.LENGTH_SHORT).show();
                refreshPlaylistSongs();
            }
        });
        songAdapter.setActionIconRes(android.R.drawable.ic_menu_delete);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(songAdapter);
    }

    private void loadLibrary() {
        new Thread(() -> {
            List<Song> songs = musicRepository.getAllSongs(this);
            runOnUiThread(() -> {
                librarySongs.clear();
                librarySongs.addAll(songs);
                refreshPlaylistSongs();
            });
        }).start();
    }

    private void refreshPlaylistSongs() {
        playlistSongs.clear();
        playlistSongs.addAll(playlistManager.getSongsForPlaylist(playlistName, librarySongs));
        songAdapter.setSongs(playlistSongs);
        tvEmptyPlaylist.setVisibility(playlistSongs.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
