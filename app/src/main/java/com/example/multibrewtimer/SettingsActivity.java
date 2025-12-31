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
