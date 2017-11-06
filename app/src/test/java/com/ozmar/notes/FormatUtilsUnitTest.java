package com.ozmar.notes;

import android.support.annotation.NonNull;

import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FormatUtilsUnitTest {
    private DateTime mDateTime = DateTime.now();

    @Test
    public void getMonthDayFormatShort_IsCorrect() throws Exception {
        DateTime futureDateTime = mDateTime.plusYears(1);

        String format;
        CharSequence charSequence = ",";
        boolean containsChar;

        format = FormatUtils.getMonthDayFormatShort(mDateTime);
        containsChar = format.contains(charSequence);
        Assert.assertEquals(false, containsChar);

        format = FormatUtils.getMonthDayFormatShort(mDateTime.getMillis());
        containsChar = format.contains(charSequence);
        Assert.assertEquals(false, containsChar);

        format = FormatUtils.getMonthDayFormatShort(futureDateTime);
        containsChar = format.contains(charSequence);
        Assert.assertEquals(true, containsChar);

        format = FormatUtils.getMonthDayFormatShort(futureDateTime.getMillis());
        containsChar = format.contains(charSequence);
        Assert.assertEquals(true, containsChar);
    }

    @Test
    public void getMonthDayFormatLong_IsCorrect() throws Exception {
        DateTime futureDateTime = mDateTime.plusYears(1);

        String format;
        CharSequence charSequence = ",";
        boolean containsChar;

        format = FormatUtils.getMonthDayFormatLong(mDateTime);
        containsChar = format.contains(charSequence);
        Assert.assertEquals(false, containsChar);

        format = FormatUtils.getMonthDayFormatLong(mDateTime.getMillis());
        containsChar = format.contains(charSequence);
        Assert.assertEquals(false, containsChar);

        format = FormatUtils.getMonthDayFormatLong(futureDateTime);
        containsChar = format.contains(charSequence);
        Assert.assertEquals(true, containsChar);

        format = FormatUtils.getMonthDayFormatLong(futureDateTime.getMillis());
        containsChar = format.contains(charSequence);
        Assert.assertEquals(true, containsChar);
    }

    @Test
    public void getCurrentDayOfWeek_IsCorrect() {
        LocalDate localDate ;
        String dayShort;
        String dayLong;

        localDate = LocalDate.now().withDayOfWeek(1);     // Monday
        dayShort = FormatUtils.getCurrentDayOfWeek(localDate,0);
        dayLong = FormatUtils.getCurrentDayOfWeek(localDate,1);
        Assert.assertEquals("Mon", dayShort);
        Assert.assertEquals("Monday", dayLong);

        localDate = LocalDate.now().withDayOfWeek(5);     // Friday
        dayShort = FormatUtils.getCurrentDayOfWeek(localDate,0);
        dayLong = FormatUtils.getCurrentDayOfWeek(localDate,1);
        Assert.assertEquals("Fri", dayShort);
        Assert.assertEquals("Friday", dayLong);
    }

    @Test
    public void getChosenDayOfWeek_IsCorrect() throws Exception {
        String dayShort;
        String dayLong;
        LocalDate localDate;

        localDate = LocalDate.now().withDayOfWeek(1); // Monday
        dayShort = FormatUtils.getChosenDayOfWeek(localDate, 0);
        dayLong = FormatUtils.getChosenDayOfWeek(localDate, 1);
        Assert.assertEquals("Mon", dayShort);
        Assert.assertEquals("Monday", dayLong);

        localDate = LocalDate.now().withDayOfWeek(5); // Friday
        dayShort = FormatUtils.getChosenDayOfWeek(localDate, 0);
        dayLong = FormatUtils.getChosenDayOfWeek(localDate, 1);
        Assert.assertEquals("Fri", dayShort);
        Assert.assertEquals("Friday", dayLong);
    }

    @Test
    public void roundToTime_IsCorrect() throws Exception {
        LocalTime localTime;
        LocalTime roundedLocalTime;
        int minuteToRoundTo;

        minuteToRoundTo = 15;
        localTime = new LocalTime(12, 5, 0);
        roundedLocalTime = FormatUtils.roundToTime(localTime, minuteToRoundTo);
        Assert.assertEquals(0, roundedLocalTime.getMinuteOfHour());

        localTime = new LocalTime(12, 20, 0);
        roundedLocalTime = FormatUtils.roundToTime(localTime, minuteToRoundTo);
        Assert.assertEquals(minuteToRoundTo, roundedLocalTime.getMinuteOfHour());

        localTime = new LocalTime(12, 35, 0);
        roundedLocalTime = FormatUtils.roundToTime(localTime, minuteToRoundTo);
        Assert.assertEquals(30, roundedLocalTime.getMinuteOfHour());

        localTime = new LocalTime(12, 59, 0);
        roundedLocalTime = FormatUtils.roundToTime(localTime, minuteToRoundTo);
        Assert.assertEquals(45, roundedLocalTime.getMinuteOfHour());
    }

    @Test
    public void getNthWeekOfMonth_IsCorrect() throws Exception {
        LocalDate localDate = new LocalDate(2001, 1, 7);
        int week;

        week = FormatUtils.getNthWeekOfMonth(localDate.getDayOfMonth());
        Assert.assertEquals(1, week);

        localDate = localDate.withDayOfMonth(14);
        week = FormatUtils.getNthWeekOfMonth(localDate.getDayOfMonth());
        Assert.assertEquals(2, week);

        localDate = localDate.withDayOfMonth(21);
        week = FormatUtils.getNthWeekOfMonth(localDate.getDayOfMonth());
        Assert.assertEquals(3, week);

        localDate = localDate.withDayOfMonth(28);
        week = FormatUtils.getNthWeekOfMonth(localDate.getDayOfMonth());
        Assert.assertEquals(4, week);

        localDate = localDate.withDayOfMonth(29);
        week = FormatUtils.getNthWeekOfMonth(localDate.getDayOfMonth());
        Assert.assertEquals(5, week);
    }

    @Test
    public void formatNthWeekOfMonth_IsCorrect() throws Exception {
        LocalDate localDate = new LocalDate(2001, 1, 1);
        String result;

        result = parseResult(localDate, 7);
        Assert.assertEquals("first", result);

        result = parseResult(localDate, 14);
        Assert.assertEquals("second", result);

        result = parseResult(localDate, 21);
        Assert.assertEquals("third", result);

        result = parseResult(localDate, 22);
        Assert.assertEquals("fourth", result);

        result = parseResult(localDate, 25);
        Assert.assertEquals("last", result);

        result = parseResult(localDate, 29);
        Assert.assertEquals("last", result);

        result = parseResult(localDate, 31);
        Assert.assertEquals("last", result);

        // Move to February with 28 days (Not a leap year)
        localDate = localDate.plusMonths(1);
        result = parseResult(localDate, 22);
        Assert.assertEquals("last", result);

    }

    @NonNull
    private String parseResult(@NonNull LocalDate localDate, int dayOfMonth) {
        String result = FormatUtils.formatNthWeekOfMonth(localDate.withDayOfMonth(dayOfMonth));
        return result.substring(0, result.indexOf(" "));
    }

    @Test
    public void getSelectedDays_IsCorrect() throws Exception {
        List<Integer> daysChosen;
        String result;
        String expected;

        // NOTE: 7(Sunday) should be put first if used
        expected = " on Mon, Tue, Wed";
        daysChosen = new ArrayList<>(Arrays.asList(1, 2, 3));
        result = FormatUtils.getSelectedDays(daysChosen);
        Assert.assertEquals(expected, result);

        daysChosen.add(5);
        daysChosen.add(6);
        expected = " on Mon, Tue, Wed, Fri, Sat";
        result = FormatUtils.getSelectedDays(daysChosen);
        Assert.assertEquals(expected, result);

        expected = " on Sun, Tue, Thu, Fri, Sat";
        daysChosen = new ArrayList<>(Arrays.asList(7, 2, 4, 5, 6));
        result = FormatUtils.getSelectedDays(daysChosen);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void isToday_IsCorrect() throws Exception {
        boolean isToday;

        isToday = FormatUtils.isToday(mDateTime);
        Assert.assertEquals(true, isToday);

        isToday = FormatUtils.isToday(mDateTime.plusDays(1));
        Assert.assertEquals(false, isToday);
    }

    @Test
    public void isTomorrow() throws Exception {
        boolean isTomorrow;

        isTomorrow = FormatUtils.isTomorrow(mDateTime);
        Assert.assertEquals(false, isTomorrow);

        isTomorrow = FormatUtils.isTomorrow(mDateTime.plusDays(1));
        Assert.assertEquals(true, isTomorrow);
    }

    @Test
    public void isYesterday_IsCorrect() throws Exception {
        boolean isYesterday;

        isYesterday = FormatUtils.isYesterday(mDateTime);
        Assert.assertEquals(false, isYesterday);

        isYesterday = FormatUtils.isYesterday(mDateTime.minusDays(1));
        Assert.assertEquals(true, isYesterday);
    }
}
