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

/**
 * Fragment that provides countdown timer functionality for reading sessions in the BookTrack application.
 * This fragment offers a comprehensive timer interface with foreground service integration,
 * notification management, and automatic cleanup of expired alarms.
 *
 * <p>The TimerFragment serves as a dedicated tool for users to track their reading time
 * with precise countdown functionality, background operation support, and integration
 * with the broader alarm and notification system.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Flexible time input with hours, minutes, and seconds configuration</li>
 *   <li>Real-time countdown display with formatted time representation</li>
 *   <li>Foreground service integration for background timer operation</li>
 *   <li>Notification channel management and permission handling</li>
 *   <li>System alarm scheduling for precise timer completion alerts</li>
 *   <li>Automatic cleanup of expired alarms from Firebase Firestore</li>
 *   <li>Sound control capabilities for timer completion notifications</li>
 * </ul></p>
 *
 * <p>The fragment handles Android version-specific requirements including notification
 * permissions for Android 13+, exact alarm scheduling for Android 12+, and proper
 * foreground service management for reliable background operation.</p>
 *
 * <p>Integration with Firebase Firestore provides automatic cleanup of expired alarms,
 * maintaining database hygiene and ensuring optimal application performance.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class TimerFragment extends Fragment {

    /** EditText for time input (legacy field, use specific time inputs instead) */
    private EditText timeInput;

    /** Button to start the countdown timer */
    private Button startButton;

    /** Button to cancel the running timer */
    private Button cancelButton;

    /** TextView for displaying the countdown in real-time */
    private TextView countdownText;

    /** Android CountDownTimer for managing the countdown functionality */
    private CountDownTimer countDownTimer;

    /** Remaining time in milliseconds for the current timer */
    private long timeLeftInMillis;

    /** EditText for seconds input in the timer configuration */
    EditText secondsInput;

    /** EditText for minutes input in the timer configuration */
    EditText minutesInput;

    /** EditText for hours input in the timer configuration */
    EditText hoursInput;

    /** Flag indicating whether to start timer after permission is granted */
    private boolean shouldStartTimerAfterPermission = false;

    /** Firebase Firestore instance for alarm cleanup operations */
    private FirebaseFirestore db;

    /** Current authenticated Firebase user */
    private FirebaseUser currentUser;

    /** Firebase Firestore listener registration for alarm monitoring */
    private ListenerRegistration alarmListener;

    /** Button to stop timer completion sound/notification */
    private Button stopSoundButton;

    /**
     * Default constructor for TimerFragment.
     * Required for proper fragment instantiation by the Android framework.
     */
    public TimerFragment() {}

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views
     * @param container          The parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_timer_fragment, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, providing access to the created view hierarchy.
     * This method initializes all UI components, sets up event listeners, creates notification channels,
     * and begins monitoring expired alarms.
     *
     * <p>The initialization process includes:
     * <ul>
     *   <li>UI component binding and event listener setup</li>
     *   <li>Notification channel creation for timer alerts</li>
     *   <li>Firebase Firestore and Authentication initialization</li>
     *   <li>Expired alarm monitoring and cleanup setup</li>
     *   <li>Sound control button configuration</li>
     * </ul></p>
     *
     * @param view               The View returned by onCreateView()
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
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

    /**
     * Starts the countdown timer with user-specified duration and initiates foreground service.
     * This method validates user input, creates the countdown timer, and starts the background
     * timer service for reliable operation even when the app is not in foreground.
     *
     * <p>The timer startup process includes:
     * <ul>
     *   <li>Input validation and time calculation from hours, minutes, and seconds</li>
     *   <li>CountDownTimer creation with real-time display updates</li>
     *   <li>Foreground service initiation for background operation</li>
     *   <li>UI state management for timer controls</li>
     * </ul></p>
     *
     * <p>The timer displays time in HH:MM:SS format and updates every second.
     * Upon completion, it shows "Time's up!" and reveals the sound control button.</p>
     */
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

    /**
     * Parses time input from string to integer with error handling.
     * This utility method safely converts user input to numeric values,
     * returning 0 for invalid or empty input.
     *
     * @param input The string input to parse
     * @return The parsed integer value, or 0 if parsing fails
     */
    private int parseTimeInput(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Cancels the currently running timer and stops the associated foreground service.
     * This method provides a clean way to abort timer operation and free system resources.
     *
     * <p>The cancellation process includes:
     * <ul>
     *   <li>CountDownTimer cancellation and cleanup</li>
     *   <li>UI update to show cancellation status</li>
     *   <li>Foreground service termination</li>
     * </ul></p>
     */
    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countdownText.setText("Canceled");
        }

        Intent stopIntent = new Intent(requireContext(), TimerService.class);
        requireContext().stopService(stopIntent);
    }

    /**
     * Starts the foreground timer service for background operation.
     * This method handles notification permission requests and initiates the
     * TimerService with the specified duration for reliable background timing.
     *
     * @param millis The timer duration in milliseconds
     */
    private void startForegroundTimer(long millis) {
        requestNotificationPermission();
        Intent serviceIntent = new Intent(requireContext(), TimerService.class);
        serviceIntent.putExtra(TimerService.EXTRA_DURATION, millis);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }

    /**
     * Creates the notification channel for timer notifications on Android 8.0+.
     * This method ensures proper notification delivery by setting up the required
     * notification channel with appropriate importance and description.
     *
     * <p>The notification channel is configured with:
     * <ul>
     *   <li>High importance for immediate user attention</li>
     *   <li>Descriptive name and explanation for user understanding</li>
     *   <li>Proper integration with system notification management</li>
     * </ul></p>
     */
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

    /**
     * Requests notification permission for Android 13+ devices.
     * This method handles the modern permission request flow required for
     * notification delivery on newer Android versions.
     *
     * <p>The permission request includes:
     * <ul>
     *   <li>Android version compatibility checking</li>
     *   <li>Current permission status validation</li>
     *   <li>ActivityResultLauncher-based permission request</li>
     *   <li>Automatic timer restart upon permission grant</li>
     * </ul></p>
     */
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

    /**
     * Handles the result of permission requests (legacy callback method).
     * This method provides fallback permission handling for older Android versions
     * and ensures proper timer operation after permission grants.
     *
     * @param requestCode  The request code passed to requestPermissions()
     * @param permissions  The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
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

    /**
     * Sets up real-time monitoring and cleanup of expired alarms from Firebase Firestore.
     * This method implements automatic database hygiene by removing alarms that have
     * passed their deadline, maintaining optimal application performance.
     *
     * <p>The monitoring process includes:
     * <ul>
     *   <li>User authentication validation</li>
     *   <li>Real-time snapshot listener setup on user's alarm collection</li>
     *   <li>Automatic detection of expired alarms by comparing deadline with current time</li>
     *   <li>Immediate deletion of expired alarms from Firestore</li>
     *   <li>Comprehensive logging for debugging and monitoring</li>
     * </ul></p>
     *
     * <p>This automatic cleanup ensures that the alarm system remains efficient
     * and prevents accumulation of outdated alarm data in the database.</p>
     */
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
                            if (alarm.getDeadlineMillis() < System.currentTimeMillis()) {
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

    /**
     * Schedules a system alarm for timer completion notification.
     * This method creates a precise system alarm that will trigger when the timer
     * duration expires, ensuring reliable notification delivery even if the app
     * is terminated or the device is in deep sleep.
     *
     * <p>The alarm scheduling process includes:
     * <ul>
     *   <li>Deadline calculation based on current time and timer duration</li>
     *   <li>PendingIntent creation for TimerReceiver broadcast</li>
     *   <li>Android 12+ exact alarm permission validation</li>
     *   <li>Version-appropriate alarm scheduling method selection</li>
     *   <li>Wake-up capability for reliable delivery during device idle</li>
     * </ul></p>
     *
     * <p>The method handles Android version differences by using appropriate
     * alarm scheduling methods and permission requirements for each API level.</p>
     *
     * @param durationMillis The timer duration in milliseconds
     */
    private void scheduleTimerAlarm(long durationMillis) {
        long deadlineMillis = System.currentTimeMillis() + durationMillis;

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
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadlineMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, deadlineMillis, pendingIntent);
        }
    }
}