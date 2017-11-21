package com.ozmar.notes.database;


import android.arch.persistence.room.TypeConverter;

import org.joda.time.DateTime;

public class DateTimeConverter {

    @TypeConverter
    public static long dateTimeToLong(DateTime dateTime){
        return dateTime.getMillis();
    }

    @TypeConverter
    public static DateTime longToDateTime(long millis){
        return new DateTime(millis);
    }

}
