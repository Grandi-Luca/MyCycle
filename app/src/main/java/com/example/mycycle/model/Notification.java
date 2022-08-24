package com.example.mycycle.model;

import java.util.Calendar;

public interface Notification {
    void setDailyNotification(Calendar calendar, String msg, int notificationID);

    void cancelNotification(int notificationID);

    void setMenstruationNotification(Calendar calendar);

    void cancelMenstruationNotification();
}
