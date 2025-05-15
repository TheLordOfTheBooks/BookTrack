package com.example.booktrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        FirebaseApp.initializeApp(context);
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("BootReceiver", "User not logged in");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("alarms")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        AlarmItem alarm = doc.toObject(AlarmItem.class);
                        long millis = alarm.getTriggerMillis();
                        String message = alarm.getMessage();

                        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                        alarmIntent.putExtra("alarm_message", alarm.getBookName() + ": " + message);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                context,
                                (int) millis,
                                alarmIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );

                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        if (alarmManager == null) continue;

                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (!alarmManager.canScheduleExactAlarms()) {
                                    Log.w("BootReceiver", "Exact alarm permission denied by system");
                                    continue;
                                }
                            }

                            alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    millis,
                                    pendingIntent
                            );
                            Log.i("BootReceiver", "Rescheduled alarm for: " + millis);

                        } catch (SecurityException e) {
                            Log.e("BootReceiver", "SecurityException while setting alarm", e);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("BootReceiver", "Failed to load alarms", e));
    }
}
