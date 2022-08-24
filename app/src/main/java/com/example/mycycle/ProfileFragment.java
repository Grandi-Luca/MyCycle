package com.example.mycycle;

import static com.example.mycycle.Utils.isUserLogin;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.mycycle.model.NotificationService;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class ProfileFragment extends Fragment {

    private TextView textView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SimpleDateFormat")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        NotificationService notificationService = new NotificationService(requireContext());

        if(!isUserLogin()){
            Intent intent = new Intent(getActivity(), LoginUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return null;
        }

        TextClock textClock = new TextClock(getActivity());

        textView = view.findViewById(R.id.textClock);
        textView.setText(LocalTime.now().format(textClock.is24HourModeEnabled() ?
                DateTimeFormatter.ofPattern("HH:mm") :
                DateTimeFormatter.ofPattern("hh:mm a")
        ));

        textView.setOnClickListener(v -> {
            boolean is24HourFormat = textClock.is24HourModeEnabled();

//            get the alarm time and parse it to a LocalTime for extract hour and minute
            var format = is24HourFormat ? DateTimeFormatter.ofPattern("HH:mm") : DateTimeFormatter.ofPattern("hh:mm a");
            var date = LocalTime.parse(textView.getText(), format);
            int hour = date.getHour();
            int minute = date.getMinute();

            // select time for the daily remainder
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getActivity(),
                    (timePicker, selectedHour, selectedMinute) ->
                            textView.setText(Utils.getHHmmInSystemFormat(selectedHour, selectedMinute, is24HourFormat)),
                    hour, minute, is24HourFormat);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        view.findViewById(R.id.logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        view.findViewById(R.id.setAlarmButton).setOnClickListener(v -> {
            CharSequence time;

            // get the time as a string using a specified format
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
            calendar.set(Calendar.SECOND, 0);

            notificationService.setMedicineDailyNotification(calendar);
        });

        view.findViewById(R.id.cancelAlarm).setOnClickListener(v -> {
            notificationService.cancelMedicineDailyNotification();
        });

        view.findViewById(R.id.setAlarmMenstruationButton).setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.MONTH, 7);
            calendar.set(Calendar.DAY_OF_MONTH, 23);
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            notificationService.setMenstruationNotification(calendar);
        });

        view.findViewById(R.id.cancelMenstruationAlarm).setOnClickListener(v -> {
            notificationService.cancelMenstruationNotification();
        });

        return view;
    }

}