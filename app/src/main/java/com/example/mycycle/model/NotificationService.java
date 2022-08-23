package com.example.mycycle.model;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.mycycle.LoginUser;
import com.example.mycycle.worker.AlarmReceiver;

import java.util.Calendar;

public class NotificationService implements Notification, DailyNotification {

    private final Context context;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public NotificationService(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    public void setDailyNotification(Calendar calendar) {
        setNotification(calendar);
        showAlert();
    }

    public void setNotification(Calendar calendar) {

        Calendar cur = Calendar.getInstance();

        // add one day if time selected is before system time
        if (cur.after(calendar)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        PendingIntent pendingIntent = getPendingIntent(
                new Intent(context, AlarmReceiver.class)
                        .putExtra(AlarmReceiver.TITLE_EXTRA, "prendi le medicine")
                        .putExtra(AlarmReceiver.HOUR, calendar.get(Calendar.HOUR_OF_DAY))
                        .putExtra(AlarmReceiver.MINUTE, calendar.get(Calendar.MINUTE)));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }

    private PendingIntent getPendingIntent(Intent intent) {
        return PendingIntent.getBroadcast(
                context,
                AlarmReceiver.NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private void showAlert() {
        Toast.makeText(context, "notifica programmata", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createNotificationChannel() {
        var name = "Medicine notification";
        var desc = "Notification to reminder the user to take his medicine";
        var importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(AlarmReceiver.CHANNEL_ID, name, importance);
            channel.setDescription(desc);
            var notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

}

