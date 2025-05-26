package com.example.booktrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

/**
 * BroadcastReceiver that handles timer completion events triggered by the Android AlarmManager.
 * This receiver is activated when a scheduled timer alarm fires, initiating the timer completion
 * notification process through the TimerService.
 *
 * <p>The TimerReceiver serves as a critical bridge between the Android alarm system and
 * the BookTrack timer functionality, ensuring that timer completion notifications are
 * delivered reliably even when the application is not running or the device is in sleep mode.</p>
 *
 * <p>Key responsibilities include:
 * <ul>
 *   <li>Detection and validation of timer alarm broadcasts</li>
 *   <li>Initiation of the TimerService for completion notification handling</li>
 *   <li>Proper foreground service startup for notification delivery</li>
 *   <li>Reliable operation during device idle and background scenarios</li>
 * </ul></p>
 *
 * <p>This receiver is designed to work in conjunction with the TimerFragment's alarm
 * scheduling system, providing a complete end-to-end timer solution that operates
 * independently of application lifecycle states.</p>
 *
 * <p>The receiver handles only the "TIMER_ALARM" action, ensuring targeted response
 * to timer-specific events while ignoring unrelated broadcast messages.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class TimerReceiver extends BroadcastReceiver {

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast from the AlarmManager.
     * This method validates the timer alarm action and initiates the TimerService to handle
     * timer completion notifications and user alerts.
     *
     * <p>The processing workflow includes:
     * <ul>
     *   <li>Intent action validation to ensure this is a timer alarm event</li>
     *   <li>TimerService intent creation with completion state configuration</li>
     *   <li>Foreground service initiation for reliable notification delivery</li>
     * </ul></p>
     *
     * <p>The method starts the TimerService with a duration of 0, signaling that the
     * timer has completed and the service should handle completion notifications,
     * sound alerts, and user interface updates.</p>
     *
     * <p>Using {@code ContextCompat.startForegroundService} ensures proper service
     * startup across different Android versions while maintaining compatibility with
     * background execution limitations introduced in modern Android versions.</p>
     *
     * @param context The Context in which the receiver is running
     * @param intent  The Intent being received, expected to contain "TIMER_ALARM" action
     *
     * @see TimerService
     * @see TimerFragment
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("TIMER_ALARM".equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, TimerService.class);
            serviceIntent.putExtra(TimerService.EXTRA_DURATION, 0);
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}