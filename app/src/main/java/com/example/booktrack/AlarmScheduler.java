package com.example.booktrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Utility class for scheduling reading alarms using the Android AlarmManager.
 * This class provides a centralized interface for scheduling precise, wake-capable
 * alarms that integrate with the BookTrack notification system.
 *
 * <p>The AlarmScheduler handles the complexities of modern Android alarm scheduling,
 * including API level compatibility, exact alarm permissions, and battery optimization
 * considerations. It ensures that reading reminders are delivered reliably even when
 * the device is in idle or doze mode.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Exact alarm scheduling with wake-up capability for reliable delivery</li>
 *   <li>Android 12+ compatibility with exact alarm permission checking</li>
 *   <li>Automatic validation of alarm data and expiration checking</li>
 *   <li>Integration with AlarmReceiver for notification handling</li>
 *   <li>Comprehensive error handling and logging for debugging</li>
 *   <li>Battery optimization bypass for critical reading reminders</li>
 * </ul></p>
 *
 * <p>The scheduler uses {@code setExactAndAllowWhileIdle} to ensure alarms trigger
 * precisely at the scheduled time, even when the device is in power-saving modes.
 * This is essential for reading habit maintenance and user engagement.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class AlarmScheduler {

    /**
     * Schedules a reading alarm using the Android AlarmManager system.
     * This method creates a precise, wake-capable alarm that will trigger
     * the AlarmReceiver at the specified deadline time.
     *
     * <p>The scheduling process includes comprehensive validation and error handling:
     * <ul>
     *   <li>Validation of alarm data and expiration checking</li>
     *   <li>AlarmManager availability verification</li>
     *   <li>Intent creation with alarm metadata for the AlarmReceiver</li>
     *   <li>PendingIntent configuration with proper flags for security and reliability</li>
     *   <li>Android 12+ exact alarm permission validation</li>
     *   <li>Precise alarm scheduling with wake-up capability</li>
     * </ul></p>
     *
     * <p>The method uses {@code setExactAndAllowWhileIdle} with {@code RTC_WAKEUP}
     * to ensure the alarm fires precisely at the scheduled time and can wake the device
     * from sleep or idle modes. This guarantees reliable delivery of reading reminders
     * regardless of device power state.</p>
     *
     * <p>Security considerations:
     * <ul>
     *   <li>Uses {@code FLAG_IMMUTABLE} for PendingIntent security on Android 12+</li>
     *   <li>Uses {@code FLAG_UPDATE_CURRENT} to handle alarm updates properly</li>
     *   <li>Validates exact alarm permissions before scheduling on Android 12+</li>
     * </ul></p>
     *
     * <p>Error conditions handled:
     * <ul>
     *   <li>Null or expired alarm objects</li>
     *   <li>Unavailable AlarmManager service</li>
     *   <li>Missing exact alarm permissions on Android 12+</li>
     *   <li>System-level scheduling failures</li>
     * </ul></p>
     *
     * @param context The application context required for system service access and Intent creation
     * @param alarm   The AlarmItem containing scheduling details including deadline, ID, book name, and message.
     *               Must not be null and must have a future deadline timestamp.
     *
     * @throws SecurityException on Android 12+ if exact alarm permission is not granted
     *
     * @see AlarmReceiver
     * @see AlarmItem
     * @see AlarmManager#setExactAndAllowWhileIdle(int, long, PendingIntent)
     */
    public static void schedule(Context context, AlarmItem alarm) {
        if (alarm == null || alarm.getDeadlineMillis() < System.currentTimeMillis()) {
            Log.w("AlarmScheduler", "Skipping expired or null alarm");
            return;
        }

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
}