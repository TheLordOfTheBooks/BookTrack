package com.example.booktrack;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class TimerFragment extends Fragment {

    private EditText timeInput;
    private Button startButton, cancelButton;
    private TextView countdownText;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    EditText secondsInput, minutesInput, hoursInput;
    private boolean shouldStartTimerAfterPermission = false;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration alarmListener;
    private Button stopSoundButton;

    public TimerFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_timer_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        startButton = view.findViewById(R.id.start_timer_button);
        cancelButton = view.findViewById(R.id.cancel_timer_button);
        countdownText = view.findViewById(R.id.countdown_text);
        hoursInput = view.findViewById(R.id.hours_input);
        minutesInput = view.findViewById(R.id.minutes_input);
        secondsInput = view.findViewById(R.id.seconds_input);

        startButton.setOnClickListener(v -> startTimer());
        cancelButton.setOnClickListener(v -> cancelTimer());
        createNotificationChannel();

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        listenToAndCleanExpiredAlarms();

        stopSoundButton = view.findViewById(R.id.stop_sound_button);

        stopSoundButton.setOnClickListener(v -> {
            Intent stopIntent = new Intent(requireContext(), TimerService.class);
            stopIntent.setAction(TimerService.ACTION_STOP);
            requireContext().stopService(stopIntent);
            stopSoundButton.setVisibility(View.GONE);
        });


    }

    private void startTimer() {
        int hours = parseTimeInput(hoursInput.getText().toString());
        int minutes = parseTimeInput(minutesInput.getText().toString());
        int seconds = parseTimeInput(secondsInput.getText().toString());
        stopSoundButton.setVisibility(View.GONE);

        long totalMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L;

        if (totalMillis <= 0) {
            countdownText.setText("Please enter valid time");
            return;
        }

        timeLeftInMillis = totalMillis;
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                long hrs = millisUntilFinished / (1000 * 60 * 60);
                long mins = (millisUntilFinished / (1000 * 60)) % 60;
                long secs = (millisUntilFinished / 1000) % 60;
                countdownText.setText(String.format("%02d:%02d:%02d", hrs, mins, secs));
            }

            public void onFinish() {
                countdownText.setText("Time's up!");
                stopSoundButton.setVisibility(View.VISIBLE);
            }
        }.start();

        startForegroundTimer(timeLeftInMillis); // ✅ this was missing
    }

    private int parseTimeInput(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countdownText.setText("Canceled");
        }

        Intent stopIntent = new Intent(requireContext(), TimerService.class);
        requireContext().stopService(stopIntent);
    }


    private void startForegroundTimer(long millis) {
        requestNotificationPermission();
        Intent serviceIntent = new Intent(requireContext(), TimerService.class);
        serviceIntent.putExtra(TimerService.EXTRA_DURATION, millis);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "timer_channel_id",
                    "Timer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifies when countdown is done");

            NotificationManager manager = requireContext().getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) startTimer();
                        }
                );
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Notification permission granted", Toast.LENGTH_SHORT).show();
                if (shouldStartTimerAfterPermission) {
                    startTimer();
                    shouldStartTimerAfterPermission = false;
                }
            } else {
                Toast.makeText(getContext(), "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void listenToAndCleanExpiredAlarms() {
        if (currentUser == null) return;

        alarmListener = db.collection("users")
                .document(currentUser.getUid())
                .collection("alarms")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("TimerFragment", "Failed to load alarms", error);
                        return;
                    }

                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            AlarmItem alarm = doc.toObject(AlarmItem.class);
                            if (alarm.getTriggerMillis() < System.currentTimeMillis()) {
                                doc.getReference().delete()
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("TimerFragment", "Deleted expired alarm: " + alarm.getAlarmId()))
                                        .addOnFailureListener(e ->
                                                Log.e("TimerFragment", "Failed to delete expired alarm", e));
                            }
                        }
                    }
                });
    }

    private void scheduleTimerAlarm(long durationMillis) {
        long triggerAtMillis = System.currentTimeMillis() + durationMillis;

        Intent intent = new Intent(requireContext(), TimerReceiver.class);
        intent.setAction("TIMER_ALARM");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                1002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(settingsIntent);
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }


}