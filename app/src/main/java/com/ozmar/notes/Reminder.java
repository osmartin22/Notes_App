package com.ozmar.notes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;


public final class Reminder implements Parcelable {

    private DateTime mDateTime;
    private long reminderTime;
    private FrequencyChoices mFrequencyChoices;


    public Reminder() {

    }

    public Reminder(long reminderTime, @Nullable FrequencyChoices frequencyChoices) {

        this.reminderTime = reminderTime;
        this.mFrequencyChoices = frequencyChoices;
    }


    public DateTime getDateTime() {
        return mDateTime;
    }

    public void setDateTime(DateTime dateTime) {
        mDateTime = dateTime;
    }

    public FrequencyChoices getFrequencyChoices() {
        return mFrequencyChoices;
    }

    public void setFrequencyChoices(FrequencyChoices frequencyChoices) {
        mFrequencyChoices = frequencyChoices;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Reminder)) {
            return false;
        }

        Reminder reminder = (Reminder) obj;

        return reminder.reminderTime == reminderTime &&
                reminder.mFrequencyChoices == mFrequencyChoices;
    }

    public Reminder(Parcel in) {
        this.reminderTime = in.readLong();
        this.mFrequencyChoices = in.readParcelable(this.getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(reminderTime);
        dest.writeParcelable(mFrequencyChoices, flags);
    }

    public static final Parcelable.Creator<Reminder> CREATOR = new Parcelable.Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

}
