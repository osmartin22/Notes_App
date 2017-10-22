package com.ozmar.notes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class FrequencyChoices implements Parcelable {

    private int repeatType = 0;       //day/week/month/year
    private int repeatTypeHowOften = 0;     // Repeat every X (day/week/month/year)(counts as 1 reminder)

    private long repeatToSpecificDate = 0;
    private int howManyRepeatEvents = 0;    // Repeat reminder X times

    private int monthRepeatType = 0;    // Set from RadioButton in monthly view
    private List<Integer> daysChosen;   // Set from weekly view

    public FrequencyChoices() {

    }

    public int getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }

    public int getRepeatTypeHowOften() {
        return repeatTypeHowOften;
    }

    public void setRepeatTypeHowOften(int repeatTypeHowOften) {
        this.repeatTypeHowOften = repeatTypeHowOften;
    }

    public long getRepeatToSpecificDate() {
        return repeatToSpecificDate;
    }

    public void setRepeatToSpecificDate(long repeatToSpecificDate) {
        this.repeatToSpecificDate = repeatToSpecificDate;
    }

    public int getHowManyRepeatEvents() {
        return howManyRepeatEvents;
    }

    public void setHowManyRepeatEvents(int howManyRepeatEvents) {
        this.howManyRepeatEvents = howManyRepeatEvents;
    }

    public int getMonthRepeatType() {
        return monthRepeatType;
    }

    public void setMonthRepeatType(int monthRepeatType) {
        this.monthRepeatType = monthRepeatType;
    }

    public List<Integer> getDaysChosen() {
        return daysChosen;
    }

    public void setDaysChosen(List<Integer> daysChosen) {
        this.daysChosen = daysChosen;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public FrequencyChoices(Parcel in) {
        this.repeatType = in.readInt();
        this.repeatTypeHowOften = in.readInt();
        this.repeatToSpecificDate = in.readLong();
        this.howManyRepeatEvents = in.readInt();
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
        dest.writeInt(repeatTypeHowOften);
        dest.writeLong(repeatToSpecificDate);
        dest.writeInt(howManyRepeatEvents);
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
