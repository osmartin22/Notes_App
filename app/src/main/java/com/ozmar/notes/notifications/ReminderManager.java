package com.ozmar.notes.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.ozmar.notes.R;
import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.MainNote;

import static android.content.Context.ALARM_SERVICE;


public class ReminderManager {

    public static void setUpReminder(@NonNull Context context, @NonNull MainNote note, @NonNull Reminder reminder) {
        createReminder(context, reminder.getId(), note.getTitle(), note.getContent(),
                reminder.getDateTime().getMillis());
    }

    public static void createReminder(@NonNull Context context, int reminderId, @NonNull String title,
                                      @NonNull String content, long reminderTime) {

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderReceiver.class);

        myIntent.putExtra(context.getString(R.string.notificationId), reminderId);
        myIntent.putExtra(context.getString(R.string.notificationTitle), title);
        myIntent.putExtra(context.getString(R.string.notificationContent), content);


        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId,
                myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (manager != null) {
            manager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }

    }

    public static void cancel(@NonNull Context context, int id) {
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, myIntent, 0);

        if (manager != null) {
            manager.cancel(pendingIntent);
        }
    }
}
