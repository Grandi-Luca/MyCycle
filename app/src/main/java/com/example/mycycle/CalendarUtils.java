package com.example.mycycle;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class CalendarUtils {
    public static Optional<LocalDate> selectedDate;
    public static LocalDate lastMenstruation;

    /**
     * Convert time from an initial format to another
     */
    public static String formattedTime(@NonNull String time, SimpleDateFormat inputFormat, SimpleDateFormat outputFormat) {
        try {
            //Converting the input String to Date
            Date date = inputFormat.parse(time);
            //Changing the format of date and return it
            return outputFormat.format(Objects.requireNonNull(date));
        }catch (ParseException e) {
            return "";
        }
    }

    public static String monthYearFromDate(@NonNull LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    @NonNull
    public static ArrayList<LocalDate> daysInMonthArray(LocalDate date) {
        ArrayList<LocalDate> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();
        int daysOfPreviousMonth = yearMonth.minusMonths(1).lengthOfMonth();

        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i++) {
            if(i < dayOfWeek) {
                daysInMonthArray.add(null);
//                daysInMonthArray.add(LocalDate.of(date.minusMonths(1).getYear(), date.minusMonths(1).getMonth(), daysOfPreviousMonth - dayOfWeek + i + 1));
            } else if(i >= daysInMonth + dayOfWeek){
//                daysInMonthArray.add(LocalDate.of(date.plusMonths(1).getYear(), date.plusMonths(1).getMonth(), i - daysInMonth - dayOfWeek + 1));
            } else {
                daysInMonthArray.add(LocalDate.of(date.getYear(), date.getMonth(), i - dayOfWeek + 1));
            }
        }
        return daysInMonthArray;
    }
}
