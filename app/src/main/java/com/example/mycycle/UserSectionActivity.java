package com.example.mycycle;

import static com.example.mycycle.Utils.isUserLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextClock;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class UserSectionActivity extends AppCompatActivity {

//    FIXME: time picker doesn't work

    private TextView textView;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isUserLogin()){
            Intent intent = new Intent(UserSectionActivity.this, LoginUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        TextClock textClock = new TextClock(UserSectionActivity.this);

        textView = findViewById(R.id.textClock);
        textView.setText(LocalTime.now().format(textClock.is24HourModeEnabled() ?
                DateTimeFormatter.ofPattern("HH:mm") :
                DateTimeFormatter.ofPattern("hh:mm a")
        ));
        textView.setOnClickListener(v -> {
            boolean is24HourFormat = textClock.is24HourModeEnabled();

//            get the alarm time and parse it to a LocalTime for extract hour and minute
            var date = LocalTime.parse(textView.getText(), textClock.is24HourModeEnabled() ?
                    DateTimeFormatter.ofPattern("HH:mm") :
                    DateTimeFormatter.ofPattern("hh:mm a")
            );
            int hour = date.getHour();
            int minute = date.getMinute();

//            select time for the daily remainder
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(UserSectionActivity.this, (timePicker, selectedHour, selectedMinute) ->
                    textView.setText(getHHmmInSystemFormat(selectedHour, selectedMinute, is24HourFormat)),
                    hour, minute, is24HourFormat);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });
        
        findViewById(R.id.logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserSectionActivity.this, LoginUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        findViewById(R.id.homeBtn).setOnClickListener(v -> {
            Intent intent = new Intent(UserSectionActivity.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.setAlarmButton).setOnClickListener(v -> {
            CharSequence time;

//          get the time as a string using a specified format
            if (!textClock.is24HourModeEnabled()) {
                time = CalendarUtils.formattedTime(textView.getText().toString(),
                        new SimpleDateFormat("hh:mm aa"),
                        new SimpleDateFormat("HH:mm"));
            } else {
                time = textView.getText().toString();
            }
            final var date = LocalTime.parse(time);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, date.getHour());
            calendar.set(Calendar.MINUTE, date.getMinute());
            calendar.set(Calendar.SECOND, date.getSecond());

//            TODO: implement alarm with alarm manager
        });
    }

    @NonNull
    private String getHHmmInSystemFormat(int hour, int minute, boolean is24HourFormat){
        String formattedTime;

        if(!is24HourFormat) {
            if(hour == 0) {
                formattedTime = (minute < 10) ?
                        12 + ":" + 0 + minute + " AM" : 12 + ":" + minute + " AM";
            } else if(hour > 12) {
                formattedTime = (minute < 10) ?
                        (hour - 12) + ":" + 0 + minute + " PM" : (hour - 12) + ":" + minute + " PM";
            } else if(hour == 12) {
                formattedTime = (minute < 10) ?
                        hour + ":" + 0 + minute + " PM" : hour + ":" + minute + " PM";
            } else {
                formattedTime = (minute < 10) ?
                        hour + ":" + 0 + minute + " AM" : hour + ":" + minute + " AM";
            }
        } else {
            formattedTime = hour + ":" + minute;
        }

        return formattedTime;
    }

}