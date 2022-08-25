package com.example.mycycle.worker;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.mycycle.R;
import com.example.mycycle.model.NotificationService;
import com.example.mycycle.model.NotificationServiceInterface;

import java.util.Calendar;


public class AlarmReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_ID = "notificationID";
    public static final int MEDICINE_NOTIFICATION_ID = 1;
    public static final int MENSTRUATION_NOTIFICATION_ID = 2;
    public static final String TITLE_EXTRA = "titleExtra";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String MEDICINE_CHANNEL_ID = "medicine notification";
    public static final String MENSTRUATION_CHANNEL_ID = "menstruation notification";
    public static int menstruationPeriod = -1;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationID = intent.getIntExtra(NOTIFICATION_ID, -1);
        var manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationService service = new NotificationService(context);
        Calendar calendar = Calendar.getInstance();
        Notification notification;

        switch (notificationID) {
            case MEDICINE_NOTIFICATION_ID:
                // fire notification
                notification = new NotificationCompat.Builder(context, MEDICINE_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(intent.getStringExtra(TITLE_EXTRA))
                        .build();

                manager.notify(MEDICINE_NOTIFICATION_ID, notification);

                // set next notification
                calendar.set(Calendar.HOUR_OF_DAY, intent.getIntExtra(HOUR, Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
                calendar.set(Calendar.MINUTE, intent.getIntExtra(MINUTE, Calendar.getInstance().get(Calendar.MINUTE)));
                calendar.set(Calendar.SECOND, 0);
                service.setDailyNotification(calendar,
                        intent.getStringExtra(TITLE_EXTRA),
                        MEDICINE_NOTIFICATION_ID);
                break;

            case MENSTRUATION_NOTIFICATION_ID:
                // fire notification
                notification = new NotificationCompat.Builder(context, MENSTRUATION_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(intent.getStringExtra(TITLE_EXTRA))
                        .build();

                manager.notify(MENSTRUATION_NOTIFICATION_ID, notification);

                // set next notification
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                service.setMenstruationNotification(calendar);
                break;

            default:
                break;

        }

    }
}
