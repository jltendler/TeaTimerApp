package com.example.multibrewtimer;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

public class SoundHelper {
    public static void playRingtone(Context context) {
        String customUri = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("pref_tone_uri", null);
        
        try {
            Uri soundUri = (customUri != null) ? Uri.parse(customUri) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            Ringtone r = RingtoneManager.getRingtone(context, soundUri);
            if (r != null) {
                r.play();
            }
        } catch (Exception e) {
            // fallback to default if custom fails
            try {
                Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                Ringtone r = RingtoneManager.getRingtone(context, defaultUri);
                if (r != null) r.play();
            } catch (Exception e2) {
                // Persistent silence
            }
        }
    }
}
