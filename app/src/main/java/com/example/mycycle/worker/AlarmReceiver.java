package com.example.mycycle.worker;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.mycycle.R;
import com.example.mycycle.model.Notification;
import com.example.mycycle.model.NotificationService;

import java.util.Calendar;


public class AlarmReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "medicine notification";
    public static final String TITLE_EXTRA = "titleExtra";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {

        var notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(intent.getStringExtra(TITLE_EXTRA))
                .build();

        var manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);

        var calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, intent.getIntExtra(HOUR, Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
        calendar.set(Calendar.MINUTE, intent.getIntExtra(MINUTE, Calendar.getInstance().get(Calendar.MINUTE)));
        calendar.set(Calendar.SECOND, 0);

        Notification service = new NotificationService(context);
        service.setNotification(calendar);

    }
}
