package com.ozmar.notes;

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
        FrequencyChoices choices;
        long result;
        int repeatEvery;

        repeatEvery = 1;
        choices = new FrequencyChoices(0, repeatEvery, 1, 0, 0, -1, 0, 0, null);
        result = ReminderUtils.calculateDailyReminderTime(choices, dateTime);
        Assert.assertEquals(dateTime.plusDays(repeatEvery).getMillis(), result);

        repeatEvery = 21;
        choices = new FrequencyChoices(0, repeatEvery, 1, 0, 0, -1, 0, 0, null);
        result = ReminderUtils.calculateDailyReminderTime(choices, dateTime);
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

        // Test when the currentDayOfWeek is the only day in the list
        daysChosen = new ArrayList<>(Collections.singletonList(MONDAY));
        weeklyReminderHelper(MONDAY, 7, 1, daysChosen);
        weeklyReminderHelper(MONDAY, 21, 3, daysChosen);
        weeklyReminderHelper(MONDAY, 0, 0, daysChosen);


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

    @Test
    public void getForcedWeek_IsCorrect() throws Exception {

    }

    @Test
    public void checkIfFifthWeekPossible_IsCorrect() throws Exception {

    }

    @Test
    public void calculateYearlyReminderTime_IsCorrect() throws Exception {

    }
}
