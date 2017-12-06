package com.ozmar.notes.database;


import android.arch.persistence.room.TypeConverter;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DaysChosenConverter {

    @TypeConverter
    public static String daysChosenToString(@Nullable List<Integer> daysChosen) {

        if (daysChosen != null) {
            StringBuilder sb = new StringBuilder();
            for (int day : daysChosen) {
                sb.append(day);
                sb.append(" ");
            }
            return sb.toString();
        }
        return "";

    }

    @TypeConverter
    public static List<Integer> daysChosenToList(@Nullable String daysChosen) {

        List<Integer> list = null;
        if (daysChosen != null) {
            Scanner scanner = new Scanner(daysChosen);
            list = new ArrayList<>();
            while (scanner.hasNextInt()) {
                list.add(scanner.nextInt());
            }
        }
        return list;
    }

}
