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
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


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


    // TODO: Put FormatUtils strings into resources
    // TODO: Modify MonthlyLayoutHelper to accept and return the nth week of the month
    // If the nth month of the week passed is 0, calculate it
    // On subsequent calls to restore the reminder, it should not have to calculate the nth week
    // and just use the passed value instead

    private long calculateNextReminderTime(FrequencyChoices choices, long currentReminderTime) {
        long nextReminderTime = 0;

        DateTime dateTimeNow = DateTime.now();

        // Set datetime with desired reminder but with the current year;
        DateTime dateTimeReminder = new DateTime(currentReminderTime);

        switch (choices.getRepeatType()) {
            case 0:     // Daily
                nextReminderTime = calculateDailyReminderTime(choices, dateTimeReminder, dateTimeNow);
                break;

            case 1:     // Weekly
                nextReminderTime = calculateWeeklyReminderTime(choices, dateTimeReminder);
                break;

            case 2:     // Monthly
                // TODO: Get week number for second option (i.e. 3rd week of the month)

                break;

            // TODO: FIX TO CHECK DAY IS CORRECT WITH REMINDER
            case 3:     // Yearly
                nextReminderTime = calculateYearlyReminderTime(choices, dateTimeReminder, dateTimeNow);
                break;
        }

        return nextReminderTime;
    }

    private long calculateDailyReminderTime(FrequencyChoices choices, DateTime dateTimeReminder, DateTime dateTimeNow) {
        long nextReminderTime;

        // Set reminder to current month/day/year
        dateTimeReminder.withDate(new LocalDate(dateTimeNow)).withYear(dateTimeNow.getYear());

        if (dateTimeReminder.isAfter(dateTimeNow)) {
            nextReminderTime = dateTimeReminder.getMillis();
        } else {

            if (choices.getRepeatEvery() == 1) {
                nextReminderTime = dateTimeReminder.plusDays(1).getMillis();
            } else {
                nextReminderTime = dateTimeReminder.plusDays(choices.getRepeatEvery()).getMillis();

            }
        }

        return nextReminderTime;
    }

    private long calculateWeeklyReminderTime(FrequencyChoices choices, DateTime dateTime) {
        List<Integer> daysChosen = choices.getDaysChosen();
        Collections.sort(daysChosen);

        long nextReminderTime = dateTime.getMillis();
        int currentDay = dateTime.getDayOfWeek();

        // Repeats every day of the week
        if (daysChosen.size() == 7) {
            if (currentDay == 7) {
                nextReminderTime += TimeUnit.DAYS.toMillis(1);
                nextReminderTime += TimeUnit.DAYS.toMillis(7 * (choices.getRepeatEvery() - 1));
            } else {
                nextReminderTime += TimeUnit.DAYS.toMillis(1);
            }

            // Does not repeat every day of the week, need to finish current week reminders
        } else if (daysChosen.get(daysChosen.size() - 1) > currentDay) {
            int i = 0;
            while (daysChosen.get(i) <= currentDay) {
                i++;
            }
            int nextReminderDay = daysChosen.get(i);
            nextReminderTime = TimeUnit.DAYS.toMillis(nextReminderDay - currentDay);

            // Start over reminders for days chosen
        } else {
            nextReminderTime += TimeUnit.DAYS.toMillis(7 - currentDay);
            nextReminderTime += TimeUnit.DAYS.toMillis(daysChosen.get(0));
            nextReminderTime += TimeUnit.DAYS.toMillis(7 * (choices.getRepeatEvery() - 1));
        }

        return nextReminderTime;
    }

    private long calculateMonthlyReminderTime(FrequencyChoices choices, DateTime dateTime) {
        long nextReminderTime = 0;

        return nextReminderTime;
    }

    private long calculateYearlyReminderTime(FrequencyChoices choices, DateTime oldReminder, DateTime dateTimeNow) {

        DateTime currentYearReminder = oldReminder.withYear(dateTimeNow.getYear());

        long nextReminderTime;
        int yearDiff = currentYearReminder.getYear() - oldReminder.getYear();
        int moduloDiff = yearDiff % choices.getRepeatEvery();
        int yearsToAdd = choices.getRepeatEvery() - moduloDiff;

        // Reminder will not occur in current year
        if (moduloDiff != 0) {
            nextReminderTime = currentYearReminder.plusYears(yearsToAdd).getMillis();

            // Reminder possibly occurs in current year
        } else {
            if (currentYearReminder.isAfter(dateTimeNow)) {
                nextReminderTime = currentYearReminder.getMillis();
            } else {
                nextReminderTime = currentYearReminder.plusYears(choices.getMonthRepeatType()).getMillis();
            }
        }

        return nextReminderTime;
    }
}



