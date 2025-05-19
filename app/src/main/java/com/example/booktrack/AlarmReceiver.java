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

public class AlarmReceiver extends BroadcastReceiver {
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
