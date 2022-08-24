package com.example.mycycle.model;

import java.util.Calendar;

public interface MedicineNotification {
    void setMedicineDailyNotification(Calendar calendar);

    void cancelMedicineDailyNotification();
}
