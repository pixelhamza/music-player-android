package com.example.musicplayer.util;

public class UiUtils {
    public static String formatDuration(long durationMs) {
        long totalSeconds = durationMs / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }
}
