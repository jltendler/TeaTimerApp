package com.example.multibrewtimer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class SoundHelper {
    
    private static MediaPlayer sCurrentMediaPlayer;

    public static void playRingtone(Context context) {
        String customUri = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("pref_tone_uri", null);
        
        int playCount = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("pref_alert_count", 1);
        
        boolean escalateVolume = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("pref_escalate_volume", false);
        
        try {
            Uri soundUri = (customUri != null) ? Uri.parse(customUri) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            playWithMediaPlayer(context, soundUri, playCount, escalateVolume);
        } catch (Exception e) {
            // fallback to default if custom fails
            try {
                Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                playWithMediaPlayer(context, defaultUri, playCount, escalateVolume);
            } catch (Exception e2) {
                // Persistent silence
            }
        }
    }

    public static void stopRingtone() {
        if (sCurrentMediaPlayer != null) {
            try {
                if (sCurrentMediaPlayer.isPlaying()) {
                    sCurrentMediaPlayer.stop();
                }
                sCurrentMediaPlayer.release();
            } catch (Exception e) {
                Log.e("SoundHelper", "Error stopping MediaPlayer", e);
            } finally {
                sCurrentMediaPlayer = null;
            }
        }
    }

    private static void playWithMediaPlayer(Context context, Uri soundUri, int totalPlays, boolean escalateVolume) {
        stopRingtone(); // Stop any currently playing sound

        sCurrentMediaPlayer = new MediaPlayer();
        try {
            sCurrentMediaPlayer.setDataSource(context, soundUri);
            sCurrentMediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            sCurrentMediaPlayer.prepare();

            final float[] currentVolume = new float[]{1.0f};
            final float volumeStep;
            
            if (escalateVolume && totalPlays > 1) {
                // Start low, end at 1.0f
                currentVolume[0] = 0.2f;
                volumeStep = (1.0f - 0.2f) / (totalPlays - 1);
            } else {
                volumeStep = 0f;
            }

            sCurrentMediaPlayer.setVolume(currentVolume[0], currentVolume[0]);

            final int[] playsRemaining = new int[]{totalPlays};

            sCurrentMediaPlayer.setOnCompletionListener(mp -> {
                playsRemaining[0]--;
                if (playsRemaining[0] > 0) {
                    if (escalateVolume) {
                        currentVolume[0] = Math.min(1.0f, currentVolume[0] + volumeStep);
                        mp.setVolume(currentVolume[0], currentVolume[0]);
                    }
                    mp.start();
                } else {
                    stopRingtone();
                }
            });

            sCurrentMediaPlayer.start();
            
        } catch (Exception e) {
            Log.e("SoundHelper", "Error playing sound with MediaPlayer", e);
            stopRingtone();
        }
    }
}
