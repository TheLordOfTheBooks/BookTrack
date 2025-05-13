package com.example.booktrack;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class TimerFragment extends Fragment {

    private EditText timeInput;
    private Button startButton, cancelButton;
    private TextView countdownText;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    EditText secondsInput, minutesInput, hoursInput;
    private boolean shouldStartTimerAfterPermission = false;

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
    }

    private void startTimer() {

        int hours = parseTimeInput(hoursInput.getText().toString());
        int minutes = parseTimeInput(minutesInput.getText().toString());
        int seconds = parseTimeInput(secondsInput.getText().toString());



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

                NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "timer_channel_id")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Time's Up!")
                        .setContentText("Your Time For Reading Has Ended.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                    shouldStartTimerAfterPermission = true;
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                    return;
                }

                notificationManager.notify(1001, builder.build());
            }
        }.start();

        startForegroundTimer(timeLeftInMillis);
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
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
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


}