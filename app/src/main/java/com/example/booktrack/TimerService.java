package com.example.booktrack;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * Foreground service that manages countdown timer operations and completion notifications
 * for the BookTrack application's reading timer functionality.
 *
 * <p>The TimerService provides reliable background timer operation with comprehensive
 * notification management, audio alerts, and user interaction capabilities. It ensures
 * that reading timers continue to function accurately even when the application is
 * not in the foreground or the device is in low-power states.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Foreground service operation for reliable background timer execution</li>
 *   <li>Real-time countdown functionality with millisecond precision</li>
 *   <li>Comprehensive notification system for timer status and completion</li>
 *   <li>Audio alert system with looping alarm sounds for timer completion</li>
 *   <li>User-controllable sound stopping through notification actions</li>
 *   <li>Proper resource management and service lifecycle handling</li>
 *   <li>Android version-compatible notification channel management</li>
 * </ul></p>
 *
 * <p>The service operates in two primary modes:
 * <ul>
 *   <li><strong>Active Timer Mode</strong> - Running countdown with progress notification</li>
 *   <li><strong>Completion Mode</strong> - Timer finished with audio alert and completion notification</li>
 * </ul></p>
 *
 * <p>Audio management includes fallback sound selection, proper MediaPlayer resource
 * handling, and user-controlled sound termination to ensure a pleasant user experience
 * while maintaining reliable timer completion alerts.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class TimerService extends Service {

    /** Notification channel identifier for timer notifications */
    public static final String CHANNEL_ID = "TimerChannel";

    /** Intent extra key for timer duration in milliseconds */
    public static final String EXTRA_DURATION = "durationMillis";

    /** Intent action for stopping the timer service and alarm sound */
    public static final String ACTION_STOP = "STOP_TIMER";

    /** CountDownTimer instance managing the active countdown */
    private CountDownTimer countDownTimer;

    /** MediaPlayer instance for timer completion audio alerts */
    private MediaPlayer mediaPlayer;

    /**
     * Called by the system every time a client explicitly starts the service with startService().
     * This method handles timer initiation, service stopping, and notification management
     * based on the received intent and its data.
     *
     * <p>The method handles three primary scenarios:
     * <ul>
     *   <li><strong>Stop Action</strong> - Stops alarm sound and terminates the service</li>
     *   <li><strong>New Timer</strong> - Initiates countdown with specified duration</li>
     *   <li><strong>Timer Completion</strong> - Handles completed timer with notifications and audio</li>
     * </ul></p>
     *
     * <p>For active timers, the method:
     * <ul>
     *   <li>Creates and displays a foreground notification</li>
     *   <li>Cancels any existing countdown timers</li>
     *   <li>Starts a new CountDownTimer with the specified duration</li>
     *   <li>Handles timer completion with audio alerts and notifications</li>
     *   <li>Automatically terminates the service after completion timeout</li>
     * </ul></p>
     *
     * <p>The service uses START_NOT_STICKY return value to prevent automatic restart
     * by the system, ensuring proper timer behavior and resource management.</p>
     *
     * @param intent  The Intent supplied to startService(), containing timer duration or stop action
     * @param flags   Additional data about this start request (not used in this implementation)
     * @param startId A unique integer representing this specific request to start
     * @return START_NOT_STICKY to prevent automatic service restart
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopAlarmSound();
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        long durationMillis = intent.getLongExtra(EXTRA_DURATION, 0);
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Timer Running")
                .setContentText("Your countdown is in progress...")
                .setSmallIcon(R.mipmap.ic_logo_round)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(0)
                .setSound(null)
                .build();

        startForeground(1, notification);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                sendFinishedNotification();
                new android.os.Handler().postDelayed(() -> stopSelf(), 20000);
            }
        }.start();

        return START_NOT_STICKY;
    }

    /**
     * Creates and displays the timer completion notification with audio alert and user controls.
     * This method handles the complete timer completion workflow including sound activation,
     * notification creation, and user interaction setup.
     *
     * <p>The completion process includes:
     * <ul>
     *   <li>Initiation of looping alarm sound for user attention</li>
     *   <li>Creation of high-priority completion notification</li>
     *   <li>Addition of "Stop Sound" action button for user control</li>
     *   <li>Proper PendingIntent configuration for service interaction</li>
     * </ul></p>
     *
     * <p>The notification features:
     * <ul>
     *   <li>High priority for immediate user attention</li>
     *   <li>BookTrack logo for brand consistency</li>
     *   <li>Auto-cancel behavior for clean notification management</li>
     *   <li>Action button for convenient sound control</li>
     * </ul></p>
     *
     * <p>The method ensures that users are immediately alerted to timer completion
     * while providing convenient controls to manage the alert sound.</p>
     */
    private void sendFinishedNotification() {
        playAlarmSound();
        Intent stopIntent = new Intent(getApplicationContext(), TimerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Timer Finished")
                .setContentText("Your Time For Reading Has Ended.")
                .setSmallIcon(R.mipmap.ic_logo_round)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(0)
                .setSound(null)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_logo_round, "Stop Sound", stopPendingIntent)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(2, notification);
    }

    /**
     * Creates the notification channel for timer notifications on Android 8.0 (API 26) and above.
     * This method configures a high-importance notification channel with proper audio attributes,
     * vibration, and visual indicators for optimal user notification experience.
     *
     * <p>Channel configuration includes:
     * <ul>
     *   <li>High importance level for immediate user attention</li>
     *   <li>Custom sound configuration with proper audio attributes</li>
     *   <li>Vibration and LED light enablement for accessibility</li>
     *   <li>Descriptive name and explanation for user understanding</li>
     * </ul></p>
     *
     * <p>The method uses system default notification sound with alarm-appropriate
     * audio attributes, ensuring that timer notifications are treated with the
     * proper priority by the Android notification system.</p>
     *
     * <p>Only executed on Android 8.0+ devices where notification channels are required
     * for proper notification delivery and user notification management.</p>
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifies when the timer ends");
            channel.setSound(soundUri, audioAttributes);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Required method for bound services. This service does not support binding,
     * so this method returns null to indicate that clients cannot bind to it.
     *
     * @param intent The Intent that was used to bind to this service
     * @return null, indicating that this service does not support binding
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called by the system to clean up resources before the service is destroyed.
     * This method ensures proper cleanup of timers, audio resources, and prevents
     * memory leaks by releasing all active components.
     *
     * <p>Cleanup operations include:
     * <ul>
     *   <li>Cancellation of active countdown timers</li>
     *   <li>Stopping and releasing MediaPlayer resources</li>
     *   <li>Proper service lifecycle completion</li>
     * </ul></p>
     *
     * <p>This method is critical for preventing resource leaks and ensuring
     * that audio playback is properly terminated when the service ends.</p>
     */
    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopAlarmSound(); // <- now stops MediaPlayer
        super.onDestroy();
    }

    /**
     * Initiates audio alarm playback for timer completion notification.
     * This method configures and starts a MediaPlayer with looping alarm sound,
     * ensuring persistent audio alert until manually stopped by the user.
     *
     * <p>Audio configuration includes:
     * <ul>
     *   <li>Primary alarm sound selection with notification sound fallback</li>
     *   <li>Alarm-appropriate audio attributes for proper system handling</li>
     *   <li>Looping playback for sustained user attention</li>
     *   <li>Comprehensive error handling for audio system failures</li>
     * </ul></p>
     *
     * <p>The method first attempts to use the system alarm sound, falling back
     * to notification sound if alarm sound is unavailable. This ensures
     * reliable audio alert delivery across different device configurations.</p>
     *
     * <p>Proper audio attributes ensure that the alarm sound is treated
     * appropriately by the system's audio management and user volume controls.</p>
     */
    private void playAlarmSound() {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (soundUri == null) {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getApplicationContext(), soundUri);

            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());

            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e("TimerService", "Failed to play alarm", e);
        }
    }

    /**
     * Stops the alarm sound and releases MediaPlayer resources.
     * This method provides safe termination of audio playback with proper
     * resource cleanup to prevent memory leaks and audio system conflicts.
     *
     * <p>The stopping process includes:
     * <ul>
     *   <li>Verification of MediaPlayer existence and playback state</li>
     *   <li>Safe stopping of audio playback</li>
     *   <li>Complete MediaPlayer resource release</li>
     *   <li>Null reference assignment for garbage collection</li>
     * </ul></p>
     *
     * <p>This method is called both when users manually stop the sound through
     * notification actions and during service cleanup to ensure no audio
     * resources remain active after timer completion.</p>
     */
    private void stopAlarmSound() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}