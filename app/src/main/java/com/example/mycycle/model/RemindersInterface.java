package com.example.mycycle.model;

import java.util.Calendar;

public interface RemindersInterface {

    void setMenstruationNotification(Calendar calendar);

    void cancelMenstruationNotification();

    boolean isMenstruationReminderActive();

    void setMedicineDailyNotification(Calendar calendar);

    void cancelMedicineDailyNotification();

    boolean isMedicineReminderActive();

    void setMenstruationTracking(Calendar calendar);
}
