package com.example.mycycle.model;

import java.util.Calendar;

public interface NotificationServiceInterface {
    void setDailyNotification(Calendar calendar, String msg, int notificationID);

    void cancelNotification(int notificationID);

    boolean isReminderActive(int reminderID);
}
