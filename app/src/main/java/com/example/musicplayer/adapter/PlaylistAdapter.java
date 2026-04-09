package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    public interface PlaylistClickListener {
        void onPlaylistClicked(Playlist playlist);
    }

    public interface PlaylistDeleteListener {
        void onPlaylistDelete(Playlist playlist);
    }

    private final boolean fullWidthCards;
    private final PlaylistClickListener listener;
    private final PlaylistDeleteListener deleteListener;
    private final List<Playlist> playlists = new ArrayList<>();

    public PlaylistAdapter(boolean fullWidthCards, PlaylistClickListener listener) {
        this(fullWidthCards, listener, null);
    }

    public PlaylistAdapter(boolean fullWidthCards,
                           PlaylistClickListener listener,
                           PlaylistDeleteListener deleteListener) {
        this.fullWidthCards = fullWidthCards;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    public void setPlaylists(List<Playlist> items) {
        playlists.clear();
        if (items != null) {
            playlists.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        if (fullWidthCards) {
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            view.setLayoutParams(layoutParams);
        }
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.tvTitle.setText(playlist.getName());
        holder.tvSubtitle.setText(playlist.getSubtitle());
        holder.tvCount.setText(playlist.getSongCount() + " songs");
        holder.itemView.setOnClickListener(v -> listener.onPlaylistClicked(playlist));
        holder.btnDelete.setVisibility(deleteListener == null ? View.GONE : View.VISIBLE);
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onPlaylistDelete(playlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvCount;
        private final com.google.android.material.button.MaterialButton btnDelete;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPlaylistTitle);
            tvSubtitle = itemView.findViewById(R.id.tvPlaylistSubtitle);
            tvCount = itemView.findViewById(R.id.tvPlaylistCount);
            btnDelete = itemView.findViewById(R.id.btnDeletePlaylist);
        }
    }
}
