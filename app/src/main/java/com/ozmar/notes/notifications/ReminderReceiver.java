package com.ozmar.notes.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.R;

import org.joda.time.DateTime;

// TODO: Fix FrequencyPicker showing wrong values in repeatToDate view

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int id = intent.getIntExtra(context.getString(R.string.notificationId), 0);
        String title = intent.getStringExtra(context.getString(R.string.notificationTitle));
        String content = intent.getStringExtra(context.getString(R.string.notificationContent));
        boolean hasFrequencyChoice = intent.getBooleanExtra(
                context.getString(R.string.notificationHasFrequency), false);

        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nManager != null) {
            nManager.notify(id, NotificationHelper.buildNotification(context, title, content));
        }

        if (hasFrequencyChoice) {
            long nextReminderTime = getNextReminderTime(context, id);
            if (nextReminderTime != 0) {
                ReminderManager.startForNextRepeat(context, id, title, content, nextReminderTime);
            }
        }
    }


    // TODO: Add eventsAlreadyOccurred to database to handle events that should only occur X times

    // TODO: Reset to zero when note reminder is modified in anyway

    public long getNextReminderTime(@NonNull Context context, int reminderId) {
        DatabaseHandler db = new DatabaseHandler(context);

        long nextReminderTime = 0;
        long currentReminderTime = db.getNextReminderTime(reminderId).nextReminderTime;
        FrequencyChoices choices = db.getFrequencyChoice(reminderId);

        boolean onEventsSatisfied = false;
        boolean onDesiredEndDateSatisfied = false;

        // User specified reminder to occur for X events
        if (choices.getRepeatEvents() > 0) {
            int eventsOccurred = db.getEventsOccurred(reminderId) + 1;  // Add 1 for the event that just occurred
            db.updateEventsOccurred(reminderId, eventsOccurred);
            onEventsSatisfied = eventsOccurred == choices.getRepeatEvents();

            // User specified an end date
        } else if (choices.getRepeatToDate() > 0) {
            onDesiredEndDateSatisfied = choices.getRepeatToDate() < System.currentTimeMillis();
        }


        // Only one of the booleans can be true at any time
        if (!(onEventsSatisfied || onDesiredEndDateSatisfied)) {
            nextReminderTime = calculateNextReminderTime(choices, currentReminderTime);

            if (nextReminderTime > currentReminderTime) {
                db.updateNextReminderTime(reminderId, nextReminderTime);
            }
        }

        return nextReminderTime;
    }

    private long calculateNextReminderTime(FrequencyChoices choices, long currentReminderTime) {
        long nextReminderTime = 0;
        DateTime dateTime = new DateTime(currentReminderTime);

        // TODO: Put FormatUtils strings into resources
        // TODO: Modify MonthlyLayoutHelper to accept and return the nth week of the month
            // If the nth month of the week passed is 0, calculate it
            // On subsequent calls to restore the reminder, it should not have to calculate the nth week
            // and just use the passed value instead

        switch (choices.getRepeatType()) {
            case 0:     // Daily
                if (choices.getRepeatEvery() == 1) {
                    nextReminderTime = dateTime.plusDays(1).getMillis();
                } else {
                    nextReminderTime = dateTime.plusDays(choices.getRepeatEvery()).getMillis();
                }
                break;

            case 1:     // Weekly

                // TODO: Calculate which day the reminder should be set to

                break;

            case 2:     // Monthly

                // TODO: Get week number for second option (i.e. 3rd week of the month)

                break;

            case 3:     // Yearly
                if (choices.getRepeatEvery() == 1) {
                    nextReminderTime = dateTime.plusYears(1).getMillis();
                } else {
                    nextReminderTime = dateTime.plusYears(choices.getRepeatEvery()).getMillis();
                }
                break;
        }

        return nextReminderTime;
    }
}



