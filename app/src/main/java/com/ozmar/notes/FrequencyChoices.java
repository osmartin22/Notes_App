package com.ozmar.notes;

import java.util.List;


public class FrequencyChoices {


    private int repeatType = 0;       //day/week/month/year
    private int repeatTypeHowOften = 0;     // Repeat every X (day/week/month/year)(counts as 1 reminder)

    private boolean repeatForever = false;
    private long repeatToSpecificDate = 0;
    private int howManyRepeatEvents = 0;    // Repeat reminder X times

    private int monthRepeatType = 0;    // Set from RadioButton in monthly view
    private List<Boolean> daysChosen;   // Set from weekly view

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

    public boolean isRepeatForever() {
        return repeatForever;
    }

    public void setRepeatForever(boolean repeatForever) {
        this.repeatForever = repeatForever;
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

    public List<Boolean> getDaysChosen() {
        return daysChosen;
    }

    public void setDaysChosen(List<Boolean> daysChosen) {
        this.daysChosen = daysChosen;
    }
}
