package com.example.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.adapter.PlaylistAdapter;
import com.example.musicplayer.adapter.SongAdapter;
import com.example.musicplayer.data.MusicRepository;
import com.example.musicplayer.data.PlaylistManager;
import com.example.musicplayer.data.SessionManager;
import com.example.musicplayer.model.Playlist;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.player.PlaybackListener;
import com.example.musicplayer.player.PlaybackManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements PlaybackListener {
    private final List<Song> allSongs = new ArrayList<>();

    private SessionManager sessionManager;
    private MusicRepository musicRepository;
    private PlaylistManager playlistManager;
    private PlaybackManager playbackManager;

    private SongAdapter songAdapter;
    private PlaylistAdapter playlistAdapter;

    private TextView tvSectionTitle;
    private TextView tvSectionSubtitle;
    private TextView tvPlaylistSection;
    private TextView tvEmptySongs;
    private TextView tvAccountName;
    private TextView tvAccountInitial;
    private View permissionState;
    private MaterialCardView miniPlayerCard;
    private TextView tvMiniSongTitle;
    private TextView tvMiniSongArtist;
    private MaterialButton btnMiniPlayPause;
    private BottomNavigationView bottomNavigationView;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (hasAudioPermission()) {
                    permissionState.setVisibility(View.GONE);
                    loadLibrary();
                } else {
                    permissionState.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        musicRepository = new MusicRepository();
        playlistManager = new PlaylistManager(this);
        playbackManager = PlaybackManager.getInstance(this);

        bindViews();
        setupRecyclerViews();
        setupActions();
        setupBottomNavigation();

        if (hasAudioPermission()) {
            permissionState.setVisibility(View.GONE);
            loadLibrary();
        } else {
            permissionState.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        playbackManager.addListener(this);
        songAdapter.notifyDataSetChanged();
        updateMiniPlayer(playbackManager.getCurrentSong(), playbackManager.isPlaying());
        refreshPlaylistCards();
        if (hasAudioPermission() && allSongs.isEmpty()) {
            loadLibrary();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        playbackManager.removeListener(this);
    }

    private void bindViews() {
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvPlaylistSection = findViewById(R.id.tvPlaylistSection);
        tvEmptySongs = findViewById(R.id.tvEmptySongs);
        tvAccountName = findViewById(R.id.tvAccountName);
        tvAccountInitial = findViewById(R.id.tvAccountInitial);
        permissionState = findViewById(R.id.permissionState);
        miniPlayerCard = findViewById(R.id.miniPlayerCard);
        tvMiniSongTitle = findViewById(R.id.tvMiniSongTitle);
        tvMiniSongArtist = findViewById(R.id.tvMiniSongArtist);
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause);
        bottomNavigationView = findViewById(R.id.bottomNav);
    }

    private void setupRecyclerViews() {
        RecyclerView recyclerSongs = findViewById(R.id.recyclerSongs);
        RecyclerView recyclerPlaylists = findViewById(R.id.recyclerPlaylists);

        songAdapter = new SongAdapter(new ArrayList<>(), new SongAdapter.SongActionListener() {
            @Override
            public void onSongClicked(Song song, int position) {
                List<Song> queue = songAdapter.getVisibleSongs();
                playbackManager.playQueue(queue, position);
                openPlayer();
            }

            @Override
            public void onActionClicked(Song song) {
                showPlaylistChooser(song);
            }
        });

        playlistAdapter = new PlaylistAdapter(false, playlist -> {
            if (playlistManager.hasPlaylist(playlist.getName())) {
                Intent intent = new Intent(this, PlaylistDetailActivity.class);
                intent.putExtra("playlist_name", playlist.getName());
                startActivity(intent);
            } else {
                startActivity(new Intent(this, PlaylistActivity.class));
            }
        });

        recyclerSongs.setLayoutManager(new LinearLayoutManager(this));
        recyclerSongs.setAdapter(songAdapter);

        recyclerPlaylists.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerPlaylists.setAdapter(playlistAdapter);
    }

    private void setupActions() {
        findViewById(R.id.btnGrantPermission).setOnClickListener(v -> requestMusicPermission());
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });

        miniPlayerCard.setOnClickListener(v -> openPlayer());
        btnMiniPlayPause.setOnClickListener(v -> {
            playbackManager.togglePlayPause();
            updateMiniPlayer(playbackManager.getCurrentSong(), playbackManager.isPlaying());
        });
    }

    private void setupBottomNavigation() {
        String displayName = sessionManager.getDisplayName();
        tvAccountName.setText(displayName);
        tvAccountInitial.setText(displayName.isEmpty()
                ? getString(R.string.account_initial_placeholder)
                : String.valueOf(Character.toUpperCase(displayName.charAt(0))));
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                showHomeMode(true);
                return true;
            }
            if (itemId == R.id.nav_songs) {
                showHomeMode(false);
                return true;
            }
            if (itemId == R.id.nav_playlists) {
                startActivity(new Intent(this, PlaylistActivity.class));
                return false;
            }
            return false;
        });
    }

    private void showHomeMode(boolean includePlaylists) {
        tvSectionTitle.setText(includePlaylists ? R.string.quick_picks : R.string.songs);
        tvSectionSubtitle.setText(includePlaylists ? R.string.home_subtitle : R.string.player_subtitle);
        updatePlaylistVisibility(includePlaylists);
    }

    private void requestMusicPermission() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        permissionLauncher.launch(permissions.toArray(new String[0]));
    }

    private boolean hasAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void loadLibrary() {
        new Thread(() -> {
            List<Song> songs = musicRepository.getAllSongs(this);
            runOnUiThread(() -> {
                allSongs.clear();
                allSongs.addAll(songs);
                songAdapter.setSongs(songs);
                tvEmptySongs.setVisibility(songs.isEmpty() ? View.VISIBLE : View.GONE);
                refreshPlaylistCards();
                Toast.makeText(this, getString(R.string.songs_loaded, songs.size()), Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void refreshPlaylistCards() {
        List<Playlist> userPlaylists = playlistManager.getPlaylists(allSongs);
        playlistAdapter.setPlaylists(userPlaylists);
        updatePlaylistVisibility(bottomNavigationView.getSelectedItemId() == R.id.nav_home);
    }

    private void updatePlaylistVisibility(boolean includePlaylists) {
        int visibility = includePlaylists && playlistAdapter.getItemCount() > 0 ? View.VISIBLE : View.GONE;
        tvPlaylistSection.setVisibility(visibility);
        findViewById(R.id.recyclerPlaylists).setVisibility(visibility);
    }

    private void showPlaylistChooser(@NonNull Song song) {
        List<String> names = playlistManager.getPlaylistNames();
        if (names.isEmpty()) {
            Toast.makeText(this, "Create a playlist first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PlaylistActivity.class));
            return;
        }
        String[] items = names.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_to_playlist)
                .setItems(items, (dialog, which) -> {
                    playlistManager.addSongToPlaylist(names.get(which), song);
                    Toast.makeText(this, R.string.song_added_to_playlist, Toast.LENGTH_SHORT).show();
                    refreshPlaylistCards();
                })
                .show();
    }

    private void openPlayer() {
        if (playbackManager.getCurrentSong() == null) {
            Toast.makeText(this, "Select a song from the list first.", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, PlayerActivity.class));
    }

    private void updateMiniPlayer(Song song, boolean isPlaying) {
        if (song == null) {
            miniPlayerCard.setVisibility(View.GONE);
            return;
        }
        miniPlayerCard.setVisibility(View.VISIBLE);
        tvMiniSongTitle.setText(song.getTitle());
        tvMiniSongArtist.setText(song.getArtist());
        btnMiniPlayPause.setIconResource(
                isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play
        );
    }

    @Override
    public void onPlaybackChanged(Song currentSong, boolean isPlaying) {
        updateMiniPlayer(currentSong, isPlaying);
    }

    @Override
    public void onProgressChanged(int positionMs, int durationMs) {
    }
}
