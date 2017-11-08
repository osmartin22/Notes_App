package com.ozmar.notes.utils;

import android.support.annotation.IntRange;
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
    public static long calculateDailyReminderTime(int repeatEveryXDays, @NonNull DateTime dateTime) {
        return dateTime.plusDays(repeatEveryXDays).getMillis();
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
        if (high == daysChosen.size()) {
            high = 0;
        }

        return high;
    }

    // This returns the time difference from the starting day to the next day
    public static long getNextWeeklyReminderTime(@NonNull List<Integer> daysChosen, int currentDayOfWeek,
                                                 int repeatEvery) {
        long nextReminderTime;

        if (daysChosen.size() == 1 && daysChosen.get(0) == currentDayOfWeek) {
            nextReminderTime = TimeUnit.DAYS.toMillis(repeatEvery * 7);
        } else {

            int nextDayPosition = getNextDayPosition(daysChosen, currentDayOfWeek);
            if (daysChosen.get(nextDayPosition) > currentDayOfWeek) {
                nextReminderTime = TimeUnit.DAYS.toMillis(daysChosen.get(nextDayPosition) - currentDayOfWeek);

            } else {
                nextReminderTime = TimeUnit.DAYS.toMillis(7 - currentDayOfWeek);
                nextReminderTime += TimeUnit.DAYS.toMillis(daysChosen.get(nextDayPosition));
                nextReminderTime += TimeUnit.DAYS.toMillis(7 * (repeatEvery - 1));
            }
        }

        return nextReminderTime;
    }

    // Does not take into account if the phone changes date/time (i.e. user manually changes date)
    public static long calculateWeeklyReminderTime(int repeatEveryXWeeks, @NonNull List<Integer> daysChosen,
                                                   @NonNull DateTime dateTime) {
        Collections.sort(daysChosen);

        long nextReminderTime = dateTime.getMillis();
        int currentDayOfWeek = dateTime.getDayOfWeek();

        // Repeats every day of the week
        if (daysChosen.size() == 7) {
            if (currentDayOfWeek == 7) {
                nextReminderTime += TimeUnit.DAYS.toMillis(1);
                nextReminderTime += TimeUnit.DAYS.toMillis(7 * (repeatEveryXWeeks - 1));
            } else {
                nextReminderTime += TimeUnit.DAYS.toMillis(1);
            }

            // Does not repeat every day of the week
        } else {
            nextReminderTime += getNextWeeklyReminderTime(daysChosen, currentDayOfWeek, repeatEveryXWeeks);
        }

        return nextReminderTime;
    }


    //TODO: Replace FrequencyChoices
    public static long calculateMonthlyReminderTime(@NonNull FrequencyChoices choices,
                                                    @NonNull DateTime oldReminder, @NonNull DateTime dateTimeNow) {
        long nextReminderTime = 0;

        DateTime currentYearReminder = oldReminder.withYear(dateTimeNow.getYear());
        boolean reminderIsAfterNow = currentYearReminder.isAfterNow();

        if (choices.getMonthRepeatType() == 0) {

        }

        return nextReminderTime;
    }

    //TODO: Replace FrequencyChoices
    public static long getNextMonthlyReminder(@NonNull DateTime dateTime, @NonNull FrequencyChoices choices) {
        long nextReminderTime = dateTime.getMillis();

        int currentWeekNumber = FormatUtils.getNthWeekOfMonth(dateTime.getDayOfMonth());
        int weekNumberToForce = choices.getMonthWeekToRepeat();
        int dayOfWeekToForce = choices.getMonthDayOfWeekToRepeat();

        if (currentWeekNumber != weekNumberToForce) {

            // Go to next month with week to force
            if (weekNumberToForce < currentWeekNumber) {

                // Move to the first desired day of the next month
                dateTime = dateTime.plusMonths(1).dayOfMonth().withMinimumValue()
                        .withDayOfWeek(dayOfWeekToForce);

            } else if (weekNumberToForce > currentWeekNumber) {
                dateTime.withDayOfWeek(dayOfWeekToForce);
            }

            nextReminderTime = getForcedWeek(dateTime, weekNumberToForce, currentWeekNumber);

        } else {
            // TODO: Change
            if (dayOfWeekToForce != dateTime.getDayOfWeek()) {
                nextReminderTime = dateTime.withDayOfWeek(dayOfWeekToForce).getMillis();
            }
        }

        return nextReminderTime;
    }

    public static long getForcedWeek(@NonNull DateTime dateTime,
                                     @IntRange(from = 1, to = 5) int weekNumberToForce,
                                     @IntRange(from = 1, to = 5) int currentWeekNumber) {
        int maxDaysInMonth;
        int newDay;
        int daysToAdd;

        maxDaysInMonth = dateTime.dayOfMonth().withMaximumValue().getDayOfMonth();
        newDay = dateTime.getDayOfMonth();

        if (weekNumberToForce == 5 && !ReminderUtils.checkIfFifthWeekPossible(maxDaysInMonth, newDay)) {
            weekNumberToForce = 4;
        }

        if (currentWeekNumber != 1) {
            daysToAdd = (weekNumberToForce - currentWeekNumber) * 7;
        } else {
            daysToAdd = (weekNumberToForce - 1) * 7;
        }

        return dateTime.plusDays(daysToAdd).getMillis();
    }

    // Check if the given day of the week can have occur five times in the given month
    public static boolean checkIfFifthWeekPossible(int maxDaysInMonth, int day) {
        boolean possible = false;
        int dayModulo = day % 7;

        if (dayModulo != 0) {
            if (maxDaysInMonth == 31 && dayModulo <= 3) {
                possible = true;

            } else if (maxDaysInMonth == 30 && dayModulo <= 2) {
                possible = true;

            } else if (maxDaysInMonth == 29 && dayModulo <= 1) {
                possible = true;
            }
        }

        return possible;
    }


    // Takes into account if phone changes date/time (i.e. user manually changes time)
    public static long calculateYearlyReminderTime(int repeatEveryXYears, @NonNull DateTime oldReminder,
                                                   @NonNull DateTime currentDateTime) {

        DateTime currentYearReminder = oldReminder.withYear(currentDateTime.getYear());

        long nextReminderTime;
        int yearDiff = currentYearReminder.getYear() - oldReminder.getYear();
        int moduloDiff = yearDiff % repeatEveryXYears;
        int yearsToAdd = repeatEveryXYears - moduloDiff;

        // Reminder will not occur in current year
        if (moduloDiff != 0) {
            nextReminderTime = currentYearReminder.plusYears(yearsToAdd).getMillis();

            // Reminder possibly occurs in current year
        } else {
            if (currentYearReminder.isAfter(currentDateTime)) {
                nextReminderTime = currentYearReminder.getMillis();
            } else {
                nextReminderTime = currentYearReminder.plusYears(repeatEveryXYears).getMillis();
            }
        }

        return nextReminderTime;
    }

}
