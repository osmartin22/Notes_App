package com.ozmar.notes.utils;

import android.support.annotation.NonNull;

import com.ozmar.notes.FrequencyChoices;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ReminderUtils {
    private ReminderUtils() {
    }


    // Does not take into account if the phone changes date/time (i.e. user manually changes date)
    public static long calculateDailyReminderTime(FrequencyChoices choices, DateTime dateTimeNow) {
        return dateTimeNow.plusDays(choices.getRepeatEvery()).getMillis();
    }

    // Does not take into account if the phone changes date/time (i.e. user manually changes date)
    public static long calculateWeeklyReminderTime(FrequencyChoices choices, DateTime dateTime) {
        List<Integer> daysChosen = choices.getDaysChosen();
        Collections.sort(daysChosen);

        long nextReminderTime = dateTime.getMillis();
        int currentDayOfWeek = dateTime.getDayOfWeek();

        // Repeats every day of the week
        if (daysChosen.size() == 7) {
            if (currentDayOfWeek == 7) {
                nextReminderTime += TimeUnit.DAYS.toMillis(1);
                nextReminderTime += TimeUnit.DAYS.toMillis(7 * (choices.getRepeatEvery() - 1));
            } else {
                nextReminderTime += TimeUnit.DAYS.toMillis(1);
            }

            // Does not repeat every day of the week
        } else {
            nextReminderTime += getNextWeeklyReminderTime(daysChosen, currentDayOfWeek, choices.getRepeatEvery());
        }

        return nextReminderTime;
    }

    public static long getNextWeeklyReminderTime(List<Integer> daysChosen, int currentDayOfWeek, int repeatEvery) {
        long nextReminderTime = 0;
        int high = getNextLargestDay(daysChosen, currentDayOfWeek);
        if (high != daysChosen.size()) {
            nextReminderTime += TimeUnit.DAYS.toMillis(daysChosen.get(high) - currentDayOfWeek);

            // Start at next X week
        } else {
            nextReminderTime += TimeUnit.DAYS.toMillis(7 - currentDayOfWeek);
            nextReminderTime += TimeUnit.DAYS.toMillis(daysChosen.get(0));
            nextReminderTime += TimeUnit.DAYS.toMillis(7 * (repeatEvery - 1));
        }

        return nextReminderTime;
    }


    private static int getNextLargestDay(@NonNull List<Integer> daysChosen, int currentDayOfWeek) {
        int low = 0;
        int high = daysChosen.size();

        while (low != high) {
            int mid = (low + high) / 2;
            if (daysChosen.get(mid) <= currentDayOfWeek) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        return high;
    }

    public static long calculateMonthlyReminderTime(FrequencyChoices choices, DateTime oldReminder, DateTime dateTimeNow) {
        long nextReminderTime = 0;

        DateTime currentYearReminder = oldReminder.withYear(dateTimeNow.getYear());
        boolean reminderIsAfterNow = currentYearReminder.isAfterNow();

        if (choices.getMonthRepeatType() == 0) {

        }

        return nextReminderTime;
    }

    // Takes into account if phone changes date/time (i.e. user manually changes time)
    public static long calculateYearlyReminderTime(FrequencyChoices choices, DateTime oldReminder, DateTime dateTimeNow) {

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
