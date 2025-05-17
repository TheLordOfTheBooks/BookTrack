package com.example.booktrack;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Received non-boot action: " + intent.getAction());
            return;
        }

        Log.d("BootReceiver", "\u26a1 BOOT_COMPLETED received");
        FirebaseApp.initializeApp(context);

        showDebugNotification(context);

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

    private void showDebugNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "timer_channel_id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("BootReceiver Test")
                .setContentText("BOOT_COMPLETED received!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        if (manager != null) {
            manager.notify(999, builder.build());
        }
    }
}