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
        DateTime dateTime = new DateTime(timeLastUpdated);

        boolean sameYear = LocalDate.now().getYear() == dateTime.getYear();
        boolean sameDay = false;
        boolean tomorrow = false;
        boolean yesterday = false;

        if (sameYear) {
            sameDay = FormatUtils.isToday(dateTime.toLocalDate());
            if (!sameDay) {
                yesterday = FormatUtils.isYesterday(dateTime.toLocalDate());
                if (!yesterday) {
                    tomorrow = FormatUtils.isTomorrow(dateTime.toLocalDate());
                }
            }
        }

        String timeModified = context.getString(R.string.lastUpdatedFormatUtils);

        Date lastUpdated = dateTime.toDate();
        if (sameYear) {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            if (sameDay) {
                timeModified += context.getString(R.string.todayFormatUtils) + timeFormat.format(lastUpdated);
            } else if (yesterday) {
                timeModified += context.getString(R.string.yesterdayFormatUtils) + timeFormat.format(lastUpdated);

            } else if (tomorrow) {  // Only used if phone had date changed to the past
                timeModified += context.getString(R.string.tomorrowFormatUtils) + timeFormat.format(lastUpdated);
            } else {
                SimpleDateFormat df = new SimpleDateFormat("MMM d", Locale.getDefault());
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

        boolean sameYear = LocalDate.now().getYear() == chosenDateTime.getYear();
        boolean sameDay = FormatUtils.isToday(chosenDateTime.toLocalDate());
        boolean tomorrow = false;
        boolean yesterday = false;

        if (sameYear) {
            if (!sameDay) {
                tomorrow = FormatUtils.isTomorrow(chosenDateTime.toLocalDate());
                if (!tomorrow) {
                    yesterday = FormatUtils.isYesterday(chosenDateTime.toLocalDate());
                }
            }
        }

        String reminderDate;
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        Date chosen = chosenDateTime.toDate();

        if (sameYear) {
            if (sameDay) {
                reminderDate = context.getString(R.string.todayFormatUtils);
            } else if (tomorrow) {
                reminderDate = context.getString(R.string.tomorrowFormatUtils);
            } else if (yesterday) {
                reminderDate = context.getString(R.string.yesterdayFormatUtils);
            } else {
                reminderDate = FormatUtils.getMonthDayFormatShort(chosenDateTime) + ", ";
            }
            reminderDate += timeFormat.format(chosen);

        } else {
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
            reminderDate = dateFormat.format(chosen) + ", ";
            reminderDate += timeFormat.format(chosen);
        }

        return reminderDate;
    }

    @NonNull
    public static String getMonthDayFormatShort(@NonNull DateTime chosen) {
        DateTimeFormatter dtfOut;
        LocalDate localDate = LocalDate.now();

        if (localDate.getYear() == chosen.getYear()) {
            dtfOut = DateTimeFormat.forPattern("MMM d");
        } else {
            dtfOut = DateTimeFormat.forPattern("MMM d, yyyy");
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
            dtfOut = DateTimeFormat.forPattern("MMMM d");
        } else {
            dtfOut = DateTimeFormat.forPattern("MMMM d, yyyy");
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
        boolean lastWeek = false;
        if (dateTime.getMonthOfYear() != dateTime.plusWeeks(1).getMonthOfYear()) {
            lastWeek = true;
        }

        return weekHelper(dateTime.toLocalDate(), weekNumber, lastWeek, 1);
    }

    @NonNull
    public static String formatNthWeekOfMonth(@NonNull LocalDate localDate) {
        int weekNumber = FormatUtils.getNthWeekOfMonth(localDate.getDayOfMonth());

        boolean lastWeek = false;
        if (localDate.getMonthOfYear() != localDate.plusWeeks(1).getMonthOfYear()) {
            lastWeek = true;
        }

        return weekHelper(localDate, weekNumber, lastWeek, 1);
    }

    private static String weekHelper(LocalDate localDate, int weekNumber, boolean lastWeek, int weekLength) {
        String nthDay;

        switch (weekNumber) {
            case 1:
            default:
                nthDay = "first ";
                break;
            case 2:
                nthDay = "second ";
                break;
            case 3:
                nthDay = "third ";
                break;
            case 4:
                if (lastWeek) {
                    nthDay = "last ";
                } else {
                    nthDay = "fourth ";
                }
                break;
            case 5:
                nthDay = "last ";
                break;
        }

        nthDay += FormatUtils.getChosenDayOfWeek(localDate, weekLength);

        return nthDay;
    }

    @NonNull
    public static String formatFrequencyText(@NonNull Context context, @NonNull FrequencyChoices choices, @NonNull DateTime dateTime) {
        String frequencyText = context.getString(R.string.repeats);

        int repeatType = choices.getRepeatEvery();
        switch (choices.getRepeatType()) {
            case 1:
                // Repeats Daily
                frequencyText += context.getResources().getQuantityString(R.plurals.repeatDay,
                        repeatType, repeatType);
                break;
            case 2:
                // Repeats Weekly (plus selected days)
                frequencyText += context.getResources().getQuantityString(R.plurals.repeatWeek,
                        repeatType, repeatType);
                assert choices.getDaysChosen() != null;
                frequencyText += FormatUtils.getSelectedDays(choices.getDaysChosen());
                break;
            case 3:
                // Repeats Monthly (plus weekHelper RadioButton)
                frequencyText += context.getResources().getQuantityString(R.plurals.repeatMonth,
                        repeatType, repeatType);
                if (choices.getMonthRepeatType() == 1) {
                    frequencyText += context.getString(R.string.repeatEvery)
                            + FormatUtils.formatNthWeekOfMonth(dateTime, choices.getMonthWeekToRepeat()) + ")";
                }
                break;
            case 4:
                // Repeats Yearly
                frequencyText += context.getResources().getQuantityString(R.plurals.repeatYear,
                        repeatType, repeatType);
                break;
        }

        if (choices.getRepeatToDate() > 0) {
            frequencyText += context.getString(R.string.repeatUntil)
                    + FormatUtils.getMonthDayFormatShort(choices.getRepeatToDate());

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

    public static boolean isToday(@NonNull LocalDate time) {
        return LocalDate.now().compareTo(time) == 0;
    }

    public static boolean isTomorrow(@NonNull LocalDate time) {
        return LocalDate.now().plusDays(1).compareTo(time) == 0;
    }

    public static boolean isYesterday(@NonNull LocalDate time) {
        return LocalDate.now().minusDays(1).compareTo(time) == 0;
    }
}
