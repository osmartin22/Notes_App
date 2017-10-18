package com.ozmar.notes.reminderDialog;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("Id", 0);
        String title = intent.getStringExtra("Title");
        String content = intent.getStringExtra("Content");

        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(id, ReminderManager.buildNotification(context, title, content));
    }
}
