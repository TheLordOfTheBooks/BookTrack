package com.example.booktrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class TimerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("TIMER_ALARM".equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, TimerService.class);
            serviceIntent.putExtra(TimerService.EXTRA_DURATION, 0); // Only to trigger sound
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
