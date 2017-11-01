package com.ozmar.notes.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ozmar.notes.R;
import com.ozmar.notes.SingleNote;

import static android.content.Context.ALARM_SERVICE;


public class ReminderManager {

    public static void start(Context context, SingleNote note) {
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderReceiver.class);

        myIntent.putExtra(context.getString(R.string.notificationId), note.get_reminderId());
        myIntent.putExtra(context.getString(R.string.notificationTitle), note.get_title());
        myIntent.putExtra(context.getString(R.string.notificationContent), note.get_content());
        myIntent.putExtra(context.getString(R.string.notificationHasFrequency), note.hasFrequencyChoices());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, note.get_reminderId(),
                myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (manager != null) {

            manager.setAlarmClock(new AlarmManager.AlarmClockInfo(note.get_nextReminderTime(),
                    pendingIntent), pendingIntent);
        }
    }

    public static void startForNextRepeat(Context context, int reminderId, String title, String content,
                                          long nextReminderTime) {

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderReceiver.class);

        myIntent.putExtra(context.getString(R.string.notificationId), reminderId);
        myIntent.putExtra(context.getString(R.string.notificationTitle), title);
        myIntent.putExtra(context.getString(R.string.notificationContent), content);
        myIntent.putExtra(context.getString(R.string.notificationHasFrequency), true);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId,
                myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (manager != null) {
            manager.setAlarmClock(new AlarmManager.AlarmClockInfo(nextReminderTime, pendingIntent), pendingIntent);
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
