package com.example.multibrewtimer;

import android.os.CountDownTimer;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.TimerViewHolder> {

    private final List<TimerModel> timers;

    private final ToneGenerator toneGen;

    public TimerAdapter() {
        this(6); // Default 6 timers
    }

    public TimerAdapter(int count) {
        toneGen = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        timers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TimerModel model = new TimerModel();
            model.index = i;
            timers.add(model);
        }
    }

    @NonNull
    @Override
    public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timer, parent, false);
        return new TimerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
        TimerModel timer = timers.get(position);
        holder.bind(timer);
    }

    @Override
    public int getItemCount() {
        return timers.size();
    }

    class TimerViewHolder extends RecyclerView.ViewHolder {
        EditText etTimeInput;
        Spinner spinnerUnit;
        Button btnStartStop, btnStopRunning;
        TextView tvCountdown;
        CircleTimerView circleVisualizer;
        LinearLayout inputContainer, runningContainer;

        CountDownTimer countDownTimer;

        public TimerViewHolder(@NonNull View itemView) {
            super(itemView);
            etTimeInput = itemView.findViewById(R.id.etTimeInput);
            spinnerUnit = itemView.findViewById(R.id.spinnerUnit);
            btnStartStop = itemView.findViewById(R.id.btnStartStop);
            btnStopRunning = itemView.findViewById(R.id.btnStopRunning);
            tvCountdown = itemView.findViewById(R.id.tvCountdown);
            circleVisualizer = itemView.findViewById(R.id.circleVisualizer);
            inputContainer = itemView.findViewById(R.id.inputContainer);
            runningContainer = itemView.findViewById(R.id.runningContainer);
        }

        void bind(TimerModel timer) {
            // Restore State from model/prefs
            android.content.SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
            
            // Get Theme Color
            int themeIndex = prefs.getInt("pref_timer_theme", 0);
            int resolvedColor = ThemeHelper.getColor(themeIndex, timer.index % 6);
            
            circleVisualizer.setColor(resolvedColor);
            
            // Consolidate definitions
            Button btnPlus = itemView.findViewById(R.id.btnPlus);
            Button btnMinus = itemView.findViewById(R.id.btnMinus);
            
            // Tint all buttons with this color
            android.content.res.ColorStateList csl = android.content.res.ColorStateList.valueOf(resolvedColor);
            btnStartStop.setBackgroundTintList(csl);
            btnStopRunning.setBackgroundTintList(csl);
            btnPlus.setBackgroundTintList(csl);
            btnMinus.setBackgroundTintList(csl);


            
            if (timer.inputValue == null) {
                String savedVal = prefs.getString("timer_" + timer.index + "_val", "");
                if (savedVal.isEmpty()) {
                    timer.inputValue = "90"; // Default 90
                    timer.unitIndex = 0;     // Default Seconds
                } else {
                    timer.inputValue = savedVal;
                    timer.unitIndex = prefs.getInt("timer_" + timer.index + "_unit", 0);
                }
                
                // Re-sync end time from persistent storage
                timer.endTimeMillis = prefs.getLong("timer_" + timer.index + "_end_time", 0);
                timer.totalTime = prefs.getLong("timer_" + timer.index + "_total_time", 0);
                
                if (timer.endTimeMillis > System.currentTimeMillis()) {
                    timer.isRunning = true;
                    timer.remainingTime = timer.endTimeMillis - System.currentTimeMillis();
                } else if (timer.endTimeMillis > 0) {
                    // It finished while we were gone
                    timer.isRunning = false;
                    timer.endTimeMillis = 0;
                    prefs.edit().putLong("timer_" + timer.index + "_end_time", 0).apply();
                } else {
                    timer.isRunning = false;
                }
            }

            if (timer.isRunning) {
                inputContainer.setVisibility(View.GONE);
                runningContainer.setVisibility(View.VISIBLE);
                startTicker(timer); // Resume visual updates
            } else {
                inputContainer.setVisibility(View.VISIBLE);
                runningContainer.setVisibility(View.GONE);
                circleVisualizer.setProgress(1.0f);
                if(countDownTimer != null) countDownTimer.cancel();
            }

            // Input Handling
            if (timer.inputValue != null) etTimeInput.setText(timer.inputValue);
            
            etTimeInput.addTextChangedListener(new TextWatcher() {
                 @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                 @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                 @Override public void afterTextChanged(Editable s) { 
                     timer.inputValue = s.toString();
                     prefs.edit().putString("timer_" + timer.index + "_val", timer.inputValue).apply();
                 }
            });
            
            // +/- Buttons
            int incrementStep = prefs.getInt("pref_increment_step", 5);
            
            btnPlus.setOnClickListener(v -> adjustTime(timer, incrementStep));
            btnMinus.setOnClickListener(v -> adjustTime(timer, -incrementStep));
            
            // Avoid triggering listener during initial setSelection
            spinnerUnit.setOnItemSelectedListener(null);
            spinnerUnit.setSelection(timer.unitIndex);
            spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { 
                    timer.unitIndex = position;
                    prefs.edit().putInt("timer_" + timer.index + "_unit", position).apply();
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Button Action
            btnStartStop.setOnClickListener(v -> startTimer(timer));
            btnStopRunning.setOnClickListener(v -> stopTimer(timer, true));
        }
        
        private void adjustTime(TimerModel timer, int delta) {
            try {
                int val = Integer.parseInt(etTimeInput.getText().toString());
                val += delta;
                if(val < 1) val = 1; // Min 1
                etTimeInput.setText(String.valueOf(val)); // This triggers text watcher -> saves pref
            } catch (NumberFormatException e) {
                etTimeInput.setText("90");
            }
        }

        private void startTimer(TimerModel timer) {
            String input = etTimeInput.getText().toString();
            if (input.isEmpty()) {
                input = "90"; // Default if empty
            }

            long durationMillis = 0;
            try {
                int val = Integer.parseInt(input);
                int unitIdx = spinnerUnit.getSelectedItemPosition();
                // 0: Seconds, 1: Minutes, 2: Hours
                if (unitIdx == 0) durationMillis = val * 1000L;
                else if (unitIdx == 1) durationMillis = val * 60 * 1000L;
                else if (unitIdx == 2) durationMillis = val * 60 * 60 * 1000L;

            } catch (NumberFormatException e) {
                return;
            }

            if (durationMillis == 0) return;

            timer.totalTime = durationMillis;
            timer.remainingTime = durationMillis;
            timer.endTimeMillis = System.currentTimeMillis() + durationMillis;
            timer.isRunning = true;

            // Persist for background accuracy
            android.content.SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
            prefs.edit()
                .putLong("timer_" + timer.index + "_end_time", timer.endTimeMillis)
                .putLong("timer_" + timer.index + "_total_time", timer.totalTime)
                .apply();

            // Schedule System Alarm
            scheduleAlarm(timer);

            // UI Update
            inputContainer.setVisibility(View.GONE);
            runningContainer.setVisibility(View.VISIBLE);

            startTicker(timer);
        }

        private void scheduleAlarm(TimerModel timer) {
            android.content.Context context = itemView.getContext();
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(android.content.Context.ALARM_SERVICE);
            
            android.content.Intent intent = new android.content.Intent(context, TimerReceiver.class);
            intent.putExtra("timer_index", timer.index);
            
            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                context, timer.index, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, timer.endTimeMillis, pendingIntent);
            } else {
                alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, timer.endTimeMillis, pendingIntent);
            }
        }

        private void cancelAlarm(int index) {
            android.content.Context context = itemView.getContext();
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(android.content.Context.ALARM_SERVICE);
            android.content.Intent intent = new android.content.Intent(context, TimerReceiver.class);
            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                context, index, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
        }

        private void startTicker(TimerModel timer) {
            if (countDownTimer != null) countDownTimer.cancel();
            
            // If it was "Restart", set it back to "Start" when ticker runs (if it's not already)
            btnStartStop.setText(R.string.start);

            // We use the remaining time calculated at bind or start
            countDownTimer = new CountDownTimer(timer.remainingTime, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timer.remainingTime = millisUntilFinished;
                    
                    // Update Text
                    long seconds = millisUntilFinished / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;
                    
                    String timeStr;
                    if (hours > 0) timeStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
                    else timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds % 60);
                    
                    tvCountdown.setText(timeStr);

                    // Update Circle
                    float progress = (float) millisUntilFinished / timer.totalTime;
                    circleVisualizer.setProgress(progress);
                }

                @Override
                public void onFinish() {
                    stopTimer(timer, false); // Not a manual stop
                    tvCountdown.setText("Done!");
                    circleVisualizer.setProgress(0f);
                    btnStartStop.setText(R.string.restart); // Change to Restart on finish
                }
            }.start();
        }

        private void stopTimer(TimerModel timer, boolean isManualStop) {
            timer.isRunning = false;
            timer.endTimeMillis = 0;
            
            // Clear persistence
            android.content.SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
            prefs.edit().putLong("timer_" + timer.index + "_end_time", 0).apply();

            if (isManualStop) {
                cancelAlarm(timer.index);
            }

            if (countDownTimer != null) countDownTimer.cancel();
            
            inputContainer.setVisibility(View.VISIBLE);
            runningContainer.setVisibility(View.GONE);
            circleVisualizer.setProgress(1.0f);
            
            // btnStartStop text usually remains "Start" if stopped manually
            // But if it was "Restart" from a previous finish, we leave it.
            // Actually, if we just stopped it, let's keep the current text or reset if needed.
            // Requirement says "When a timer is finished the button should say 'Restart'".
        }
    }


    // Simple Model to hold state
    static class TimerModel {
        int index;
        long totalTime;
        long remainingTime;
        long endTimeMillis; // 0 if not running
        boolean isRunning;
        String inputValue;
        int unitIndex;
    }
}
