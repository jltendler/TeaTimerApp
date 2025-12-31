package com.example.multibrewtimer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    public static boolean isAppVisible = false;

    @Override
    protected void onStart() {
        super.onStart();
        isAppVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isAppVisible = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply Theme Preference
        int themeMode = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("pref_theme_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(themeMode);
        
        setContentView(R.layout.activity_main);
        
        createNotificationChannel();
        checkPermissions();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewTimers);
        
        // 2 Columns looks better than 3 on narrow phones, but user asked for 9 timers on main page.
        // 3 columns might be tight but fits "9 timers" well in a 3x3 grid.
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setItemViewCacheSize(10); // Keep all 9 timers in memory to avoid recycling issues
        
        TimerAdapter adapter = new TimerAdapter();
        recyclerView.setAdapter(new TimerAdapter(6));
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Channel with sound disabled so we can play custom sound manually without double-ringing
            CharSequence name = "Timer Completion";
            String description = "Alerts when a tea timer finishes";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationChannel channel = new android.app.NotificationChannel("TIMER_CHANNEL_SILENT", name, importance);
            channel.setDescription(description);
            channel.setSound(null, null); // Silence system default
            channel.enableVibration(true);
            
            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
