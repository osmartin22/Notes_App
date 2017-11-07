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
    public static long calculateDailyReminderTime(@NonNull FrequencyChoices choices, @NonNull DateTime dateTime) {
        return dateTime.plusDays(choices.getRepeatEvery()).getMillis();
    }

    public static int getNextDayPosition(@NonNull List<Integer> daysChosen, int currentDayOfWeek) {
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

        // Occurs when at the end of the list, next day is now the first day in the list
        if(high == daysChosen.size()){
            high = 0;
        }

        return high;
    }

    // This returns the time difference from the starting day to the next day
    public static long getNextWeeklyReminderTime(@NonNull List<Integer> daysChosen, int currentDayOfWeek,
                                                 int repeatEvery) {
        long nextReminderTime = 0;
        int high = getNextDayPosition(daysChosen, currentDayOfWeek);
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

    // Does not take into account if the phone changes date/time (i.e. user manually changes date)
    public static long calculateWeeklyReminderTime(@NonNull FrequencyChoices choices, @NonNull DateTime dateTime) {
        List<Integer> daysChosen = choices.getDaysChosen();
        assert daysChosen != null;
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


    public static long calculateMonthlyReminderTime(@NonNull FrequencyChoices choices,
                                                    @NonNull DateTime oldReminder, @NonNull DateTime dateTimeNow) {
        long nextReminderTime = 0;

        DateTime currentYearReminder = oldReminder.withYear(dateTimeNow.getYear());
        boolean reminderIsAfterNow = currentYearReminder.isAfterNow();

        if (choices.getMonthRepeatType() == 0) {

        }

        return nextReminderTime;
    }

    public static long getNextMonthlyReminder(@NonNull DateTime dateTime, @NonNull FrequencyChoices choices) {
        long nextReminderTime = dateTime.getMillis();

        int chosenDateWeekNumber = FormatUtils.getNthWeekOfMonth(dateTime.getDayOfMonth());
        int weekNumberToForce = choices.getMonthWeekToRepeat();
        int dayOfWeekToForce = choices.getMonthDayOfWeekToRepeat();

        if (chosenDateWeekNumber != weekNumberToForce) {    // Need to find next occurrence

            // Go to next month with week to force
            if (weekNumberToForce < chosenDateWeekNumber) {

                // Move to the first desired day of the next month
                dateTime = dateTime.plusMonths(1).dayOfMonth().withMinimumValue()
                        .withDayOfWeek(dayOfWeekToForce);

            } else if (weekNumberToForce > chosenDateWeekNumber) {
                dateTime.withDayOfWeek(dayOfWeekToForce);
            }

            nextReminderTime = getForcedWeek(dateTime, weekNumberToForce, chosenDateWeekNumber);

        } else {
            // TODO: Change
            if (dayOfWeekToForce != dateTime.getDayOfWeek()) {
                nextReminderTime = dateTime.withDayOfWeek(dayOfWeekToForce).getMillis();
            }
        }

        return nextReminderTime;
    }

    public static long getForcedWeek(@NonNull DateTime dateTime, int weekNumberToForce, int chosenDateWeekNumber) {
        int maxDaysInMonth;
        int newDay;
        int daysToAdd;

        maxDaysInMonth = dateTime.dayOfMonth().withMaximumValue().getDayOfMonth();
        newDay = dateTime.getDayOfMonth();

        // Check if a fifth week is possible in the month
        if (weekNumberToForce == 5 && ReminderUtils.checkIfFifthWeekPossible(maxDaysInMonth, newDay)) {
            weekNumberToForce = 4;
        }

        if (chosenDateWeekNumber != 1) {
            daysToAdd = (weekNumberToForce - chosenDateWeekNumber) * 7;
        } else {
            daysToAdd = (weekNumberToForce - 1) * 7;
        }

        return dateTime.plusDays(daysToAdd).getMillis();
    }

    public static boolean checkIfFifthWeekPossible(int maxDaysInMonth, int day) {
        boolean possible = true;
        if (maxDaysInMonth == 31 && day > 3) {
            possible = false;

        } else if (maxDaysInMonth == 30 && day > 2) {
            possible = false;

        } else if (maxDaysInMonth == 29 && day > 1) {
            possible = false;
        }

        return possible;
    }


    // Takes into account if phone changes date/time (i.e. user manually changes time)
    public static long calculateYearlyReminderTime(@NonNull FrequencyChoices choices,
                                                   @NonNull DateTime oldReminder, @NonNull DateTime dateTimeNow) {

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
