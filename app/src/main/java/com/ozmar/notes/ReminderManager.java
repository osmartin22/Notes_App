package com.ozmar.notes;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import static android.content.Context.ALARM_SERVICE;


public class ReminderManager {

    public static void start(Context context, SingleNote note) {
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderReceiver.class);

        myIntent.putExtra("Id", note.get_reminderId());
        myIntent.putExtra("Title", note.get_title());
        myIntent.putExtra("Content", note.get_content());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, note.get_reminderId(), myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setExact(AlarmManager.RTC_WAKEUP, note.get_reminderTime(), pendingIntent);
    }

    public static void cancel(Context context, int id) {
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, myIntent, 0);

        manager.cancel(pendingIntent);
    }

    public static Notification buildNotification(Context context, String title, String content) {
        NotificationCompat.Builder mBuilder = (android.support.v7.app.NotificationCompat.Builder)
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notes_drawer)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(true);

        return mBuilder.build();
    }
}
