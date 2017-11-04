package com.ozmar.notes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FrequencyChoices implements Parcelable {

    private final int repeatType;     // day/week/month/year
    private final int repeatEvery;    // Repeat every X (day/week/month/year)

    private final long repeatToDate;
    private final int repeatEvents;   // Repeat reminder for X events(times)

    private final int monthRepeatType;    // Set from RadioButton in monthly view
    private final List<Integer> daysChosen;   // Set from weekly view

    public FrequencyChoices(int repeatType, int repeatEvery, long repeatToDate, int repeatEvents,
                            int monthRepeatType, List<Integer> daysChosen) {
        this.repeatType = repeatType;
        this.repeatEvery = repeatEvery;
        this.repeatToDate = repeatToDate;
        this.repeatEvents = repeatEvents;
        this.monthRepeatType = monthRepeatType;
        this.daysChosen = daysChosen;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public int getRepeatEvery() {
        return repeatEvery;
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

    @Nullable
    public List<Integer> getDaysChosen() {
        if(daysChosen != null) {
            return new ArrayList<>(daysChosen);
        }

        return null;
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
                choices.repeatToDate == repeatToDate &&
                choices.repeatEvents == repeatEvents &&
                choices.monthRepeatType == monthRepeatType &&
                choices.daysChosen.containsAll(daysChosen);
    }

    public FrequencyChoices(Parcel in) {
        this.repeatType = in.readInt();
        this.repeatEvery = in.readInt();
        this.repeatToDate = in.readLong();
        this.repeatEvents = in.readInt();
        this.monthRepeatType = in.readInt();
        this.daysChosen = new ArrayList<>();
        in.readList(daysChosen, List.class.getClassLoader());

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(repeatType);
        dest.writeInt(repeatEvery);
        dest.writeLong(repeatToDate);
        dest.writeInt(repeatEvents);
        dest.writeInt(monthRepeatType);
        dest.writeList(daysChosen);
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
