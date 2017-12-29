package com.ozmar.notes.utilsTest;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.utils.ReminderUtils;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
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

        result = ReminderUtils.getNextDailyReminderTime(1, dateTime);
        Assert.assertEquals(dateTime.plusDays(1).getMillis(), result);

        result = ReminderUtils.getNextDailyReminderTime(21, dateTime);
        Assert.assertEquals(dateTime.plusDays(21).getMillis(), result);

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

    private void nextMonthlyReminderHelper(DateTime dateTime, int weekNumberToForce, int dayOfWeek,
                                           DateTime expectedDateTime, int repeatEveryXMonths) {

        DateTime newDateTime = new DateTime(ReminderUtils.getNextMonthlyReminder(dateTime,
                repeatEveryXMonths, weekNumberToForce, dayOfWeek));
        Assert.assertEquals(expectedDateTime, newDateTime);

    }

    @Test
    public void getNextMonthlyReminder_IsCorrect() throws Exception {
        DateTime dateTime = new DateTime(2017, 11, 1, 12, 0);
        DateTime expectedDateTime;

        // Testing dates before expected reminder occurrence
        expectedDateTime = dateTime.withDayOfMonth(17);
        nextMonthlyReminderHelper(dateTime, 3, 5, expectedDateTime, 1);
        nextMonthlyReminderHelper(dateTime, 3, 5, expectedDateTime, 1);

        expectedDateTime = dateTime.withDayOfMonth(28);
        nextMonthlyReminderHelper(dateTime, 5, 2, expectedDateTime, 1);

        expectedDateTime = dateTime.withDayOfMonth(23);
        nextMonthlyReminderHelper(dateTime, 4, 4, expectedDateTime, 1);


        // Testing dates after expected reminder occurrence(go to next month)
        dateTime = dateTime.withDayOfMonth(15);
        expectedDateTime = new DateTime(2017, 12, 11, 12, 0);
        nextMonthlyReminderHelper(dateTime, 2, 1, expectedDateTime, 1);

        expectedDateTime = new DateTime(2018, 1, 8, 12, 0);
        nextMonthlyReminderHelper(dateTime, 2, 1, expectedDateTime, 2);

        expectedDateTime = new DateTime(2018, 5, 2, 12, 0);
        nextMonthlyReminderHelper(dateTime, 1, 3, expectedDateTime, 6);

        // Testing the same date returns the next expected occurrence
        dateTime.withDayOfMonth(15);
        expectedDateTime = new DateTime(2017, 12, 20, 12, 0);
        nextMonthlyReminderHelper(dateTime, 3, 3, expectedDateTime, 1);

        expectedDateTime = new DateTime(2018, 2, 21, 12, 0);
        nextMonthlyReminderHelper(dateTime, 3, 3, expectedDateTime, 3);
    }

    @Test
    public void getDayInCurrentMonth_IsCorrect() throws Exception {
        LocalDate localDate;

        localDate = new LocalDate(2017, 11, 1);
        Assert.assertEquals(1, ReminderUtils.getDayInCurrentMonth(localDate, 11, WEDNESDAY));
        Assert.assertEquals(2, ReminderUtils.getDayInCurrentMonth(localDate, 11, THURSDAY));
        Assert.assertEquals(3, ReminderUtils.getDayInCurrentMonth(localDate, 11, FRIDAY));
        Assert.assertEquals(4, ReminderUtils.getDayInCurrentMonth(localDate, 11, SATURDAY));
        Assert.assertEquals(5, ReminderUtils.getDayInCurrentMonth(localDate, 11, SUNDAY));
        Assert.assertEquals(6, ReminderUtils.getDayInCurrentMonth(localDate, 11, MONDAY));
        Assert.assertEquals(7, ReminderUtils.getDayInCurrentMonth(localDate, 11, TUESDAY));

        localDate = localDate.withDayOfMonth(22);
        Assert.assertEquals(20, ReminderUtils.getDayInCurrentMonth(localDate, 11, MONDAY));
        Assert.assertEquals(22, ReminderUtils.getDayInCurrentMonth(localDate, 11, WEDNESDAY));
        Assert.assertEquals(24, ReminderUtils.getDayInCurrentMonth(localDate, 11, FRIDAY));
        Assert.assertEquals(26, ReminderUtils.getDayInCurrentMonth(localDate, 11, SUNDAY));

        localDate = localDate.withDayOfMonth(27);
        Assert.assertEquals(27, ReminderUtils.getDayInCurrentMonth(localDate, 11, MONDAY));
        Assert.assertEquals(30, ReminderUtils.getDayInCurrentMonth(localDate, 11, THURSDAY));

        // NOTE: 31 not possible in current month, should revert to last
        Assert.assertEquals(24, ReminderUtils.getDayInCurrentMonth(localDate, 11, FRIDAY));

        localDate = new LocalDate(2018, 1, 1);
        Assert.assertEquals(1, ReminderUtils.getDayInCurrentMonth(localDate, 1, MONDAY));
        Assert.assertEquals(2, ReminderUtils.getDayInCurrentMonth(localDate, 1, TUESDAY));
        Assert.assertEquals(3, ReminderUtils.getDayInCurrentMonth(localDate, 1, WEDNESDAY));
        Assert.assertEquals(4, ReminderUtils.getDayInCurrentMonth(localDate, 1, THURSDAY));
        Assert.assertEquals(5, ReminderUtils.getDayInCurrentMonth(localDate, 1, FRIDAY));
        Assert.assertEquals(6, ReminderUtils.getDayInCurrentMonth(localDate, 1, SATURDAY));
        Assert.assertEquals(7, ReminderUtils.getDayInCurrentMonth(localDate, 1, SUNDAY));

        localDate = localDate.withDayOfMonth(16);
        Assert.assertEquals(17, ReminderUtils.getDayInCurrentMonth(localDate, 1, WEDNESDAY));
        Assert.assertEquals(21, ReminderUtils.getDayInCurrentMonth(localDate, 1, SUNDAY));

        localDate = localDate.withDayOfMonth(29);
        Assert.assertEquals(29, ReminderUtils.getDayInCurrentMonth(localDate, 1, MONDAY));
        Assert.assertEquals(30, ReminderUtils.getDayInCurrentMonth(localDate, 1, TUESDAY));
        Assert.assertEquals(31, ReminderUtils.getDayInCurrentMonth(localDate, 1, WEDNESDAY));
    }

    @Test
    public void nthWeekDayOfMonth_IsCorrect() throws Exception {
        LocalDate localDate;

        localDate = new LocalDate(2017, 11, 1);

        Assert.assertEquals(1, ReminderUtils.nthWeekDayOfMonth(localDate, WEDNESDAY, 1));
        Assert.assertEquals(2, ReminderUtils.nthWeekDayOfMonth(localDate, THURSDAY, 1));
        Assert.assertEquals(6, ReminderUtils.nthWeekDayOfMonth(localDate, MONDAY, 1));
        Assert.assertEquals(7, ReminderUtils.nthWeekDayOfMonth(localDate, TUESDAY, 1));

        Assert.assertEquals(8, ReminderUtils.nthWeekDayOfMonth(localDate, WEDNESDAY, 2));
        Assert.assertEquals(13, ReminderUtils.nthWeekDayOfMonth(localDate, MONDAY, 2));

        Assert.assertEquals(17, ReminderUtils.nthWeekDayOfMonth(localDate, FRIDAY, 3));
        Assert.assertEquals(19, ReminderUtils.nthWeekDayOfMonth(localDate, SUNDAY, 3));

        Assert.assertEquals(22, ReminderUtils.nthWeekDayOfMonth(localDate, WEDNESDAY, 4));
        Assert.assertEquals(23, ReminderUtils.nthWeekDayOfMonth(localDate, THURSDAY, 4));

        Assert.assertEquals(27, ReminderUtils.nthWeekDayOfMonth(localDate, MONDAY, 5));
        Assert.assertEquals(28, ReminderUtils.nthWeekDayOfMonth(localDate, TUESDAY, 5));
        Assert.assertEquals(30, ReminderUtils.nthWeekDayOfMonth(localDate, THURSDAY, 5));
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

    // Unit test for second method
//    private void yearlyReminderHelper(DateTime current, DateTime expected, int repeatEveryXYears) {
//        long result = ReminderUtils.getNextYearlyReminder(repeatEveryXYears, expected, current);
//        Assert.assertEquals(expected.getMillis(), result);
//    }
//
//    @Test
//    public void getNextYearlyReminder_IsCorrect() throws Exception {
//        DateTime expectedDateTime = new DateTime(2001, 5, 2, 11, 0);
//        DateTime currentDateTime;
//
//        // Repeat yearly
//        currentDateTime = expectedDateTime.minusMonths(2);
//        yearlyReminderHelper(currentDateTime, expectedDateTime, 1);
//
//        currentDateTime.minusDays(1);
//        yearlyReminderHelper(currentDateTime, expectedDateTime, 1);
//
//        // Testing same year after the reminder date
//        expectedDateTime = expectedDateTime.withYear(2002);
//
//        currentDateTime = new DateTime(2001, 5, 2, 12, 0);
//        yearlyReminderHelper(currentDateTime, expectedDateTime, 1);
//
//        currentDateTime = new DateTime(2001, 12, 31, 12, 0);
//        yearlyReminderHelper(currentDateTime, expectedDateTime, 1);
//
//
//        // Testing different years before expected reminder
//        currentDateTime = new DateTime(2002, 1, 3, 17, 12, 43, 23);
//        yearlyReminderHelper(currentDateTime, expectedDateTime, 1);
//    }

    @Test
    public void getNextYearlyReminder_IsCorrect() throws Exception {
        DateTime dateTime = new DateTime(2017, 11, 9, 12, 0);
        long result;

        result = ReminderUtils.getNextYearlyReminder(dateTime,1);
        Assert.assertEquals(dateTime.plusYears(1).getMillis(), result);

        result = ReminderUtils.getNextYearlyReminder(dateTime,3);
        Assert.assertEquals(dateTime.plusYears(3).getMillis(), result);
    }


    @Test
    public void getNextRepeatReminder_IsCorrect() throws Exception {
        FrequencyChoices choices;
        DateTime dateTime = new DateTime(2001, 5, 2, 12, 0);

        long expectedTime;
        long resultTime;

        choices = new FrequencyChoices(0, null);
        expectedTime = dateTime.plusDays(1).getMillis();
        resultTime = ReminderUtils.getNextRepeatReminder(choices, dateTime.getMillis());
        Assert.assertEquals(expectedTime, resultTime);

        List<Integer> daysChosen = new ArrayList<>(Collections.singletonList(dateTime.getDayOfWeek()));
        choices = new FrequencyChoices(1, daysChosen);
        expectedTime = dateTime.plusWeeks(1).getMillis();
        resultTime = ReminderUtils.getNextRepeatReminder(choices, dateTime.getMillis());
        Assert.assertEquals(expectedTime, resultTime);

        choices = new FrequencyChoices(2, null);
        expectedTime = dateTime.plusMonths(1).getMillis();
        resultTime = ReminderUtils.getNextRepeatReminder(choices, dateTime.getMillis());
        Assert.assertEquals(expectedTime, resultTime);

        choices = new FrequencyChoices(3, null);
        expectedTime = dateTime.plusYears(1).getMillis();
        resultTime = ReminderUtils.getNextRepeatReminder(choices, dateTime.getMillis());
        Assert.assertEquals(expectedTime, resultTime);
    }
}
