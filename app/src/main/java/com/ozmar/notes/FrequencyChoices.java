package com.ozmar.notes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FrequencyChoices implements Parcelable {

    private final int repeatType;     // day/week/month/year
    private final int repeatEvery;    // Repeat every X (day/week/month/year)

    private final int repeatForever;
    private final long repeatToDate;
    private final int repeatEvents;   // Repeat reminder for X events(times)

    private final int monthRepeatType;
    private final int monthWeekToRepeat;
    private final int monthDayOfWeekToRepeat;

    private final List<Integer> daysChosen;

    private final int repeatEventsOccurred;


    // Constructor for presets
    public FrequencyChoices(@IntRange(from = 0, to = 4)int repeatType, @Nullable List<Integer> daysChosen) {
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

    public FrequencyChoices(@IntRange(from = 0, to = 4)int repeatType, int repeatEvery,
                            @IntRange(from = 0, to = 1)int repeatForever, long repeatToDate, int repeatEvents,
                            @IntRange(from = -1, to = 1)int monthRepeatType,
                            @IntRange(from = 0, to = 5)int monthWeekToRepeat,
                            @IntRange(from = 0, to = 7)int monthDayOfWeekToRepeat, @Nullable List<Integer> daysChosen) {
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

    public int getRepeatEvery() {
        return repeatEvery;
    }

    public int getRepeatForever() {
        return repeatForever;
    }

    public long getRepeatToDate() {
        return repeatToDate;
    }

    public int getRepeatEvents() {
        return repeatEvents;
    }

    public int getMonthRepeatType() {
        return monthRepeatType;
    }

    public int getMonthWeekToRepeat() {
        return monthWeekToRepeat;
    }

    public int getMonthDayOfWeekToRepeat() {
        return monthDayOfWeekToRepeat;
    }

    @Nullable
    public List<Integer> getDaysChosen() {
        if (daysChosen != null) {
            return new ArrayList<>(daysChosen);
        }

        return null;
    }

    public int getRepeatEventsOccurred() {
        return repeatEventsOccurred;
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
