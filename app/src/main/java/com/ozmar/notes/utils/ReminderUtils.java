package com.ozmar.notes.utils;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.ozmar.notes.FrequencyChoices;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class ReminderUtils {
    private ReminderUtils() {
    }

    public static long getNextDailyReminderTime(int repeatEveryXDays, @NonNull DateTime dateTime) {
        return dateTime.plusDays(repeatEveryXDays).getMillis();
    }

    public static long getNextWeeklyReminderTime(@NonNull List<Integer> daysChosen, int currentDayOfWeek,
                                                 int repeatEvery) {
        long nextReminderTime;
        int nextDayPosition = getNextDayPosition(daysChosen, currentDayOfWeek);
        if (daysChosen.get(nextDayPosition) > currentDayOfWeek) {
            nextReminderTime = TimeUnit.DAYS.toMillis(daysChosen.get(nextDayPosition) - currentDayOfWeek);

        } else {
            nextReminderTime = TimeUnit.DAYS.toMillis(7 - currentDayOfWeek);
            nextReminderTime += TimeUnit.DAYS.toMillis(daysChosen.get(nextDayPosition));
            nextReminderTime += TimeUnit.DAYS.toMillis(7 * (repeatEvery - 1));
        }
        return nextReminderTime;
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

    public static long getNextMonthlyReminder(@NonNull DateTime dateTime, int repeatEveryXMonths,
                                              @IntRange(from = 1, to = 5) int weekNumberToForce,
                                              @IntRange(from = 1, to = 5) int dayOfWeekToForce) {
        long nextReminderTime;

        int result = nthWeekDayOfMonth(dateTime.toLocalDate(), dayOfWeekToForce, weekNumberToForce);
        if (dateTime.getDayOfMonth() < result) {
            nextReminderTime = dateTime.withDayOfMonth(result).getMillis();
        } else {
            dateTime = dateTime.plusMonths(repeatEveryXMonths);
            result = nthWeekDayOfMonth(dateTime.toLocalDate(), dayOfWeekToForce, weekNumberToForce);
            nextReminderTime = dateTime.withDayOfMonth(result).getMillis();
        }

        return nextReminderTime;
    }


    // withDayOfWeek() has the chance of setting LocalDate to another month
    // (i.e. wednesday is the 1st, setting to MONDAY, will cause LocalDate to go to the previous month
    // This function makes sure that the correct month is used when this happens
    public static int getDayInCurrentMonth(LocalDate localDate, int month, int dayOfWeek) {
        LocalDate temp = localDate.withDayOfWeek(dayOfWeek);
        int newDay;
        if (temp.getMonthOfYear() != month) {
            if (localDate.isBefore(temp)) {
                newDay = temp.minusWeeks(1).getDayOfMonth();
            } else {
                newDay = temp.plusWeeks(1).getDayOfMonth();
            }
        } else {
            newDay = temp.getDayOfMonth();
        }

        return newDay;
    }

    public static int nthWeekDayOfMonth(LocalDate localDate, int dayOfWeek, int n) {

        int year = localDate.getYear();
        int month = localDate.getMonthOfYear();
        int newDay = getDayInCurrentMonth(localDate, month, dayOfWeek);
        int maxDaysInMonth = localDate.dayOfMonth().withMaximumValue().getDayOfMonth();
        boolean fifthWeekPossible = ReminderUtils.checkIfFifthWeekPossible(maxDaysInMonth, newDay);

        if (n == 5 && !fifthWeekPossible) {
            n = 4;
        }

        LocalDate start = new LocalDate(year, month, 1);
        LocalDate date = start.withDayOfWeek(dayOfWeek);

        LocalDate result = (date.isBefore(start)) ? date.plusWeeks(n) : date.plusWeeks(n - 1);
        return result.getDayOfMonth();
    }

    // Check if the given day of the week can occur five times in the given month
    public static boolean checkIfFifthWeekPossible(@IntRange(from = 28, to = 31) int maxDaysInMonth,
                                                   @IntRange(from = 1, to = 31) int day) {
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

    // TODO: Decide which year calculation I will use
    public static long getNextYearlyReminder(@NonNull DateTime dateTime, int repeatEveryXYears) {
        return dateTime.plusYears(repeatEveryXYears).getMillis();
    }

    //    public static long getNextYearlyReminder(int repeatEveryXYears, @NonNull DateTime oldReminder,
//                                                   @NonNull DateTime currentDateTime) {
//
//        DateTime currentYearReminder = oldReminder.withYear(currentDateTime.getYear());
//
//        long nextReminderTime;
//        int yearDiff = currentYearReminder.getYear() - oldReminder.getYear();
//        int moduloDiff = yearDiff % repeatEveryXYears;
//        int yearsToAdd = repeatEveryXYears - moduloDiff;
//
//        // Reminder will not occur in current year
//        if (moduloDiff != 0) {
//            nextReminderTime = currentYearReminder.plusYears(yearsToAdd).getMillis();
//
//            // Reminder possibly occurs in current year
//        } else {
//            if (currentYearReminder.isAfter(currentDateTime)) {
//                nextReminderTime = currentYearReminder.getMillis();
//            } else {
//                nextReminderTime = currentYearReminder.plusYears(repeatEveryXYears).getMillis();
//            }
//        }
//
//        return nextReminderTime;
//    }

    public static long getNextRepeatReminder(FrequencyChoices choices, long currentReminderTime) {
        long nextReminderTime = 0;

        DateTime dateTimeReminder = new DateTime(currentReminderTime);

        switch (choices.getRepeatType()) {
            case 0:     // Daily
                nextReminderTime = getNextDailyReminderTime(choices.getRepeatEvery(),
                        dateTimeReminder);
                break;

            case 1:     // Weekly
                assert choices.getDaysChosen() != null;
                nextReminderTime = dateTimeReminder.getMillis() +
                        getNextWeeklyReminderTime(choices.getDaysChosen(),
                                dateTimeReminder.getDayOfWeek(), choices.getRepeatEvery());
                break;

            case 2:     // Monthly
                if (choices.getMonthRepeatType() != 0) {
                    nextReminderTime = getNextMonthlyReminder(dateTimeReminder,
                            choices.getRepeatEvery(), choices.getMonthWeekToRepeat(),
                            choices.getMonthDayOfWeekToRepeat());
                } else {
                    nextReminderTime = dateTimeReminder.plusMonths(1).getMillis();
                }
                break;

            case 3:     // Yearly
                nextReminderTime = getNextYearlyReminder(dateTimeReminder, choices.getRepeatEvery());
                break;
        }

        return nextReminderTime;
    }
}
