package com.ozmar.notes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;

import static android.content.Context.ALARM_SERVICE;


public class ReminderManager {

    public static void start(Context context, DateTime dateTime) {
        PendingIntent pendingIntent;
        AlarmManager manager;

        manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);

        manager.setExact(AlarmManager.RTC_WAKEUP, dateTime.getMillis(), pendingIntent);
    }
}
