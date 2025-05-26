package com.example.booktrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.booktrack.AlarmItem;
import com.example.booktrack.AlarmScheduler;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * BroadcastReceiver that handles device boot completion to restore scheduled reading alarms.
 * This receiver is essential for maintaining alarm functionality across device reboots,
 * as Android clears all scheduled alarms when the device restarts.
 *
 * <p>The BootReceiver performs critical system restoration functions including:
 * <ul>
 *   <li>Detection of device boot completion events</li>
 *   <li>Firebase initialization for database connectivity</li>
 *   <li>User session validation through SharedPreferences</li>
 *   <li>Retrieval of all active alarms from Firestore</li>
 *   <li>Automatic cleanup of expired alarms discovered during boot</li>
 *   <li>Rescheduling of all valid future alarms through AlarmScheduler</li>
 * </ul></p>
 *
 * <p>This receiver ensures that users continue to receive their reading reminders
 * even after device restarts, power cycles, or system updates. It maintains the
 * integrity of the alarm system by performing database cleanup and selective
 * rescheduling based on current time validation.</p>
 *
 * <p>The receiver operates in a background context immediately after boot,
 * requiring minimal system resources while ensuring complete alarm restoration.
 * It includes comprehensive error handling to gracefully manage connectivity
 * issues, invalid user sessions, and database access failures.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class BootReceiver extends BroadcastReceiver {

    /**
     * Called when the BroadcastReceiver receives the BOOT_COMPLETED intent.
     * This method handles the complete alarm restoration workflow including
     * validation, cleanup, and rescheduling of all user alarms.
     *
     * <p>The restoration process follows these steps:
     * <ul>
     *   <li>Validation of the received intent action to ensure it's BOOT_COMPLETED</li>
     *   <li>Firebase initialization for database connectivity in boot context</li>
     *   <li>User session validation through SharedPreferences UID retrieval</li>
     *   <li>Firestore query to fetch all alarms for the authenticated user</li>
     *   <li>Iteration through all retrieved alarms for validation and processing</li>
     *   <li>Automatic deletion of expired alarms to maintain database hygiene</li>
     *   <li>Rescheduling of valid future alarms using AlarmScheduler</li>
     *   <li>Comprehensive logging for successful operations and error conditions</li>
     * </ul></p>
     *
     * <p>Validation and cleanup logic:
     * <ul>
     *   <li>Compares alarm deadline with current system time</li>
     *   <li>Deletes expired alarms from Firestore to prevent accumulation</li>
     *   <li>Only reschedules alarms with future deadlines</li>
     *   <li>Maintains alarm count logging for debugging and monitoring</li>
     * </ul></p>
     *
     * <p>Error handling covers:
     * <ul>
     *   <li>Non-boot intent actions (graceful early return)</li>
     *   <li>Missing or invalid user session data</li>
     *   <li>Firebase connectivity and authentication issues</li>
     *   <li>Firestore query failures and timeout conditions</li>
     *   <li>Individual alarm scheduling failures</li>
     * </ul></p>
     *
     * <p>This method operates asynchronously with Firebase operations to avoid
     * blocking the boot process while ensuring complete alarm restoration.</p>
     *
     * @param context The Context in which the receiver is running, providing access to system services
     * @param intent  The Intent being received, expected to contain ACTION_BOOT_COMPLETED action
     *
     * @see AlarmScheduler#schedule(Context, AlarmItem)
     * @see Intent#ACTION_BOOT_COMPLETED
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Received non-boot action: " + intent.getAction());
            return;
        }

        FirebaseApp.initializeApp(context);

        SharedPreferences prefs = context.getSharedPreferences("BookTrackPrefs", Context.MODE_PRIVATE);
        String uid = prefs.getString("uid", null);

        if (uid == null) {
            Log.e("BootReceiver", "No UID stored, skipping reschedule.");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("alarms")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("BootReceiver", "Fetched " + querySnapshot.size() + " alarms");

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        AlarmItem alarm = doc.toObject(AlarmItem.class);
                        long millis = alarm.getDeadlineMillis();

                        if (millis < System.currentTimeMillis()) {
                            doc.getReference().delete();
                            Log.d("BootReceiver", "Deleted expired alarm: " + alarm.getAlarmId());
                            continue;
                        }

                        AlarmScheduler.schedule(context, alarm);
                        Log.i("BootReceiver", "Rescheduled alarm: " + millis);
                    }
                })
                .addOnFailureListener(e -> Log.e("BootReceiver", "Failed to load alarms", e));
    }
}