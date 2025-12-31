package com.example.multibrewtimer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class TimerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int timerIndex = intent.getIntExtra("timer_index", 0);
        
        // Always play the custom sound manually to ensure exactly one (correct) ring
        playRingtone(context);

        // Only show notification if the app is not in the foreground
        if (!MainActivity.isAppVisible) {
            showNotification(context, timerIndex);
        }
    }

    private void showNotification(Context context, int timerIndex) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "TIMER_CHANNEL_SILENT")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Timer Finished!")
                .setContentText("Tea Timer #" + (timerIndex + 1) + " is done.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(null) // Ensure builder also knows it's silent
                .setContentIntent(contentIntent);

        notificationManager.notify(timerIndex, builder.build());
    }

    private void playRingtone(Context context) {
        String customUri = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
                .getString("pref_tone_uri", null);
        
        try {
            Uri soundUri = (customUri != null) ? Uri.parse(customUri) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            Ringtone r = RingtoneManager.getRingtone(context, soundUri);
            r.play();
        } catch (Exception e) {
            // Silent fallback
        }
    }
}
