package com.example.booktrack;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * BroadcastReceiver that handles scheduled reading alarms for the BookTrack application.
 * This receiver is triggered by the Android AlarmManager when a reading reminder is due,
 * and handles the complete alarm processing workflow including notification display
 * and automatic cleanup.
 *
 * <p>The receiver performs critical alarm management functions including:
 * <ul>
 *   <li>Notification permission validation for Android 13+ devices</li>
 *   <li>High-priority notification display with BookTrack branding</li>
 *   <li>Automatic alarm deletion from Firebase Firestore after triggering</li>
 *   <li>Comprehensive error handling and logging for debugging</li>
 *   <li>User session validation through SharedPreferences</li>
 * </ul></p>
 *
 * <p>This receiver integrates with the Android notification system to ensure users
 * receive timely reading reminders, and maintains database hygiene by automatically
 * removing triggered alarms to prevent duplicate notifications.</p>
 *
 * <p>The receiver is designed to work reliably in background scenarios and handles
 * various edge cases including missing permissions, invalid user sessions, and
 * Firebase connectivity issues.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast from the AlarmManager.
     * This method handles the complete alarm processing workflow including permission validation,
     * notification display, and automatic alarm cleanup.
     *
     * <p>The processing workflow includes:
     * <ul>
     *   <li>Notification permission validation for Android 13+ (API 33+) devices</li>
     *   <li>Extraction of alarm message and ID from the triggering Intent</li>
     *   <li>Creation and display of a high-priority BookTrack notification</li>
     *   <li>Firebase initialization and user session validation</li>
     *   <li>Automatic deletion of the triggered alarm from Firestore</li>
     *   <li>Comprehensive logging for successful operations and errors</li>
     * </ul></p>
     *
     * <p>Notification features:
     * <ul>
     *   <li>BookTrack logo as the notification icon</li>
     *   <li>High priority for immediate user attention</li>
     *   <li>Auto-cancel behavior for clean notification management</li>
     *   <li>Custom channel ID for notification categorization</li>
     * </ul></p>
     *
     * <p>Error handling covers:
     * <ul>
     *   <li>Missing notification permissions on Android 13+</li>
     *   <li>Invalid or missing user session data</li>
     *   <li>Firebase connectivity and deletion failures</li>
     *   <li>Null alarm ID or message data</li>
     * </ul></p>
     *
     * @param context The Context in which the receiver is running
     * @param intent  The Intent being received, containing alarm message and ID data
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.w("AlarmReceiver", "Notification permission not granted");
            return;
        }

        String message = intent.getStringExtra("alarm_message");
        String alarmId = intent.getStringExtra("alarm_id");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "timer_channel_id")
                .setSmallIcon(R.mipmap.ic_logo_round)
                .setContentTitle("BookTrack Alarm")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(1001, builder.build());

        FirebaseApp.initializeApp(context);
        SharedPreferences prefs = context.getSharedPreferences("BookTrackPrefs", Context.MODE_PRIVATE);
        String uid = prefs.getString("uid", null);

        if (uid != null && alarmId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("alarms")
                    .document(alarmId)
                    .delete()
                    .addOnSuccessListener(aVoid ->
                            Log.d("AlarmReceiver", "Deleted alarm: " + alarmId))
                    .addOnFailureListener(e ->
                            Log.e("AlarmReceiver", "Failed to delete alarm", e));
        } else {
            Log.w("AlarmReceiver", "User not logged in or alarmId is null");
        }
    }
}