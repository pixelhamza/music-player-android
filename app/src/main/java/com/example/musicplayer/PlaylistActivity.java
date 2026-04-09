package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.adapter.PlaylistAdapter;
import com.example.musicplayer.data.MusicRepository;
import com.example.musicplayer.data.PlaylistManager;
import com.example.musicplayer.model.Playlist;
import com.example.musicplayer.model.Song;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {
    private final List<Song> allSongs = new ArrayList<>();

    private PlaylistManager playlistManager;
    private MusicRepository musicRepository;
    private PlaylistAdapter playlistAdapter;
    private TextInputEditText etPlaylistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        playlistManager = new PlaylistManager(this);
        musicRepository = new MusicRepository();

        etPlaylistName = findViewById(R.id.etPlaylistName);
        RecyclerView recyclerView = findViewById(R.id.recyclerUserPlaylists);
        MaterialButton btnCreate = findViewById(R.id.btnCreatePlaylist);

        playlistAdapter = new PlaylistAdapter(true, playlist -> {
            Intent intent = new Intent(this, PlaylistDetailActivity.class);
            intent.putExtra("playlist_name", playlist.getName());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(playlistAdapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCreate.setOnClickListener(v -> createPlaylist());

        loadLibraryAndPlaylists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLibraryAndPlaylists();
    }

    private void createPlaylist() {
        String name = etPlaylistName.getText() == null ? "" : etPlaylistName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter a playlist name.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!playlistManager.createPlaylist(name)) {
            Toast.makeText(this, "That playlist already exists.", Toast.LENGTH_SHORT).show();
            return;
        }
        etPlaylistName.setText("");
        Toast.makeText(this, R.string.playlist_created, Toast.LENGTH_SHORT).show();
        refreshPlaylists();
    }

    private void loadLibraryAndPlaylists() {
        new Thread(() -> {
            List<Song> songs = musicRepository.getAllSongs(this);
            runOnUiThread(() -> {
                allSongs.clear();
                allSongs.addAll(songs);
                refreshPlaylists();
            });
        }).start();
    }

    private void refreshPlaylists() {
        List<Playlist> playlists = playlistManager.getPlaylists(allSongs);
        playlistAdapter.setPlaylists(playlists);
    }
}
