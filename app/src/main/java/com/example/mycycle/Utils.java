package com.example.mycycle;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

    public static void showDialog(Dialog dialog, int layout, int animation) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(layout);
        dialog.getWindow().getAttributes().windowAnimations = animation;
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        var btn_cancel = dialog.findViewById(R.id.closeButton);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static boolean isInternetConnected(Context context) {
        if(context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return  networkInfo.isConnected();
            }
        }
        return false;
    }

}
