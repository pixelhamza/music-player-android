package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.data.FavoritesManager;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.util.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    public interface SongActionListener {
        void onSongClicked(Song song, int position);

        void onFavoriteClicked(Song song);

        void onActionClicked(Song song);
    }

    private final FavoritesManager favoritesManager;
    private final SongActionListener listener;
    private final List<Song> allSongs = new ArrayList<>();
    private final List<Song> visibleSongs = new ArrayList<>();
    private int actionIconRes = android.R.drawable.ic_input_add;

    public SongAdapter(List<Song> songs, FavoritesManager favoritesManager, SongActionListener listener) {
        this.favoritesManager = favoritesManager;
        this.listener = listener;
        setSongs(songs);
    }

    public void setSongs(List<Song> songs) {
        allSongs.clear();
        visibleSongs.clear();
        if (songs != null) {
            allSongs.addAll(songs);
            visibleSongs.addAll(songs);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        visibleSongs.clear();
        if (query == null || query.trim().isEmpty()) {
            visibleSongs.addAll(allSongs);
        } else {
            String lower = query.toLowerCase(Locale.getDefault());
            for (Song song : allSongs) {
                String title = song.getTitle() == null ? "" : song.getTitle().toLowerCase(Locale.getDefault());
                String artist = song.getArtist() == null ? "" : song.getArtist().toLowerCase(Locale.getDefault());
                String album = song.getAlbum() == null ? "" : song.getAlbum().toLowerCase(Locale.getDefault());
                if (title.contains(lower) || artist.contains(lower) || album.contains(lower)) {
                    visibleSongs.add(song);
                }
            }
        }
        notifyDataSetChanged();
    }

    public List<Song> getVisibleSongs() {
        return new ArrayList<>(visibleSongs);
    }

    public void setActionIconRes(@DrawableRes int actionIconRes) {
        this.actionIconRes = actionIconRes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = visibleSongs.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvMeta.setText(song.getArtist() + " | " + UiUtils.formatDuration(song.getDuration()));
        holder.btnFavorite.setImageResource(
                favoritesManager.isFavorite(song.getId())
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );
        holder.btnAction.setImageResource(actionIconRes);
        holder.itemView.setOnClickListener(v -> listener.onSongClicked(song, holder.getBindingAdapterPosition()));
        holder.imgArt.setOnClickListener(v -> listener.onSongClicked(song, holder.getBindingAdapterPosition()));
        holder.btnFavorite.setOnClickListener(v -> listener.onFavoriteClicked(song));
        holder.btnAction.setOnClickListener(v -> listener.onActionClicked(song));
    }

    @Override
    public int getItemCount() {
        return visibleSongs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgArt;
        private final TextView tvTitle;
        private final TextView tvMeta;
        private final ImageButton btnFavorite;
        private final ImageButton btnAction;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgArt = itemView.findViewById(R.id.imgArt);
            tvTitle = itemView.findViewById(R.id.tvSongTitle);
            tvMeta = itemView.findViewById(R.id.tvSongMeta);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
