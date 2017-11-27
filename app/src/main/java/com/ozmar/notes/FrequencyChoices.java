package com.ozmar.notes;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FrequencyChoices implements Parcelable {

    @ColumnInfo(name = "repeatType")
    private int repeatType = -1;     // day/week/month/year

    @ColumnInfo(name = "repeatEvery")
    private int repeatEvery;    // Repeat every X (day/week/month/year)


    @ColumnInfo(name = "repeatForever")
    private int repeatForever;

    @ColumnInfo(name = "repeatToDate")
    private long repeatToDate;

    @ColumnInfo(name = "repeatEvents")
    private int repeatEvents;   // Repeat reminder for X events(times)


    @ColumnInfo(name = "monthRepeatType")
    private int monthRepeatType;

    @ColumnInfo(name = "monthWeekToRepeat")
    private int monthWeekToRepeat;

    @ColumnInfo(name = "monthDayOfWeekToRepeat")
    private int monthDayOfWeekToRepeat;

    @ColumnInfo(name = "daysChosen")
    private List<Integer> daysChosen;

    @ColumnInfo(name = "repeatEventsOccurred")
    private int repeatEventsOccurred;

    public FrequencyChoices() {

    }

    // Constructor for presets
    @Ignore
    public FrequencyChoices(@IntRange(from = 0, to = 4) int repeatType, @Nullable List<Integer> daysChosen) {
        this.repeatType = repeatType;
        this.repeatEvery = 1;
        this.repeatForever = 1;
        this.repeatToDate = 0;
        this.repeatEvents = 0;
        this.monthWeekToRepeat = 0;
        this.monthDayOfWeekToRepeat = 0;
        this.daysChosen = daysChosen;

        if (repeatType == 2) {
            this.monthRepeatType = 0;
        } else {
            this.monthRepeatType = -1;
        }

        this.repeatEventsOccurred = 0;
    }

    @Ignore
    public FrequencyChoices(@IntRange(from = 0, to = 4) int repeatType, int repeatEvery,
                            @IntRange(from = 0, to = 1) int repeatForever, long repeatToDate, int repeatEvents,
                            @IntRange(from = -1, to = 1) int monthRepeatType,
                            @IntRange(from = 0, to = 5) int monthWeekToRepeat,
                            @IntRange(from = 0, to = 7) int monthDayOfWeekToRepeat, @Nullable List<Integer> daysChosen) {
        this.repeatType = repeatType;
        this.repeatEvery = repeatEvery;
        this.repeatForever = repeatForever;
        this.repeatToDate = repeatToDate;
        this.repeatEvents = repeatEvents;
        this.monthRepeatType = monthRepeatType;
        this.monthWeekToRepeat = monthWeekToRepeat;
        this.monthDayOfWeekToRepeat = monthDayOfWeekToRepeat;
        this.daysChosen = daysChosen;
        this.repeatEventsOccurred = 0;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }

    public int getRepeatEvery() {
        return repeatEvery;
    }

    public void setRepeatEvery(int repeatEvery) {
        this.repeatEvery = repeatEvery;
    }

    public int getRepeatForever() {
        return repeatForever;
    }

    public void setRepeatForever(int repeatForever) {
        this.repeatForever = repeatForever;
    }

    public long getRepeatToDate() {
        return repeatToDate;
    }

    public void setRepeatToDate(long repeatToDate) {
        this.repeatToDate = repeatToDate;
    }

    public int getRepeatEvents() {
        return repeatEvents;
    }

    public void setRepeatEvents(int repeatEvents) {
        this.repeatEvents = repeatEvents;
    }

    public int getMonthRepeatType() {
        return monthRepeatType;
    }

    public void setMonthRepeatType(int monthRepeatType) {
        this.monthRepeatType = monthRepeatType;
    }

    public int getMonthWeekToRepeat() {
        return monthWeekToRepeat;
    }

    public void setMonthWeekToRepeat(int monthWeekToRepeat) {
        this.monthWeekToRepeat = monthWeekToRepeat;
    }

    public int getMonthDayOfWeekToRepeat() {
        return monthDayOfWeekToRepeat;
    }

    public void setMonthDayOfWeekToRepeat(int monthDayOfWeekToRepeat) {
        this.monthDayOfWeekToRepeat = monthDayOfWeekToRepeat;
    }

    @Nullable
    public List<Integer> getDaysChosen() {
        return daysChosen;
    }

    public void setDaysChosen(List<Integer> daysChosen) {
        this.daysChosen = daysChosen;
    }

    public int getRepeatEventsOccurred() {
        return repeatEventsOccurred;
    }

    public void setRepeatEventsOccurred(int repeatEventsOccurred) {
        this.repeatEventsOccurred = repeatEventsOccurred;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof FrequencyChoices)) {
            return false;
        }

        FrequencyChoices choices = (FrequencyChoices) obj;

        return choices.repeatType == repeatType &&
                choices.repeatEvery == repeatEvery &&
                choices.repeatForever == repeatForever &&
                choices.repeatToDate == repeatToDate &&
                choices.repeatEvents == repeatEvents &&
                choices.monthRepeatType == monthRepeatType &&
                choices.monthWeekToRepeat == monthWeekToRepeat &&
                choices.monthDayOfWeekToRepeat == monthDayOfWeekToRepeat &&
                choices.daysChosen.containsAll(daysChosen) &&
                choices.repeatEventsOccurred == repeatEventsOccurred;
    }

    public FrequencyChoices(Parcel in) {
        this.repeatType = in.readInt();
        this.repeatEvery = in.readInt();
        this.repeatForever = in.readInt();
        this.repeatToDate = in.readLong();
        this.repeatEvents = in.readInt();
        this.monthRepeatType = in.readInt();
        this.monthWeekToRepeat = in.readInt();
        this.monthDayOfWeekToRepeat = in.readInt();
        this.daysChosen = new ArrayList<>();
        in.readList(daysChosen, List.class.getClassLoader());
        this.repeatEventsOccurred = in.readInt();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(repeatType);
        dest.writeInt(repeatEvery);
        dest.writeInt(repeatForever);
        dest.writeLong(repeatToDate);
        dest.writeInt(repeatEvents);
        dest.writeInt(monthRepeatType);
        dest.writeInt(monthWeekToRepeat);
        dest.writeInt(monthDayOfWeekToRepeat);
        dest.writeList(daysChosen);
        dest.writeInt(repeatEventsOccurred);
    }

    public static final Parcelable.Creator<FrequencyChoices> CREATOR = new Parcelable.Creator<FrequencyChoices>() {
        @Override
        public FrequencyChoices createFromParcel(Parcel in) {
            return new FrequencyChoices(in);
        }

        @Override
        public FrequencyChoices[] newArray(int size) {
            return new FrequencyChoices[size];
        }
    };
}
