package com.example.mycycle;

import static com.example.mycycle.CalendarUtils.daysInMonthArray;
import static com.example.mycycle.CalendarUtils.monthYearFromDate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mycycle.recycleView.CalendarAdapter;
import com.example.mycycle.recycleView.OnItemListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class CalendarActivity extends AppCompatActivity implements OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        initWidgets();
        CalendarUtils.selectedDate = Optional.ofNullable(LocalDate.now());
        updateMonthView(LocalDate.now());
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);

        Button prev = findViewById(R.id.prevMonth);
        Button next = findViewById(R.id.nextMonth);

        prev.setOnClickListener(this::previousMonthAction);

        next.setOnClickListener(this::nextMonthAction);
    }

    private void updateMonthView(LocalDate date) {
        monthYearText.setText(monthYearFromDate(date));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(date);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    public void previousMonthAction(View view) {
        LocalDate date = CalendarUtils.selectedDate.orElseGet(LocalDate::now);

        CalendarUtils.selectedDate = Optional.ofNullable(date.minusMonths(1));
        updateMonthView(date.minusMonths(1));
    }

    public void nextMonthAction(View view) {
        LocalDate date = CalendarUtils.selectedDate.orElseGet(LocalDate::now);

        CalendarUtils.selectedDate = Optional.ofNullable(date.plusMonths(1));
        updateMonthView(date.plusMonths(1));
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        if(date != null) {
            CalendarUtils.selectedDate = Optional.of(date);
            updateMonthView(date);
        }
    }
}