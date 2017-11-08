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

        daysChosen = new ArrayList<>(Arrays.asList(MONDAY, WEDNESDAY, FRIDAY, SUNDAY));
        nextDayPositionHelper(daysChosen, MONDAY, WEDNESDAY);
        nextDayPositionHelper(daysChosen, WEDNESDAY, FRIDAY);
        nextDayPositionHelper(daysChosen, FRIDAY, SUNDAY);
        nextDayPositionHelper(daysChosen, SUNDAY, MONDAY);

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

        daysChosen = new ArrayList<>(Collections.singletonList(MONDAY));
        weeklyReminderHelper(MONDAY, 21, 3, daysChosen);
        weeklyReminderHelper(MONDAY, 0, 0, daysChosen);
        weeklyReminderHelper(TUESDAY, 6, 1, daysChosen);
        weeklyReminderHelper(SUNDAY, 8, 2, daysChosen);


        daysChosen = new ArrayList<>(Arrays.asList(MONDAY, WEDNESDAY, SATURDAY));
        weeklyReminderHelper(MONDAY, 2, 1, daysChosen);
        weeklyReminderHelper(TUESDAY, 1, 71, daysChosen);
        weeklyReminderHelper(WEDNESDAY, 3, 11, daysChosen);
        weeklyReminderHelper(THURSDAY, 2, 0, daysChosen);
        weeklyReminderHelper(FRIDAY, 1, 21, daysChosen);


        daysChosen = new ArrayList<>(Arrays.asList(MONDAY, WEDNESDAY, THURSDAY));
        weeklyReminderHelper(FRIDAY, 3, 1, daysChosen);
        weeklyReminderHelper(FRIDAY, 31, 5, daysChosen);
        weeklyReminderHelper(SATURDAY, 16, 3, daysChosen);
        weeklyReminderHelper(SATURDAY, 72, 11, daysChosen);
        weeklyReminderHelper(SUNDAY, 22, 4, daysChosen);
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

    private void forcedWeekDateHelper(DateTime dateTime, int weekNumberToForce, int weeksToAdd) {
        int currentWeekNumber = FormatUtils.getNthWeekOfMonth(dateTime.getDayOfMonth());
        long result = ReminderUtils.getForcedWeekDate(dateTime, weekNumberToForce, currentWeekNumber);
        Assert.assertEquals(dateTime.plusWeeks(weeksToAdd).getMillis(), result);
    }

    @Test
    public void getForcedWeekDate_IsCorrect() throws Exception {
        DateTime dateTime = new DateTime(2017, 11, 1, 12, 0);

        // First week of the month
        // 5 weeks possible for starting day
        for (int i = 1; i < 6; i++) {
            forcedWeekDateHelper(dateTime, i, i - 1);
        }

        // 5 weeks not possible for starting day
        dateTime = dateTime.withDayOfMonth(7);
        for (int i = 1; i < 5; i++) {
            forcedWeekDateHelper(dateTime, i, i - 1);
        }
        forcedWeekDateHelper(dateTime, 5, 3);


        // Third week of the month
        // Test when forceWeekNumber is after currentWeekNumber
        dateTime = dateTime.withDayOfMonth(15); // 5 weeks possible for given day
        for (int i = 3; i < 6; i++) {
            forcedWeekDateHelper(dateTime, i, i - 3);
        }

        // Test when forceWeekNumber is before the currentWeekNumber
        forcedWeekDateHelper(dateTime, 1, 3);
        forcedWeekDateHelper(dateTime, 2, 4);

        // TODO: Test 4 weeks possible by trying to force a 5 week
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

    private void yearlyReminderHelper(DateTime current, DateTime expected, int repeatEveryXYears) {
        long result = ReminderUtils.calculateYearlyReminderTime(repeatEveryXYears, expected, current);
        Assert.assertEquals(expected.getMillis(), result);
    }

    @Test
    public void calculateYearlyReminderTime_IsCorrect() throws Exception {
        DateTime expectedDateTime;
        DateTime currentDateTime;
        DateTime reminderDate = new DateTime(2001, 5, 2, 11, 0);

        // Repeat yearly
        currentDateTime = reminderDate.minusMonths(2);
        yearlyReminderHelper(currentDateTime, reminderDate, 1);

        currentDateTime.minusDays(1);
        yearlyReminderHelper(currentDateTime, reminderDate, 1);

        // Testing same year after the reminder date
        expectedDateTime = reminderDate.plusYears(1);

        currentDateTime = new DateTime(2001, 5, 2, 12, 0);
        yearlyReminderHelper(currentDateTime, expectedDateTime, 1);

        currentDateTime = new DateTime(2001, 12, 31, 12, 0);
        yearlyReminderHelper(currentDateTime, expectedDateTime, 1);


        // Testing different years before expected reminder
        currentDateTime = new DateTime(2001 + 1, 1, 3, 17, 12, 43, 23);
        yearlyReminderHelper(currentDateTime, expectedDateTime, 1);

        currentDateTime = new DateTime(2001 + 2, 1, 3, 17, 12, 43, 23);
        yearlyReminderHelper(currentDateTime, expectedDateTime.plusYears(1), 1);
    }
}
