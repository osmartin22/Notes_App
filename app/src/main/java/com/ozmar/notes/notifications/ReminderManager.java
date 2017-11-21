package com.ozmar.notes.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.ozmar.notes.R;
import com.ozmar.notes.SingleNote;

import static android.content.Context.ALARM_SERVICE;


public class ReminderManager {

    public static void start(Context context, SingleNote note) {
        createReminder(context, note.getReminderId(), note.getTitle(), note.getContent(),
                note.getNextReminderTime(), note.hasFrequencyChoices());
    }

    public static void createReminder(Context context, int reminderId, @NonNull String title,
                                      @NonNull String content, long reminderTime,
                                      boolean hasFrequencyChoice) {

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderReceiver.class);

        myIntent.putExtra(context.getString(R.string.notificationId), reminderId);
        myIntent.putExtra(context.getString(R.string.notificationTitle), title);
        myIntent.putExtra(context.getString(R.string.notificationContent), content);
        myIntent.putExtra(context.getString(R.string.notificationHasFrequency), hasFrequencyChoice);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId,
                myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (manager != null) {
            manager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }

    }

    public static void cancel(Context context, int id) {
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, myIntent, 0);

        if (manager != null) {
            manager.cancel(pendingIntent);
        }
    }
}
