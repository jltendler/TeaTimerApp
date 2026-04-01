package com.example.multibrewtimer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    public static boolean isAppVisible = false;
    private TimerAdapter adapter;

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
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        checkStopSound(intent);
    }

    private void checkStopSound(android.content.Intent intent) {
        if (intent != null && intent.getBooleanExtra("stop_sound", false)) {
            SoundHelper.stopRingtone();
            intent.removeExtra("stop_sound"); // Don't trigger on rotation
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        checkStopSound(getIntent());
        
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
        
        adapter = new TimerAdapter(6);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.refreshTimers();
        }
    }

    private android.os.CountDownTimer pipTimer;

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        android.content.SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("pref_enable_pip", false)) return;

        if (adapter != null) {
            TimerAdapter.TimerModel soonest = adapter.getSoonestEndingTimer();
            if (soonest != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    android.app.PictureInPictureParams params = new android.app.PictureInPictureParams.Builder()
                            .setAspectRatio(new android.util.Rational(16, 9))
                            .build();
                    enterPictureInPictureMode(params);
                }
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, android.content.res.Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        
        androidx.recyclerview.widget.RecyclerView recyclerView = findViewById(R.id.recyclerViewTimers);
        android.view.View pipContainer = findViewById(R.id.pipContainer);
        android.widget.TextView tvPipCountdown = findViewById(R.id.tvPipCountdown);

        if (isInPictureInPictureMode) {
            recyclerView.setVisibility(android.view.View.GONE);
            pipContainer.setVisibility(android.view.View.VISIBLE);
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            TimerAdapter.TimerModel soonest = adapter.getSoonestEndingTimer();
            if (soonest != null) {
                long remaining = soonest.endTimeMillis - System.currentTimeMillis();
                if (pipTimer != null) pipTimer.cancel();
                pipTimer = new android.os.CountDownTimer(remaining, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long seconds = millisUntilFinished / 1000;
                        long minutes = seconds / 60;
                        long hours = minutes / 60;
                        
                        String timeStr;
                        if (hours > 0) timeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
                        else timeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds % 60);
                        tvPipCountdown.setText(timeStr);
                    }

                    @Override
                    public void onFinish() {
                        tvPipCountdown.setText("Done");
                    }
                }.start();
            }
        } else {
            recyclerView.setVisibility(android.view.View.VISIBLE);
            pipContainer.setVisibility(android.view.View.GONE);
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }

            if (pipTimer != null) {
                pipTimer.cancel();
                pipTimer = null;
            }
            
            if (adapter != null) {
                adapter.refreshTimers();
            }
        }
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
