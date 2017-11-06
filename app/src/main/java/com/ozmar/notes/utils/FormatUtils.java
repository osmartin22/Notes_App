package com.ozmar.notes.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.R;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FormatUtils {

    private FormatUtils() {

    }

    @NonNull
    public static String lastUpdated(@NonNull Context context, long timeLastUpdated) {

        Date current = new Date(System.currentTimeMillis());
        Date lastUpdated = new Date(timeLastUpdated);
        SimpleDateFormat sameYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        boolean sameYear = sameYearFormat.format(current).equals(sameYearFormat.format(lastUpdated));
        boolean sameDay = false;
        boolean yesterday = false;
        boolean tomorrow = false;

        DateTime dateTime = new DateTime(timeLastUpdated);
        if (sameYear) {
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

    @NonNull
    public static String getReminderText(@NonNull Context context, @NonNull DateTime chosenDateTime) {

        String reminderDate = "";
        DateTime today = DateTime.now();
        boolean sameYear = today.getYear() == chosenDateTime.getYear();

        boolean sameDay = FormatUtils.isToday(chosenDateTime);
        boolean tomorrow = false;
        boolean yesterday = false;

        if (sameYear) {
            if (!sameDay) {
                tomorrow = FormatUtils.isTomorrow(chosenDateTime);
                if (!tomorrow) {
                    yesterday = FormatUtils.isYesterday(chosenDateTime);
                }
            }
        }

        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        Date chosen = chosenDateTime.toDate();

        if (sameYear) {
            if (sameDay) {
                reminderDate += "Today, ";
            } else if (tomorrow) {
                reminderDate += "Tomorrow, ";
            } else if (yesterday) {
                reminderDate += "Yesterday, ";
            } else {
                reminderDate += FormatUtils.getMonthDayFormatShort(chosenDateTime) + ", ";
            }
            reminderDate += timeFormat.format(chosen);

        } else {
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
            reminderDate += dateFormat.format(chosen) + ", ";
            reminderDate += timeFormat.format(chosen);
        }

        return reminderDate;
    }

    @NonNull
    public static String getMonthDayFormatShort(@NonNull DateTime chosen) {
        DateTimeFormatter dtfOut;
        LocalDate localDate = LocalDate.now();

        if (localDate.getYear() == chosen.getYear()) {
            dtfOut = DateTimeFormat.forPattern("MMM dd");
        } else {
            dtfOut = DateTimeFormat.forPattern("MMM dd, yyyy");
        }

        return dtfOut.print(chosen);
    }

    @NonNull
    public static String getMonthDayFormatShort(long millis) {
        return FormatUtils.getMonthDayFormatShort(new DateTime(millis));
    }

    @NonNull
    public static String getMonthDayFormatLong(@NonNull DateTime chosen) {
        DateTimeFormatter dtfOut;
        LocalDate localDate = LocalDate.now();

        if (localDate.getYear() == chosen.getYear()) {
            dtfOut = DateTimeFormat.forPattern("MMMM dd");
        } else {
            dtfOut = DateTimeFormat.forPattern("MMMM dd, yyyy");
        }

        return dtfOut.print(chosen);
    }

    @NonNull
    public static String getMonthDayFormatLong(long millis) {
        return getMonthDayFormatLong(new DateTime(millis));
    }

    @NonNull
    public static String getTimeFormat(@NonNull Context context, @NonNull LocalTime localTime) {
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        return timeFormat.format(localTime.toDateTimeToday().toDate());
    }

    @NonNull
    public static String getCurrentDayOfWeek(LocalDate localDate, int length) {
        String day;

        if (length == 0) {
            day = localDate.dayOfWeek().getAsShortText(Locale.getDefault());
        } else {
            day = localDate.dayOfWeek().getAsText(Locale.getDefault());
        }

        return day;
    }

    @NonNull
    public static String getChosenDayOfWeek(@NonNull LocalDate localDate, int length) {
        String day;
        if (length == 0) {
            day = localDate.dayOfWeek().getAsShortText(Locale.getDefault());
        } else {
            day = localDate.dayOfWeek().getAsText(Locale.getDefault());
        }
        return day;
    }

    @NonNull
    public static LocalTime roundToTime(@NonNull LocalTime localTime, int minute) {
        LocalTime timeToSet = localTime.minuteOfHour().roundFloorCopy();
        return timeToSet.withMinuteOfHour((timeToSet.getMinuteOfHour() / minute) * minute);
    }

    public static int getNthWeekOfMonth(int dayOfMonth) {
        int week = dayOfMonth / 7;

        if (dayOfMonth % 7 == 0) {
            return week;
        }

        return week + 1;
    }

    @NonNull
    public static String formatNthWeekOfMonth(@NonNull DateTime dateTime, int weekNumber) {
        String nthDay = "";
        boolean lastWeek = false;
        if (dateTime.getMonthOfYear() != dateTime.plusWeeks(1).getMonthOfYear()) {
            lastWeek = true;
        }

        switch (weekNumber) {
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

        nthDay += FormatUtils.getChosenDayOfWeek(dateTime.toLocalDate(), 1);

        return nthDay;
    }

    @NonNull
    public static String formatNthWeekOfMonth(@NonNull LocalDate localDate) {
        String nthDay = "";
        int week = FormatUtils.getNthWeekOfMonth(localDate.getDayOfMonth());
        boolean lastWeek = false;

        if (localDate.getMonthOfYear() != localDate.plusWeeks(1).getMonthOfYear()) {
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

        nthDay += FormatUtils.getChosenDayOfWeek(localDate, 1);

        return nthDay;
    }

    @NonNull
    public static String formatFrequencyText(@NonNull Context context, @NonNull FrequencyChoices choices, @NonNull DateTime dateTime) {
        String frequencyText = "Repeats ";

        int repeatType = choices.getRepeatEvery();
        switch (choices.getRepeatType()) {
            case 0:
                // Repeats Daily
                frequencyText += context.getResources().getQuantityString(R.plurals.repeatDay,
                        repeatType, repeatType);
                break;
            case 1:
                // Repeats Weekly (plus selected days)
                frequencyText += context.getResources().getQuantityString(R.plurals.repeatWeek,
                        repeatType, repeatType);
                assert choices.getDaysChosen() != null;
                frequencyText += FormatUtils.getSelectedDays(choices.getDaysChosen());
                break;
            case 2:
                // Repeats Monthly (plus chosen RadioButton)
                frequencyText += context.getResources().getQuantityString(R.plurals.repeatMonth,
                        repeatType, repeatType);
                if (choices.getMonthRepeatType() == 1) {
                    frequencyText += " (on every " + FormatUtils.formatNthWeekOfMonth(dateTime, choices.getMonthWeekToRepeat()) + ")";
                }
                break;
            case 3:
                // Repeats Yearly
                frequencyText += context.getResources().getQuantityString(R.plurals.repeatYear,
                        repeatType, repeatType);
                break;
        }

        if (choices.getRepeatToDate() > 0) {
            frequencyText += "; until " + FormatUtils.getMonthDayFormatShort(choices.getRepeatToDate());

        } else if (choices.getRepeatEvents() > 0) {
            frequencyText += context.getResources().getQuantityString(R.plurals.repeatEvents,
                    choices.getRepeatEvents(), choices.getRepeatEvents());
        }

        return frequencyText;
    }

    @NonNull
    public static String getSelectedDays(@NonNull List<Integer> daysChosen) {

        LocalDate localDate = LocalDate.now();
        StringBuilder sb = new StringBuilder();
        sb.append(" on ");

        if (daysChosen.size() == 1) {
            sb.append(localDate.withDayOfWeek(daysChosen.get(0)).dayOfWeek().getAsText());
        } else {
            DateTimeFormatter dtfOut = DateTimeFormat.forPattern("EEE");
            for (int i = 0; i <= daysChosen.size() - 2; i++) {
                sb.append(dtfOut.print(localDate.withDayOfWeek(daysChosen.get(i))));
                sb.append(", ");
            }
            sb.append(dtfOut.print(localDate.withDayOfWeek(daysChosen.get(daysChosen.size() - 1))));
        }

        return sb.toString();
    }

    public static boolean isToday(@NonNull DateTime time) {
        return LocalDate.now().compareTo(new LocalDate(time)) == 0;
    }

    public static boolean isTomorrow(@NonNull DateTime time) {
        return LocalDate.now().plusDays(1).compareTo(new LocalDate(time)) == 0;
    }

    public static boolean isYesterday(@NonNull DateTime time) {
        return LocalDate.now().minusDays(1).compareTo(new LocalDate(time)) == 0;
    }
}
