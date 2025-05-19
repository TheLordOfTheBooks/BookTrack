package com.example.booktrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BootReceiver extends BroadcastReceiver {
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