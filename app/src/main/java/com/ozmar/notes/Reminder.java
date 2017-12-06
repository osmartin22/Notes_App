package com.ozmar.notes;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import javax.annotation.Nonnull;

// TODO: Update rest of code to handle null DateTimes

@Entity(tableName = "remindersTable")
public final class Reminder implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "reminderId")
    private int id;

    @Nonnull
    @ColumnInfo(name = "reminderTime")
    private DateTime mDateTime;

    @Nullable
    @Embedded
    private FrequencyChoices mFrequencyChoices;


    public Reminder(int id, @Nonnull DateTime dateTime, @Nullable FrequencyChoices frequencyChoices) {
        this.id = id;
        mDateTime = dateTime;
        mFrequencyChoices = frequencyChoices;
    }

    @Ignore
    public Reminder(@Nonnull DateTime dateTime, @Nullable FrequencyChoices frequencyChoices) {
        this.mDateTime = dateTime;
        this.mFrequencyChoices = frequencyChoices;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Nonnull
    public DateTime getDateTime() {
        return mDateTime;
    }

    public void setDateTime(@Nonnull DateTime dateTime) {
        mDateTime = dateTime;
    }

    @Nullable
    public FrequencyChoices getFrequencyChoices() {
        return mFrequencyChoices;
    }

    public void setFrequencyChoices(@Nullable FrequencyChoices frequencyChoices) {
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

        return reminder.mDateTime == mDateTime &&
                reminder.mFrequencyChoices == mFrequencyChoices;
    }

    public Reminder(Parcel in) {
        this.mDateTime = new DateTime(in.readLong());
        this.mFrequencyChoices = in.readParcelable(this.getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mDateTime.getMillis());
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
