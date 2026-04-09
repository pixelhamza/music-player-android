package com.example.musicplayer.data;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import com.example.musicplayer.model.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MusicRepository {
    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList(
            "mp3", "wav", "m4a", "aac", "flac", "ogg", "opus", "amr", "3gp"
    );

    public List<Song> getAllSongs(Context context) {
        List<Song> songs = new ArrayList<>();
        Set<String> visitedPaths = new HashSet<>();

        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        scanFolder(downloadsFolder, songs, visitedPaths);

        File appDownloadsFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        scanFolder(appDownloadsFolder, songs, visitedPaths);

        Collections.sort(songs, (first, second) -> Long.compare(
                new File(second.getDataPath()).lastModified(),
                new File(first.getDataPath()).lastModified()
        ));
        return songs;
    }

    private void scanFolder(File folder, List<Song> songs, Set<String> visitedPaths) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        for (File file : files) {
            if (file.isDirectory()) {
                scanFolder(file, songs, visitedPaths);
            } else if (isAudioFile(file)) {
                String path = file.getAbsolutePath();
                if (visitedPaths.add(path)) {
                    Song song = createSongFromFile(file);
                    if (song != null) {
                        songs.add(song);
                    }
                }
            }
        }
    }

    private boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase(Locale.getDefault());
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1) {
            return false;
        }
        String extension = name.substring(lastDot + 1);
        return AUDIO_EXTENSIONS.contains(extension);
    }

    private Song createSongFromFile(File file) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(file.getAbsolutePath());
            String title = readOrDefault(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    stripExtension(file.getName())
            );
            String artist = readOrDefault(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                    "Unknown artist"
            );
            String album = readOrDefault(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                    "Downloads"
            );
            long duration = parseDuration(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            );
            if (duration < 10_000L) {
                return null;
            }

            return new Song(
                    Integer.toUnsignedLong(file.getAbsolutePath().hashCode()),
                    title,
                    artist,
                    album,
                    file.getAbsolutePath(),
                    duration
            );
        } catch (Exception e) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private String readOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private long parseDuration(String durationValue) {
        try {
            return durationValue == null ? 0L : Long.parseLong(durationValue);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private String stripExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0) {
            return fileName;
        }
        return fileName.substring(0, lastDot);
    }
}
