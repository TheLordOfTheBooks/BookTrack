package com.example.booktrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmScheduler {
    public static void schedule(Context context, AlarmItem alarm) {
        if (alarm == null || alarm.getDeadlineMillis() < System.currentTimeMillis()) {
            Log.w("AlarmScheduler", "Skipping expired or null alarm");
            return;
        }

        Log.d("AlarmScheduler", "\u23f0 Scheduling alarm for: " + alarm.getDeadlineMillis());
        Log.d("AlarmScheduler", "\ud83d\udcc6 Current time: " + System.currentTimeMillis());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("AlarmScheduler", "AlarmManager not available");
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarm_id", alarm.getAlarmId());
        intent.putExtra("alarm_message", alarm.getBookName() + ": " + alarm.getMessage());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.getAlarmId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w("AlarmScheduler", "Exact alarm not allowed by system.");
            return;
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarm.getDeadlineMillis(),
                pendingIntent
        );

        Log.i("AlarmScheduler", "Alarm scheduled for: " + alarm.getDeadlineMillis());
    }

    public static void cancel(Context context, AlarmItem alarm) {
        if (alarm == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.getAlarmId().hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                Log.i("AlarmScheduler", "Canceled alarm: " + alarm.getAlarmId());
            }
        }
    }
}
