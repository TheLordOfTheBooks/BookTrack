package com.example.booktrack;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.Manifest;

public class AlarmHandlerService extends IntentService {
    public AlarmHandlerService() {
        super("AlarmHandlerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FirebaseApp.initializeApp(this);

        String alarmId = intent.getStringExtra("alarm_id");
        String message = intent.getStringExtra("alarm_message");

        // 🔔 Show notification
        showNotification(this, message);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            deleteAlarm(user.getUid(), alarmId);
        } else {
            auth.signInAnonymously()
                    .addOnSuccessListener(result -> {
                        FirebaseUser u = result.getUser();
                        if (u != null) deleteAlarm(u.getUid(), alarmId);
                    })
                    .addOnFailureListener(e ->
                            Log.e("AlarmHandlerService", "Anon login failed", e));
        }
    }

    private void deleteAlarm(String uid, String alarmId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("alarms")
                .document(alarmId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d("AlarmHandlerService", "Alarm deleted: " + alarmId))
                .addOnFailureListener(e ->
                        Log.e("AlarmHandlerService", "Delete failed", e));
    }

    private void showNotification(Context context, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "timer_channel_id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Book Alarm")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(1001, builder.build());
    }
}
