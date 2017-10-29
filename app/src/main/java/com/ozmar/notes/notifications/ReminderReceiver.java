package com.ozmar.notes.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.ozmar.notes.R;


public class ReminderReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int id = intent.getIntExtra(context.getString(R.string.notificationId), 0);
        String title = intent.getStringExtra(context.getString(R.string.notificationTitle));
        String content = intent.getStringExtra(context.getString(R.string.notificationContent));

        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nManager != null) {
            nManager.notify(id, NotificationHelper.buildNotification(context, title, content));
        }
    }
}
