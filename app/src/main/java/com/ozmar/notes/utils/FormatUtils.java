package com.ozmar.notes.utils;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class FormatUtils {

    private FormatUtils() {

    }

    public static String lastUpdated(Context context, long timeLastUpdated) {

        Date current = new Date(System.currentTimeMillis());
        Date lastUpdated = new Date(timeLastUpdated);
        SimpleDateFormat sameYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        boolean sameYear = sameYearFormat.format(current).equals(sameYearFormat.format(lastUpdated));
        boolean sameDay = false;
        boolean yesterday = false;
        boolean tomorrow = false;

        DateTime dateTime = new DateTime(timeLastUpdated);
        if (sameYear) {              // Only get boolean if needed
            sameDay = FormatUtils.isToday(dateTime);
            if (!sameDay) {
                yesterday = FormatUtils.isYesterday(dateTime);
                if (!yesterday) {
                    tomorrow = FormatUtils.isTomorrow(dateTime);
                }
            }
        }

        String timeModified = "Last Updated \n";

        if (sameYear) {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            if (sameDay) {
                timeModified += "Today, " + timeFormat.format(lastUpdated);
            } else if (yesterday) {
                timeModified += "Yesterday, " + timeFormat.format(lastUpdated);
            } else if (tomorrow) {
                timeModified += "Tomorrow, " + timeFormat.format(lastUpdated);
            } else {
                SimpleDateFormat df = new SimpleDateFormat("MMM dd", Locale.getDefault());
                timeModified += df.format(lastUpdated);
            }
        } else {
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
            timeModified += dateFormat.format(lastUpdated);
        }

        return timeModified;
    }

    public static String getReminderText(Context context, DateTime dateTime) {

        String reminderDate = "";
        DateTime today = DateTime.now();
        boolean sameYear = today.getYear() == dateTime.getYear();

        boolean sameDay = FormatUtils.isToday(dateTime);
        boolean tomorrow = false;
        boolean yesterday = false;

        if (sameYear) {      // Only get boolean if needed
            if (!sameDay) {
                tomorrow = FormatUtils.isTomorrow(dateTime);
                if (!tomorrow) {
                    yesterday = FormatUtils.isYesterday(dateTime);
                }
            }
        }

        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        Date chosen = dateTime.toDate();

        if (sameYear) {
            if (sameDay) {
                reminderDate += "Today, ";
            } else if (tomorrow) {
                reminderDate += "Tomorrow, ";
            } else if (yesterday) {
                reminderDate += "Yesterday, ";
            } else {
                DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MMM dd, ");
                reminderDate += dtfOut.print(dateTime);
            }
            reminderDate += timeFormat.format(chosen);

        } else {
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
            reminderDate += dateFormat.format(chosen) + ", ";
            reminderDate += timeFormat.format(chosen);
        }

        return reminderDate;
    }

    // Use if you know the dates are in the same year
    public static String getMonthDayFormat(DateTime chosen) {
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MMMM dd");
        return dtfOut.print(chosen);
    }

    // Used to check if the chosen date is within the same year
    public static String getMonthDayFormat(DateTime dateTimeNow, DateTime chosen) {
        String date;

        if (dateTimeNow.getYear() == chosen.getYear()) {
            date = FormatUtils.getMonthDayFormat(chosen);
        } else {
            DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MMMM dd, yyyy");
            date = dtfOut.print(chosen);
        }
        return date;
    }

    public static String getTimeFormat(Context context, LocalTime dateTime) {
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        Date date = dateTime.toDateTimeToday().toDate();
        return timeFormat.format(date);
    }

    public static String getDayOfWeek(DateTime dateTime, int type) {
        String day;
        if (type == 0) {
            day = dateTime.dayOfWeek().getAsShortText(Locale.getDefault());
        } else {
            day = dateTime.dayOfWeek().getAsText(Locale.getDefault());
        }
        return day;
    }

    public static LocalTime roundToTime(LocalTime localTime, int minute) {
        // Remove seconds and milliseconds
        LocalTime timeToSet = localTime.minuteOfHour().roundFloorCopy();
        return timeToSet.withMinuteOfHour((timeToSet.getMinuteOfHour() / minute) * minute);
    }

    public static String formatNthDayOfMonthItIs(DateTime dateTime) {
        String nthDay = "";
        int week = (dateTime.getDayOfMonth() / 7) + 1;
        boolean lastWeek = false;

        if (dateTime.getMonthOfYear() != dateTime.plusWeeks(1).getMonthOfYear()) {
            lastWeek = true;
        }

        switch (week) {
            case 1:
                nthDay += "first ";
                break;
            case 2:
                nthDay += "second ";
                break;
            case 3:
                nthDay += "third ";
                break;
            case 4:
                if (lastWeek) {
                    nthDay += "last ";
                } else {
                    nthDay += "fourth ";
                }
                break;
            case 5:
                nthDay += "last ";
                break;
        }

        nthDay += FormatUtils.getDayOfWeek(dateTime, 1);

        return nthDay;
    }

    private static boolean isToday(DateTime time) {
        return LocalDate.now().compareTo(new LocalDate(time)) == 0;
    }

    private static boolean isTomorrow(DateTime time) {
        return LocalDate.now().plusDays(1).compareTo(new LocalDate(time)) == 0;
    }

    private static boolean isYesterday(DateTime time) {
        return LocalDate.now().minusDays(1).compareTo(new LocalDate(time)) == 0;
    }
}
