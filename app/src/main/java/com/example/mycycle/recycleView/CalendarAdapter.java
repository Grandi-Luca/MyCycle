package com.example.mycycle.recycleView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycycle.CalendarUtils;
import com.example.mycycle.R;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener) {
        this.days = days;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        layoutParams.height = (int) (parent.getHeight() * 0.166666666);

        return new CalendarViewHolder(view, onItemListener, days);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        final LocalDate date = days.get(position);
        if(date == null) {
            holder.getDayOfMonth().setText("");
        }
        else {
            holder.getDayOfMonth().setText(String.valueOf(date.getDayOfMonth()));
            LocalDate actual = CalendarUtils.selectedDate.orElseGet(LocalDate::now);

            if(date.equals(actual) && CalendarUtils.selectedDate.isPresent()){
                holder.getParentView().setBackgroundResource(R.drawable.cell_selected);
            }
            if(date.equals(LocalDate.now())){
                holder.getDayOfMonth().setBackgroundResource(R.drawable.cell_today);
                holder.getDayOfMonth().setTextColor(Color.WHITE);
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return days.size();
    }
}
