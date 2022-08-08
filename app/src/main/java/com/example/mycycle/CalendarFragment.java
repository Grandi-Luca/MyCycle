package com.example.mycycle;

import static com.example.mycycle.CalendarUtils.daysInMonthArray;
import static com.example.mycycle.CalendarUtils.monthYearFromDate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycycle.recycleView.CalendarAdapter;
import com.example.mycycle.recycleView.OnItemListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class CalendarFragment extends Fragment implements OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;

    public CalendarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        initWidgets(view);
        CalendarUtils.selectedDate = Optional.ofNullable(LocalDate.now());
        updateMonthView(LocalDate.now());

        return view;
    }

    private void initWidgets(View v) {
        calendarRecyclerView = v.findViewById(R.id.calendarRecyclerView);
        monthYearText = v.findViewById(R.id.monthYearTV);

        v.findViewById(R.id.prevMonth).setOnClickListener(this::previousMonthAction);
        v.findViewById(R.id.nextMonth).setOnClickListener(this::nextMonthAction);
    }

    private void updateMonthView(LocalDate date) {
        monthYearText.setText(monthYearFromDate(date));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(date);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext().getApplicationContext(), 7);
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