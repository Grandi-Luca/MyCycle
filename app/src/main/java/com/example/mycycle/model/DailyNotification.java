package com.example.mycycle.model;

import java.util.Calendar;

public interface DailyNotification {
    void setDailyNotification(Calendar calendar);

    void cancelDailyNotification();
}
