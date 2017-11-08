package com.ozmar.notes;


import android.content.Context;

import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class FormatUtilsWithContext {

    // Context is created with a 24 hour format
    private Context mContext = RuntimeEnvironment.application.getApplicationContext();
    private DateTime mDateTime = DateTime.now();

    @Test
    public void lastUpdated_IsCorrect() throws Exception {
        String lastUpdated;
        DateTime dateTime = mDateTime.withHourOfDay(13).withMinuteOfHour(10).withSecondOfMinute(0);

        lastUpdated = FormatUtils.lastUpdated(mContext, dateTime.getMillis());
        Assert.assertEquals("Last Updated \nToday, 13:10:00", lastUpdated);

        lastUpdated = FormatUtils.lastUpdated(mContext, dateTime.minusDays(1).getMillis());
        Assert.assertEquals("Last Updated \nYesterday, 13:10:00", lastUpdated);

        lastUpdated = FormatUtils.lastUpdated(mContext, dateTime.plusDays(1).getMillis());
        Assert.assertEquals("Last Updated \nTomorrow, 13:10:00", lastUpdated);

        lastUpdated = FormatUtils.lastUpdated(mContext, dateTime.withDate(mDateTime.getYear(), 1, 1).getMillis());
        Assert.assertEquals("Last Updated \nJan 1", lastUpdated);

        lastUpdated = FormatUtils.lastUpdated(mContext, dateTime.withDate(2001, 1, 1).getMillis());
        Assert.assertEquals("Last Updated \nJan 1, 2001", lastUpdated);
    }

    @Test
    public void getReminderText_IsCorrect() throws Exception {
        String reminderText;
        DateTime dateTime = mDateTime.withHourOfDay(13).withMinuteOfHour(10).withSecondOfMinute(0);

        reminderText = FormatUtils.getReminderText(mContext, dateTime);
        Assert.assertEquals("Today, 13:10:00", reminderText);

        reminderText = FormatUtils.getReminderText(mContext, dateTime.plusDays(1));
        Assert.assertEquals("Tomorrow, 13:10:00", reminderText);

        reminderText = FormatUtils.getReminderText(mContext, dateTime.minusDays(1));
        Assert.assertEquals("Yesterday, 13:10:00", reminderText);

        reminderText = FormatUtils.getReminderText(mContext, dateTime.withDate(mDateTime.getYear(), 1, 1));
        Assert.assertEquals("Jan 1, 13:10:00", reminderText);

        reminderText = FormatUtils.getReminderText(mContext, dateTime.withDate(2001, 1, 1));
        Assert.assertEquals("Jan 1, 2001, 13:10:00", reminderText);
    }

    @Test
    public void getTimeFormat_IsCorrect() throws Exception {
        String timeFormat;
        LocalTime localTime;

        localTime = new LocalTime(13, 15, 0);
        timeFormat = FormatUtils.getTimeFormat(mContext, localTime);
        Assert.assertEquals("13:15:00", timeFormat);

        localTime = new LocalTime(1, 15, 0);
        timeFormat = FormatUtils.getTimeFormat(mContext, localTime);
        Assert.assertEquals("01:15:00", timeFormat);
    }

    @Test
    public void formatFrequencyText_IsCorrect() throws Exception {
        FrequencyChoices choices;
        List<Integer> daysChosen = new ArrayList<>();
        daysChosen.add(1);

        // Test presets
        // Repeat daily
        choices = new FrequencyChoices(0, null);
        Assert.assertEquals("Repeats daily",
                FormatUtils.formatFrequencyText(mContext, choices, mDateTime));

        // Repeat weekly
        choices = new FrequencyChoices(1, daysChosen);
        Assert.assertEquals("Repeats weekly on Monday",
                FormatUtils.formatFrequencyText(mContext, choices, mDateTime));
        // Repeat weekly multiple days
        daysChosen.add(2);
        daysChosen.add(5);
        choices = new FrequencyChoices(1, daysChosen);
        Assert.assertEquals("Repeats weekly on Mon, Tue, Fri",
                FormatUtils.formatFrequencyText(mContext, choices, mDateTime));

        // Repeat monthly
        choices = new FrequencyChoices(2, null);
        Assert.assertEquals("Repeats monthly",
                FormatUtils.formatFrequencyText(mContext, choices, mDateTime));

        // Repeat yearly
        choices = new FrequencyChoices(3, null);
        Assert.assertEquals("Repeats yearly",
                FormatUtils.formatFrequencyText(mContext, choices, mDateTime));


        // Test repeat to date
        DateTime repeatToDateTime = new DateTime(2001, 1, 1, 1, 0);
        choices = new FrequencyChoices(0, 1, 0, repeatToDateTime.getMillis(), 0, -1, 0, 0, null);
        Assert.assertEquals("Repeats daily; until Jan 1, 2001",
                FormatUtils.formatFrequencyText(mContext, choices, mDateTime));

        choices = new FrequencyChoices(0, 1, 0, repeatToDateTime.withYear(mDateTime.getYear()).getMillis(),
                0, -1, 0, 0, null);
        Assert.assertEquals("Repeats daily; until Jan 1",
                FormatUtils.formatFrequencyText(mContext, choices, mDateTime));


        // Test repeat until X events
        choices = new FrequencyChoices(3, 1, 0, 0, 7, -1, 0, 0, null);
        Assert.assertEquals("Repeats yearly; for 7 times",
                FormatUtils.formatFrequencyText(mContext, choices, mDateTime));


        // Test repeat every
        choices = new FrequencyChoices(3, 2, 0, 0, 0, -1, 0, 0, null);
        Assert.assertEquals("Repeats every 2 years",
                FormatUtils.formatFrequencyText(mContext, choices, mDateTime));
    }

}
