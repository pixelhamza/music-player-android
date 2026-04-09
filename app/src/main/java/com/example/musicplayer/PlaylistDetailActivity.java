package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.adapter.SongAdapter;
import com.example.musicplayer.data.FavoritesManager;
import com.example.musicplayer.data.MusicRepository;
import com.example.musicplayer.data.PlaylistManager;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.player.PlaybackManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlaylistDetailActivity extends AppCompatActivity {
    private final List<Song> librarySongs = new ArrayList<>();
    private final List<Song> playlistSongs = new ArrayList<>();

    private String playlistName;
    private PlaylistManager playlistManager;
    private MusicRepository musicRepository;
    private FavoritesManager favoritesManager;
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
        favoritesManager = new FavoritesManager(this);
        playbackManager = PlaybackManager.getInstance(this);

        ((TextView) findViewById(R.id.tvPlaylistName)).setText(playlistName);
        tvEmptyPlaylist = findViewById(R.id.tvEmptyPlaylist);

        setupRecyclerView();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddSongs).setOnClickListener(v -> showAddSongsDialog());

        loadLibrary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPlaylistSongs();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerPlaylistSongs);
        songAdapter = new SongAdapter(new ArrayList<>(), favoritesManager, new SongAdapter.SongActionListener() {
            @Override
            public void onSongClicked(Song song, int position) {
                playbackManager.playQueue(songAdapter.getVisibleSongs(), position);
                startActivity(new Intent(PlaylistDetailActivity.this, PlayerActivity.class));
            }

            @Override
            public void onFavoriteClicked(Song song) {
                boolean favorite = favoritesManager.toggleFavorite(song);
                songAdapter.notifyDataSetChanged();
                Toast.makeText(PlaylistDetailActivity.this,
                        favorite ? R.string.liked_song_saved : R.string.liked_song_removed,
                        Toast.LENGTH_SHORT).show();
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

    private void showAddSongsDialog() {
        if (librarySongs.isEmpty()) {
            Toast.makeText(this, "Load your library first to add songs.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] titles = new String[librarySongs.size()];
        boolean[] checkedItems = new boolean[librarySongs.size()];
        Set<Long> selectedIds = new HashSet<>();
        for (Song song : playlistSongs) {
            selectedIds.add(song.getId());
        }
        for (int i = 0; i < librarySongs.size(); i++) {
            Song song = librarySongs.get(i);
            titles[i] = song.getTitle() + " - " + song.getArtist();
            checkedItems[i] = selectedIds.contains(song.getId());
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_songs)
                .setMultiChoiceItems(titles, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    List<Song> selectedSongs = new ArrayList<>();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            selectedSongs.add(librarySongs.get(i));
                        }
                    }
                    playlistManager.addSongsToPlaylist(playlistName, selectedSongs);
                    Toast.makeText(this, R.string.song_added_to_playlist, Toast.LENGTH_SHORT).show();
                    refreshPlaylistSongs();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
