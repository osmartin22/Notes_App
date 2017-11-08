package com.ozmar.notes;

import com.ozmar.notes.utils.FormatUtils;
import com.ozmar.notes.utils.ReminderUtils;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ReminderUtilsUnitTest {

    private static final int MONDAY = 1;
    private static final int TUESDAY = 2;
    private static final int WEDNESDAY = 3;
    private static final int THURSDAY = 4;
    private static final int FRIDAY = 5;
    private static final int SATURDAY = 6;
    private static final int SUNDAY = 7;

    @Test
    public void calculateDailyReminderTime_IsCorrect() throws Exception {
        DateTime dateTime = new DateTime(2001, 1, 7, 13, 0, 0);
        long result;
        int repeatEvery;

        repeatEvery = 1;
        result = ReminderUtils.calculateDailyReminderTime(1, dateTime);
        Assert.assertEquals(dateTime.plusDays(repeatEvery).getMillis(), result);

        repeatEvery = 21;
        result = ReminderUtils.calculateDailyReminderTime(21, dateTime);
        Assert.assertEquals(dateTime.plusDays(repeatEvery).getMillis(), result);

    }

    private void nextDayPositionHelper(List<Integer> daysChosen, int currentDayOfWeek, int expectedDay) {
        int resultDay = daysChosen.get(ReminderUtils.getNextDayPosition(daysChosen, currentDayOfWeek));
        Assert.assertEquals(expectedDay, resultDay);
    }

    @Test
    public void getNextDayPosition_IsCorrect() throws Exception {
        List<Integer> daysChosen;

        // Test if the current day is also in the list
        daysChosen = new ArrayList<>(Arrays.asList(MONDAY, WEDNESDAY, FRIDAY, SUNDAY));
        nextDayPositionHelper(daysChosen, MONDAY, WEDNESDAY);
        nextDayPositionHelper(daysChosen, WEDNESDAY, FRIDAY);
        nextDayPositionHelper(daysChosen, FRIDAY, SUNDAY);
        nextDayPositionHelper(daysChosen, SUNDAY, MONDAY);


        // Test when the current day is not in the list
        daysChosen = new ArrayList<>(Collections.singletonList(SATURDAY));
        nextDayPositionHelper(daysChosen, THURSDAY, SATURDAY);
        nextDayPositionHelper(daysChosen, FRIDAY, SATURDAY);
        nextDayPositionHelper(daysChosen, SUNDAY, SATURDAY);

        daysChosen = new ArrayList<>(Arrays.asList(FRIDAY, SUNDAY));
        nextDayPositionHelper(daysChosen, MONDAY, FRIDAY);
        nextDayPositionHelper(daysChosen, WEDNESDAY, FRIDAY);
        nextDayPositionHelper(daysChosen, SATURDAY, SUNDAY);

        daysChosen = new ArrayList<>(Arrays.asList(TUESDAY, WEDNESDAY, FRIDAY, SATURDAY));
        nextDayPositionHelper(daysChosen, MONDAY, TUESDAY);
        nextDayPositionHelper(daysChosen, THURSDAY, FRIDAY);
        nextDayPositionHelper(daysChosen, SUNDAY, TUESDAY);
    }

    private void weeklyReminderHelper(int currentDayOfWeek, int daysSeparation, int repeatEveryWeeks,
                                      List<Integer> daysChosen) {

        long resultTime = ReminderUtils.getNextWeeklyReminderTime(daysChosen, currentDayOfWeek, repeatEveryWeeks);
        Assert.assertEquals(TimeUnit.DAYS.toMillis(daysSeparation), resultTime);
    }

    @Test
    public void getNextWeeklyReminderTime_IsCorrect() throws Exception {
        List<Integer> daysChosen;

        // Test with only one day in the list
        daysChosen = new ArrayList<>(Collections.singletonList(MONDAY));
        weeklyReminderHelper(MONDAY, 7, 1, daysChosen);
        weeklyReminderHelper(MONDAY, 21, 3, daysChosen);
        weeklyReminderHelper(MONDAY, 0, 0, daysChosen);
        weeklyReminderHelper(TUESDAY, 6, 1, daysChosen);
        weeklyReminderHelper(SUNDAY, 8, 2, daysChosen);


        // Test when currentDayOfWeek is in the list returns the difference to the next day in the list
        daysChosen = new ArrayList<>(Arrays.asList(MONDAY, TUESDAY));
        weeklyReminderHelper(MONDAY, 1, 1, daysChosen);
        weeklyReminderHelper(MONDAY, 1, 117, daysChosen);

        daysChosen = new ArrayList<>(Arrays.asList(MONDAY, THURSDAY, SUNDAY));
        weeklyReminderHelper(MONDAY, 3, 1, daysChosen);
        weeklyReminderHelper(MONDAY, 3, 99, daysChosen);

        daysChosen = new ArrayList<>(Arrays.asList(MONDAY, WEDNESDAY, THURSDAY, SUNDAY));
        weeklyReminderHelper(FRIDAY, 2, 3, daysChosen);


        // Test if time difference returned when at the end of the list is correct
        daysChosen = new ArrayList<>(Arrays.asList(MONDAY, WEDNESDAY, THURSDAY));
        weeklyReminderHelper(SATURDAY, 2, 1, daysChosen);
        weeklyReminderHelper(SATURDAY, 16, 3, daysChosen);

        daysChosen = new ArrayList<>(Arrays.asList(MONDAY, WEDNESDAY, THURSDAY));
        weeklyReminderHelper(SATURDAY, 30, 5, daysChosen);
        weeklyReminderHelper(SATURDAY, 72, 11, daysChosen);
    }

    @Test
    public void calculateWeeklyReminderTime_IsCorrect() throws Exception {

    }

    @Test
    public void calculateMonthlyReminderTime_IsCorrect() throws Exception {

    }

    @Test
    public void getNextMonthlyReminder_IsCorrect() throws Exception {

    }

    private void forcedWeekHelper(DateTime dateTime, int weekNumberToForce, int weeksToAdd) {
        int currentWeekNumber = FormatUtils.getNthWeekOfMonth(dateTime.getDayOfMonth());
        long result = ReminderUtils.getForcedWeek(dateTime, weekNumberToForce, currentWeekNumber);
        Assert.assertEquals(dateTime.plusWeeks(weeksToAdd).getMillis(), result);
    }

    @Test
    public void getForcedWeek_IsCorrect() throws Exception {
        DateTime dateTime = new DateTime(2017, 11, 1, 12, 0);

        // First week of the month
        // 5 weeks possible for starting day
        for (int i = 1; i < 6; i++) {
            forcedWeekHelper(dateTime, i, i - 1);
        }

        // 5 weeks not possible for starting day
        dateTime = dateTime.withDayOfMonth(7);
        for (int i = 1; i < 5; i++) {
            forcedWeekHelper(dateTime, i, i - 1);
        }
        forcedWeekHelper(dateTime, 5, 3);


        // Third week of the month
        // Test when forceWeekNumber is after currentWeekNumber
        dateTime = dateTime.withDayOfMonth(15); // 5 weeks possible for given day
        for (int i = 3; i < 6; i++) {
            forcedWeekHelper(dateTime, i, i - 3);
        }

        // Test when forceWeekNumber is before the currentWeekNumber
//        forcedWeekHelper();

//        forcedWeekHelper(dateTime, 1, 0);
//        forcedWeekHelper(dateTime, 2, 1);
//        forcedWeekHelper(dateTime, 3, 2);
//        forcedWeekHelper(dateTime, 4, 3);
//        forcedWeekHelper(dateTime, 5, 4);
    }

    @Test
    public void checkIfFifthWeekPossible_IsCorrect() throws Exception {
        // Test 31 days
        for (int i = 1; i < 8; i++) {
            if (i == 1 || i == 2 || i == 3) {
                Assert.assertEquals(true, ReminderUtils.checkIfFifthWeekPossible(31, i));
            } else {
                Assert.assertEquals(false, ReminderUtils.checkIfFifthWeekPossible(31, i));
            }
        }

        // Test 30 days
        for (int i = 8; i < 15; i++) {
            if (i == 8 || i == 9) {
                Assert.assertEquals(true, ReminderUtils.checkIfFifthWeekPossible(30, i));
            } else {
                Assert.assertEquals(false, ReminderUtils.checkIfFifthWeekPossible(30, i));
            }
        }

        // Test 29 days
        for (int i = 15; i < 22; i++) {
            if (i == 15) {
                Assert.assertEquals(true, ReminderUtils.checkIfFifthWeekPossible(29, i));
            } else {
                Assert.assertEquals(false, ReminderUtils.checkIfFifthWeekPossible(29, i));
            }
        }

        // Test 28 days
        for (int i = 22; i < 29; i++) {
            Assert.assertEquals(false, ReminderUtils.checkIfFifthWeekPossible(28, i));
        }
    }

    @Test
    public void calculateYearlyReminderTime_IsCorrect() throws Exception {

        int currentYear;
        DateTime oldDateTime;
        DateTime expectedDateTime;
        DateTime currentDateTime;
        long result;

        int repeatEveryXYears;

        // DateTime to test against
        currentYear = 2001;
        repeatEveryXYears = 1;
        oldDateTime = new DateTime(currentYear, 5, 2, 11, 0);

        // Repeat yearly

        // Testing same year before the reminder date
        currentDateTime = new DateTime(currentYear, 1, 22, 17, 59);
        result = ReminderUtils.calculateYearlyReminderTime(repeatEveryXYears, oldDateTime, currentDateTime);
        Assert.assertEquals(oldDateTime.getMillis(), result);

        currentDateTime = new DateTime(currentYear, 5, 1, 1, 1);
        result = ReminderUtils.calculateYearlyReminderTime(repeatEveryXYears, oldDateTime, currentDateTime);
        Assert.assertEquals(oldDateTime.getMillis(), result);

        // Testing same year after the reminder date
        expectedDateTime = oldDateTime.plusYears(1);

        currentDateTime = new DateTime(currentYear, 5, 2, 12, 0);
        result = ReminderUtils.calculateYearlyReminderTime(repeatEveryXYears, oldDateTime, currentDateTime);
        Assert.assertEquals(expectedDateTime.getMillis(), result);

        currentDateTime = new DateTime(currentYear, 12, 31, 12, 0);
        result = ReminderUtils.calculateYearlyReminderTime(repeatEveryXYears, oldDateTime, currentDateTime);
        Assert.assertEquals(expectedDateTime.getMillis(), result);


        // Testing different years before expected reminder
        currentDateTime = new DateTime(currentYear + 1, 1, 3, 17, 12, 43, 23);
        result = ReminderUtils.calculateYearlyReminderTime(repeatEveryXYears, oldDateTime, currentDateTime);
        Assert.assertEquals(expectedDateTime.getMillis(), result);

        currentDateTime = new DateTime(currentYear + 2, 1, 3, 17, 12, 43, 23);
        result = ReminderUtils.calculateYearlyReminderTime(repeatEveryXYears, oldDateTime, currentDateTime);
        Assert.assertEquals(expectedDateTime.plusYears(1).getMillis(), result);
    }
}
