package com.example.multibrewtimer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_RINGTONE = 1;
    private TextView tvCurrentSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvCurrentSound = findViewById(R.id.tvCurrentSound);
        Button btnPickSound = findViewById(R.id.btnPickSound);

        updateCurrentSoundDisplay();

        btnPickSound.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Timer Sound");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

            String currentUri = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("pref_tone_uri", null);
            if (currentUri != null) {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentUri));
            }

            startActivityForResult(intent, REQUEST_CODE_PICK_RINGTONE);
        });

        // Increment Setting
        android.widget.Spinner spinnerIncrement = findViewById(R.id.spinnerIncrement);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int currentStep = prefs.getInt("pref_increment_step", 5); // Default 5
        
        // Find position of saved step in the array (1, 5, 10, 15, 30)
        String[] steps = getResources().getStringArray(R.array.increment_steps);
        for(int i=0; i<steps.length; i++) {
            if(Integer.parseInt(steps[i]) == currentStep) {
                spinnerIncrement.setSelection(i);
                break;
            }
        }

        spinnerIncrement.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                int val = Integer.parseInt(steps[position]);
                prefs.edit().putInt("pref_increment_step", val).apply();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Timer Theme Setting
        android.widget.Spinner spinnerTimerTheme = findViewById(R.id.spinnerTimerTheme);
        int currentTimerTheme = prefs.getInt("pref_timer_theme", 0); // Default 0 (Original)
        spinnerTimerTheme.setSelection(currentTimerTheme);

        spinnerTimerTheme.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                prefs.edit().putInt("pref_timer_theme", position).apply();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Theme Settings
        android.widget.RadioGroup rgTheme = findViewById(R.id.rgTheme);
        int savedTheme = prefs.getInt("pref_theme_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        if (savedTheme == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO) rgTheme.check(R.id.rbLight);
        else if (savedTheme == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) rgTheme.check(R.id.rbDark);
        else rgTheme.check(R.id.rbSystem);

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rbLight) mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
            else if (checkedId == R.id.rbDark) mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
            else mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            prefs.edit().putInt("pref_theme_mode", mode).apply();
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode);
        });

        // Alert Repeat Count Setting
        android.widget.Spinner spinnerAlertCount = findViewById(R.id.spinnerAlertCount);
        int currentAlertCount = prefs.getInt("pref_alert_count", 1); // Default 1
        
        String[] alertCounts = getResources().getStringArray(R.array.alert_repeat_counts);
        for(int i=0; i<alertCounts.length; i++) {
            if(Integer.parseInt(alertCounts[i]) == currentAlertCount) {
                spinnerAlertCount.setSelection(i);
                break;
            }
        }

        spinnerAlertCount.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                int val = Integer.parseInt(alertCounts[position]);
                prefs.edit().putInt("pref_alert_count", val).apply();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Escalate Volume Setting
        SwitchCompat switchEscalateVolume = findViewById(R.id.switchEscalateVolume);
        boolean currentEscalateVolume = prefs.getBoolean("pref_escalate_volume", false); // Default false
        switchEscalateVolume.setChecked(currentEscalateVolume);

        switchEscalateVolume.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("pref_escalate_volume", isChecked).apply();
        });

        // Show Last Used Setting
        SwitchCompat switchShowLastUsed = findViewById(R.id.switchShowLastUsed);
        boolean currentShowLastUsed = prefs.getBoolean("pref_show_last_used", false); // Default false
        switchShowLastUsed.setChecked(currentShowLastUsed);

        switchShowLastUsed.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("pref_show_last_used", isChecked).apply();
        });

        // Show Rank Badge Setting
        SwitchCompat switchShowRankBadge = findViewById(R.id.switchShowRankBadge);
        boolean currentShowRankBadge = prefs.getBoolean("pref_show_rank_badge", true); // Default true
        switchShowRankBadge.setChecked(currentShowRankBadge);

        switchShowRankBadge.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("pref_show_rank_badge", isChecked).apply();
        });

        // Enable Timer Overlay (PIP) Setting
        SwitchCompat switchEnablePip = findViewById(R.id.switchEnablePip);
        boolean currentEnablePip = prefs.getBoolean("pref_enable_pip", false); // Default false
        switchEnablePip.setChecked(currentEnablePip);

        switchEnablePip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("pref_enable_pip", isChecked).apply();
        });

        // Clear Timestamps
        Button btnClearTimestamps = findViewById(R.id.btnClearTimestamps);
        btnClearTimestamps.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            // Clear for 6 timers
            for (int i = 0; i < 6; i++) {
                editor.remove("timer_" + i + "_last_used");
            }
            editor.apply();
            
            android.widget.Toast.makeText(SettingsActivity.this, "Timestamps Cleared", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void updateCurrentSoundDisplay() {
        String currentUri = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("pref_tone_uri", null);
        
        if (currentUri == null) {
            tvCurrentSound.setText("Default Beep");
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(currentUri));
            if (ringtone != null) {
                tvCurrentSound.setText(ringtone.getTitle(this));
            } else {
                tvCurrentSound.setText("Custom Ringtone");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_RINGTONE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putString("pref_tone_uri", uri.toString()).apply();
                updateCurrentSoundDisplay();
            }
        }
    }
}
