package com.ozmar.notes.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.R;
import com.ozmar.notes.utils.ReminderUtils;

import org.joda.time.DateTime;


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

    // TODO: Check if repeatEvents, or repeatToDate has not passed
    // If not, calculate next reminder
    // Check again if repeatToDate will be violated with the new reminder time
    public long getNextReminderTime(@NonNull Context context, int reminderId) {
        DatabaseHandler db = new DatabaseHandler(context);

        long nextReminderTime = 0;
        long currentReminderTime = db.getNextReminderTime(reminderId).nextReminderTime;
        FrequencyChoices choices = db.getFrequencyChoice(reminderId);

        boolean onEventsSatisfied = false;
        boolean onDesiredEndDateSatisfied = false;

        if (choices != null) {
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

        }
        return nextReminderTime;
    }


    // TODO: Possible optimization is to not immediately set an alarm manager when a new reminder is created
    // A reminder 2 months away does not need to be created.
    // Can have a separate alarm that runs every week that sets up any reminders that
    // will occur in that week
    private long calculateNextReminderTime(FrequencyChoices choices, long currentReminderTime) {
        long nextReminderTime = 0;

        DateTime dateTimeNow = DateTime.now();
        DateTime dateTimeReminder = new DateTime(currentReminderTime);

        // TODO: Decide which DateTime to use
        switch (choices.getRepeatType()) {
            case 0:     // Daily
                nextReminderTime = ReminderUtils.getNextDailyReminderTime(choices.getRepeatEvery(),
                        dateTimeNow);
                break;

            case 1:     // Weekly
                assert choices.getDaysChosen() != null;
                nextReminderTime = dateTimeReminder.getMillis() +
                        ReminderUtils.getNextWeeklyReminderTime(choices.getDaysChosen(),
                                dateTimeReminder.getDayOfWeek(), choices.getRepeatEvery());
                break;

            case 2:     // Monthly
                if (choices.getMonthRepeatType() != 0) {
                    nextReminderTime = dateTimeReminder.getMillis() +
                            ReminderUtils.getNextMonthlyReminder(dateTimeReminder, choices.getRepeatEvery(),
                                    choices.getMonthWeekToRepeat(), choices.getMonthDayOfWeekToRepeat());
                } else {
                    nextReminderTime = dateTimeReminder.plusMonths(1).getMillis();
                }
                break;

            case 3:     // Yearly
//                nextReminderTime = ReminderUtils.calculateYearlyReminderTime(choices.getRepeatEvery(),
//                        dateTimeReminder, dateTimeNow);

                nextReminderTime = ReminderUtils.calculateYearlyReminderTime(dateTimeReminder,
                        choices.getRepeatEvery());

                break;
        }

        return nextReminderTime;
    }
}



