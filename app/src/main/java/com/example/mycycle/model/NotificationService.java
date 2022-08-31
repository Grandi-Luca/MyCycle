package com.example.mycycle.model;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.mycycle.repo.FirebaseDAOUser;
import com.example.mycycle.worker.AlarmReceiver;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class NotificationService implements NotificationServiceInterface, RemindersInterface {

    private final Context context;
    private final FirebaseDAOUser daoUser;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public NotificationService(Context context) {
        this.context = context;
        createNotificationChannel(AlarmReceiver.MEDICINE_CHANNEL_ID,
                "Medicine notification",
                "NotificationServiceInterface to reminder the user to take his medicine");
        createNotificationChannel(AlarmReceiver.MENSTRUATION_CHANNEL_ID,
                "Menstruation notification",
                "NotificationServiceInterface to reminder the user when menstruation is arrive");

        daoUser = new FirebaseDAOUser();
    }

    public void setMedicineDailyNotification(Calendar calendar) {
        setDailyNotification(calendar,
                "prendi le medicine",
                AlarmReceiver.MEDICINE_NOTIFICATION_ID);
        showAlert("notifica giornaliera programmata");
    }

    public void setDailyNotification(Calendar calendar, String msg, int notificationID) {

        Calendar cur = Calendar.getInstance();

        // add one day if time selected is before system time
        if (cur.after(calendar)) {
            calendar.add(Calendar.DATE, 1);
        }

        PendingIntent pendingIntent = getPendingIntent(
                this.getDefaultAlarmIntent(notificationID, msg)
                        .putExtra(AlarmReceiver.HOUR, calendar.get(Calendar.HOUR_OF_DAY))
                        .putExtra(AlarmReceiver.MINUTE, calendar.get(Calendar.MINUTE)),
                notificationID);

        launchExactAlarmManager(pendingIntent, calendar);
    }

    @Override
    public void cancelNotification(int notificationID) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent =
                getPendingIntent(new Intent(context, AlarmReceiver.class), notificationID);

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @Override
    public void setMenstruationNotification(Calendar calendar) {
        Calendar cur = Calendar.getInstance();

        // add x days if time selected is before system time
        if (cur.after(calendar)) {

            if(AlarmReceiver.menstruationPeriod <= 0) {
                daoUser.getCurrentUserInfo()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            var currentUser = snapshot.getValue(User.class);
                            AlarmReceiver.menstruationPeriod = currentUser.getDurationPeriod();

                            calendar.add(Calendar.DATE, AlarmReceiver.menstruationPeriod - 1);

                            PendingIntent pendingIntent = getPendingIntent(
                                    getDefaultAlarmIntent(
                                            AlarmReceiver.MENSTRUATION_NOTIFICATION_ID,
                                            "domani c'è alta probabilità che inizieranno le mestruazioni"),
                                    AlarmReceiver.MENSTRUATION_NOTIFICATION_ID);

                            launchExactAlarmManager(pendingIntent, calendar);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                return;
            }

            calendar.add(Calendar.DATE, AlarmReceiver.menstruationPeriod - 1);
        }

        PendingIntent pendingIntent = getPendingIntent(
                this.getDefaultAlarmIntent(
                        AlarmReceiver.MENSTRUATION_NOTIFICATION_ID,
                        "domani c'è alta probabilità che inizieranno le mestruqzioni"),
                AlarmReceiver.MENSTRUATION_NOTIFICATION_ID);

        launchExactAlarmManager(pendingIntent, calendar);
    }

    @Override
    public void cancelMenstruationNotification() {
        cancelNotification(AlarmReceiver.MENSTRUATION_NOTIFICATION_ID);
        showAlert("notifica mestruazioni disabilitata");
    }

    @Override
    public boolean isMenstruationReminderActive() {
        return isReminderActive(AlarmReceiver.MENSTRUATION_NOTIFICATION_ID);
    }

    @Override
    public boolean isReminderActive(int reminderID) {
        return (PendingIntent.getBroadcast(context, reminderID,
                new Intent(context, AlarmReceiver.class),
                PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_NO_CREATE) != null);
    }

    @Override
    public void cancelMedicineDailyNotification() {
        cancelNotification(AlarmReceiver.MEDICINE_NOTIFICATION_ID);
        showAlert("notifica giornaliera disabilitata");
    }

    @Override
    public boolean isMedicineReminderActive() {
        return isReminderActive(AlarmReceiver.MEDICINE_NOTIFICATION_ID);
    }

    @Override
    public void setMenstruationTracking(Calendar calendar) {
        Calendar cur = Calendar.getInstance();

        // add x days if time selected is before system time
        if (cur.after(calendar)) {

            if(AlarmReceiver.menstruationPeriod <= 0) {
                daoUser.getCurrentUserInfo()
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                var currentUser = snapshot.getValue(User.class);
                                AlarmReceiver.menstruationPeriod = currentUser.getDurationPeriod();
                                AlarmReceiver.menstruationDuration = currentUser.getDurationMenstruation();

                                calendar.add(Calendar.DATE, AlarmReceiver.menstruationPeriod);

                                PendingIntent pendingIntent = getPendingIntent(
                                        new Intent(context, AlarmReceiver.class)
                                                .putExtra(AlarmReceiver.YEAR, calendar.get(Calendar.YEAR))
                                                .putExtra(AlarmReceiver.MONTH, calendar.get(Calendar.MONTH))
                                                .putExtra(AlarmReceiver.DAY, calendar.get(Calendar.DAY_OF_MONTH)),
                                        AlarmReceiver.MENSTRUATION_TRACKING_ID);

                                launchExactAlarmManager(pendingIntent, calendar);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                return;
            }

            calendar.add(Calendar.DATE, AlarmReceiver.menstruationPeriod);
        }

        PendingIntent pendingIntent = getPendingIntent(
                new Intent(context, AlarmReceiver.class)
                        .putExtra(AlarmReceiver.YEAR, calendar.get(Calendar.YEAR))
                        .putExtra(AlarmReceiver.MONTH, calendar.get(Calendar.MONTH))
                        .putExtra(AlarmReceiver.DAY, calendar.get(Calendar.DAY_OF_MONTH)),
                AlarmReceiver.MENSTRUATION_TRACKING_ID);

        launchExactAlarmManager(pendingIntent, calendar);
    }

    private void launchExactAlarmManager(PendingIntent pendingIntent, Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent);
    }

    private PendingIntent getPendingIntent(Intent intent, int id) {
        return PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private Intent getDefaultAlarmIntent(int notificationID, String msg) {
        return new Intent(context, AlarmReceiver.class)
                .putExtra(AlarmReceiver.NOTIFICATION_ID, notificationID)
                .putExtra(AlarmReceiver.TITLE_EXTRA, msg);
    }

    private void showAlert(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel(String channelID, String name, String desc) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            var importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel;
            channel = new NotificationChannel(channelID, name, importance);
            channel.setDescription(desc);
            var notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

}

