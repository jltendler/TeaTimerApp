package com.example.multibrewtimer;

import android.os.CountDownTimer;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.TimerViewHolder> {

    private final List<TimerModel> timers;

    public TimerAdapter() {
        this(6); // Default 6 timers
    }

    public TimerAdapter(int count) {
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
        Button btnStartStop, btnPause, btnResume, btnReset;
        Button btnMinusRunning, btnPlusRunning;
        TextView tvCountdown;
        CircleTimerView circleVisualizer;
        LinearLayout inputContainer, runningContainer;

        CountDownTimer countDownTimer;

        public TimerViewHolder(@NonNull View itemView) {
            super(itemView);
            etTimeInput = itemView.findViewById(R.id.etTimeInput);
            spinnerUnit = itemView.findViewById(R.id.spinnerUnit);
            btnStartStop = itemView.findViewById(R.id.btnStartStop);
            
            btnPause = itemView.findViewById(R.id.btnPause);
            btnResume = itemView.findViewById(R.id.btnResume);
            btnReset = itemView.findViewById(R.id.btnReset);

            btnMinusRunning = itemView.findViewById(R.id.btnMinusRunning);
            btnPlusRunning = itemView.findViewById(R.id.btnPlusRunning);

            tvCountdown = itemView.findViewById(R.id.tvCountdown);
            circleVisualizer = itemView.findViewById(R.id.circleVisualizer);
            inputContainer = itemView.findViewById(R.id.inputContainer);
            runningContainer = itemView.findViewById(R.id.runningContainer);
        }

        void bind(TimerModel timer) {
            android.content.SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
            
            // Theme Logic
            int themeIndex = prefs.getInt("pref_timer_theme", 0);
            int resolvedColor = ThemeHelper.getColor(themeIndex, timer.index % 6);
            
            circleVisualizer.setColor(resolvedColor);
            
            Button btnPlus = itemView.findViewById(R.id.btnPlus);
            Button btnMinus = itemView.findViewById(R.id.btnMinus);
            
            android.content.res.ColorStateList csl = android.content.res.ColorStateList.valueOf(resolvedColor);
            btnStartStop.setBackgroundTintList(csl);
            btnPause.setBackgroundTintList(csl);
            btnResume.setBackgroundTintList(csl);
            btnReset.setBackgroundTintList(csl);
            btnPlus.setBackgroundTintList(csl);
            btnMinus.setBackgroundTintList(csl);
            btnPlusRunning.setBackgroundTintList(csl);
            btnMinusRunning.setBackgroundTintList(csl);

            tvCountdown.setTextColor(resolvedColor);
            etTimeInput.setTextColor(resolvedColor);

            // Restore State
            if (timer.inputValue == null) {
                // Initial Load
                String savedVal = prefs.getString("timer_" + timer.index + "_val", "");
                if (savedVal.isEmpty()) {
                    timer.inputValue = "90";
                    timer.unitIndex = 0;
                } else {
                    timer.inputValue = savedVal;
                    timer.unitIndex = prefs.getInt("timer_" + timer.index + "_unit", 0);
                }
                
                timer.endTimeMillis = prefs.getLong("timer_" + timer.index + "_end_time", 0);
                timer.totalTime = prefs.getLong("timer_" + timer.index + "_total_time", 0);
                timer.isPaused = prefs.getBoolean("timer_" + timer.index + "_is_paused", false);
                timer.remainingTime = prefs.getLong("timer_" + timer.index + "_remaining", 0);

                if (timer.isPaused) {
                    // Paused State
                    timer.isRunning = false;
                    timer.isFinished = false;
                } else if (timer.endTimeMillis > 0) {
                    if (timer.endTimeMillis > System.currentTimeMillis()) {
                        // Running State
                        timer.isRunning = true;
                        timer.remainingTime = timer.endTimeMillis - System.currentTimeMillis();
                        timer.isFinished = false;
                    } else {
                        // Finished while backgrounded
                        timer.isRunning = false;
                        timer.isFinished = true;
                        timer.endTimeMillis = 0;
                    }
                } else {
                    // Idle State
                    timer.isRunning = false;
                    timer.isFinished = false;
                }
            }

            updateUI(timer);

            // Input Listeners
            if (timer.inputValue != null) etTimeInput.setText(timer.inputValue);
            
            etTimeInput.addTextChangedListener(new TextWatcher() {
                 @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                 @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                 @Override public void afterTextChanged(Editable s) { 
                     timer.inputValue = s.toString();
                     prefs.edit().putString("timer_" + timer.index + "_val", timer.inputValue).apply();
                 }
            });
            
            int incrementStep = prefs.getInt("pref_increment_step", 5);
            btnPlus.setOnClickListener(v -> adjustTime(timer, incrementStep));
            btnMinus.setOnClickListener(v -> adjustTime(timer, -incrementStep));
            
            spinnerUnit.setOnItemSelectedListener(null);
            spinnerUnit.setSelection(timer.unitIndex);
            spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { 
                    timer.unitIndex = position;
                    prefs.edit().putInt("timer_" + timer.index + "_unit", position).apply();
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Control Listeners
            btnStartStop.setOnClickListener(v -> startTimer(timer));
            btnPause.setOnClickListener(v -> pauseTimer(timer));
            btnResume.setOnClickListener(v -> resumeTimer(timer));
            btnReset.setOnClickListener(v -> resetTimer(timer));

            int adjustStep = prefs.getInt("pref_increment_step", 5);
            btnPlusRunning.setOnClickListener(v -> adjustRemainingTime(timer, adjustStep));
            btnMinusRunning.setOnClickListener(v -> adjustRemainingTime(timer, -adjustStep));
        }

        private void updateUI(TimerModel timer) {
            if (timer.isFinished) {
                // FINISHED State
                inputContainer.setVisibility(View.GONE);
                runningContainer.setVisibility(View.VISIBLE);
                
                tvCountdown.setText("Done");
                circleVisualizer.setProgress(0f);
                
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.GONE);
                btnReset.setVisibility(View.VISIBLE); // Show Reset

                btnPlusRunning.setVisibility(View.GONE);
                btnMinusRunning.setVisibility(View.GONE);
                
                if (countDownTimer != null) countDownTimer.cancel();

            } else if (timer.isRunning) {
                // RUNNING State
                inputContainer.setVisibility(View.GONE);
                runningContainer.setVisibility(View.VISIBLE);
                
                btnPause.setVisibility(View.VISIBLE); // Pause only
                btnResume.setVisibility(View.GONE);
                btnReset.setVisibility(View.GONE);

                btnPlusRunning.setVisibility(View.VISIBLE);
                btnMinusRunning.setVisibility(View.VISIBLE);

                startTicker(timer);

            } else if (timer.isPaused) {
                // PAUSED State
                inputContainer.setVisibility(View.GONE);
                runningContainer.setVisibility(View.VISIBLE);

                UpdateTickerText(timer.remainingTime); // Static Update
                float progress = (float) timer.remainingTime / timer.totalTime;
                circleVisualizer.setProgress(progress);

                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.VISIBLE); // Resume
                btnReset.setVisibility(View.VISIBLE);  // Reset

                btnPlusRunning.setVisibility(View.VISIBLE);
                btnMinusRunning.setVisibility(View.VISIBLE);

                if (countDownTimer != null) countDownTimer.cancel();
            } else {
                // IDLE State
                inputContainer.setVisibility(View.VISIBLE);
                runningContainer.setVisibility(View.GONE);
                circleVisualizer.setProgress(1.0f);
                
                if (countDownTimer != null) countDownTimer.cancel();
            }
        }

        private void adjustTime(TimerModel timer, int delta) {
            try {
                int val = Integer.parseInt(etTimeInput.getText().toString());
                val += delta;
                if(val < 1) val = 1;
                etTimeInput.setText(String.valueOf(val));
            } catch (NumberFormatException e) {
                etTimeInput.setText("90");
            }
        }

        private void startTimer(TimerModel timer) {
            String input = etTimeInput.getText().toString();
            if (input.isEmpty()) input = "90";
            
            long durationMillis = 0;
            try {
                int val = Integer.parseInt(input);
                int unitIdx = spinnerUnit.getSelectedItemPosition();
                if (unitIdx == 0) durationMillis = val * 1000L;
                else if (unitIdx == 1) durationMillis = val * 60 * 1000L;
                else if (unitIdx == 2) durationMillis = val * 60 * 60 * 1000L;
            } catch (NumberFormatException e) { return; }

            if (durationMillis == 0) return;

            timer.totalTime = durationMillis;
            timer.remainingTime = durationMillis;
            timer.endTimeMillis = System.currentTimeMillis() + durationMillis;
            timer.isRunning = true;
            timer.isPaused = false;
            timer.isFinished = false;

            saveState(timer);
            scheduleAlarm(timer);
            updateUI(timer);
        }

        private void pauseTimer(TimerModel timer) {
            timer.isRunning = false;
            timer.isPaused = true;
            // remainingTime is already updated by the ticker's last tick
            // but we should save it
            
            cancelAlarm(timer.index); // No alarm while paused
            saveState(timer);
            updateUI(timer);
        }

        private void resumeTimer(TimerModel timer) {
            timer.isRunning = true;
            timer.isPaused = false;
            timer.endTimeMillis = System.currentTimeMillis() + timer.remainingTime;
            
            saveState(timer);
            scheduleAlarm(timer);
            updateUI(timer);
        }

        private void stopTimer(TimerModel timer, boolean isManual) {
            timer.isRunning = false;
            timer.isPaused = false;
            timer.isFinished = false;
            timer.endTimeMillis = 0;
            
            if (isManual) cancelAlarm(timer.index);
            
            saveState(timer);
            updateUI(timer);
        }

        private void resetTimer(TimerModel timer) {
            // Reset to Idle
            timer.isRunning = false;
            timer.isPaused = false;
            timer.isFinished = false;
            saveState(timer);
            updateUI(timer);
        }
        
        private void finishTimer(TimerModel timer) {
            timer.isRunning = false;
            timer.isFinished = true;
            timer.endTimeMillis = 0;
            
            cancelAlarm(timer.index); // Prevent redundant AlarmManager trigger if we are here via CountDownTimer

            if (MainActivity.isAppVisible) {
                SoundHelper.playRingtone(itemView.getContext());
            }

            saveState(timer);
            updateUI(timer);
        }

        private void adjustRemainingTime(TimerModel timer, int deltaSeconds) {
            long deltaMillis = deltaSeconds * 1000L;
            timer.remainingTime += deltaMillis;
            if (timer.remainingTime < 1000) {
                timer.remainingTime = 0;
                finishTimer(timer);
                return;
            }

            // Update totalTime if remaining exceeds it to keep circle visual sane-ish
            if (timer.remainingTime > timer.totalTime) {
                timer.totalTime = timer.remainingTime;
            }

            if (timer.isRunning) {
                // Shift endTimeMillis forward/backward
                timer.endTimeMillis = System.currentTimeMillis() + timer.remainingTime;
                scheduleAlarm(timer);
                // restartTicker occurs inside updateUI -> startTicker
            }

            saveState(timer);
            updateUI(timer);
        }

        private void saveState(TimerModel timer) {
            android.content.SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
            prefs.edit()
                .putLong("timer_" + timer.index + "_end_time", timer.endTimeMillis)
                .putLong("timer_" + timer.index + "_total_time", timer.totalTime)
                .putLong("timer_" + timer.index + "_remaining", timer.remainingTime)
                .putBoolean("timer_" + timer.index + "_is_paused", timer.isPaused)
                .apply();
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

        private void UpdateTickerText(long millisUntilFinished) {
            long seconds = millisUntilFinished / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            
            String timeStr;
            if (hours > 0) timeStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
            else timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds % 60);
            tvCountdown.setText(timeStr);
        }

        private void startTicker(TimerModel timer) {
            if (countDownTimer != null) countDownTimer.cancel();
            
            countDownTimer = new CountDownTimer(timer.remainingTime, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timer.remainingTime = millisUntilFinished;
                    UpdateTickerText(millisUntilFinished);
                    float progress = (float) millisUntilFinished / timer.totalTime;
                    circleVisualizer.setProgress(progress);
                }

                @Override
                public void onFinish() {
                    finishTimer(timer);
                }
            }.start();
        }
    }

    static class TimerModel {
        int index;
        long totalTime;
        long remainingTime;
        long endTimeMillis;
        boolean isRunning;
        boolean isPaused;
        boolean isFinished;
        String inputValue;
        int unitIndex;
    }
}
