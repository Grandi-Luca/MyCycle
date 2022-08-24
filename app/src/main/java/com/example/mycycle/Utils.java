package com.example.mycycle;

import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static boolean isUserLogin(){
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static String getHHmmInSystemFormat(int hour, int minute, boolean is24HourFormat) {
        String date;
        if (hour < 10) {
            if (minute < 10) {
                date = "0" + hour + ":" + "0" + minute;
            } else {
                date = "0" + hour + ":" + minute;
            }
        } else {
            if (minute < 10) {
                date = hour + ":" + "0" + minute;
            } else {
                date = hour + ":" + minute;
            }
        }
        return is24HourFormat ? date :
                LocalTime.parse(date).format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

}
