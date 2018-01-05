package com.ozmar.notes.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.ozmar.notes.R;
import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.MainNote;

import java.util.List;

import static android.content.Context.ALARM_SERVICE;


public class ReminderNotificationManager {

    public static void setUpReminderAlarm(@NonNull Context context, @NonNull MainNote note,
                                          @NonNull Reminder reminder) {
        createReminderAlarm(context, reminder.getId(), note.getTitle(), note.getContent(),
                reminder.getDateTime().getMillis());
    }

    public static void createReminderAlarm(@NonNull Context context, int reminderId,
                                           @NonNull String title, @NonNull String content,
                                           long reminderTime) {

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderNotificationReceiver.class);

        myIntent.putExtra(context.getString(R.string.notificationId), reminderId);
        myIntent.putExtra(context.getString(R.string.notificationTitle), title);
        myIntent.putExtra(context.getString(R.string.notificationContent), content);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId,
                myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (manager != null) {
            manager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }

    }

    public static void cancelAlarm(@NonNull Context context, int id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, myIntent, 0);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void cancelListOfAlarms(@NonNull Context context, @NonNull List<Integer> reminderIds) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderNotificationReceiver.class);

        if (alarmManager != null) {
            PendingIntent pendingIntent;
            for (int id : reminderIds) {
                pendingIntent = PendingIntent.getBroadcast(context, id, myIntent, 0);
                alarmManager.cancel(pendingIntent);
            }
        }


    }
}
