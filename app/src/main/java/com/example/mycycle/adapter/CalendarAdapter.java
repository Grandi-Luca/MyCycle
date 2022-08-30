package com.example.mycycle.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycycle.CalendarUtils;
import com.example.mycycle.R;
import com.example.mycycle.model.Menstruation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    private List<LocalDate> days;
    private final OnItemListener onItemListener;
    private List<Menstruation> menstruationList;
    private List<Menstruation> predictedList;

    public CalendarAdapter(List<LocalDate> days,
                           OnItemListener onItemListener,
                           List<Menstruation> menstruationList, List<Menstruation> predictedList) {
        this.days = days;
        this.onItemListener = onItemListener;
        this.menstruationList = menstruationList;
        this.predictedList = predictedList;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        layoutParams.height = (int) (parent.getHeight() * 0.1666666666);

        return new CalendarViewHolder(view, onItemListener, days);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        final LocalDate date = days.get(position);
        if(date == null) {
            holder.getDayOfMonth().setText("");
        }
        else {

            holder.getDayOfMonth().setText(String.valueOf(date.getDayOfMonth()));
            LocalDate actual = CalendarUtils.selectedDate.orElseGet(LocalDate::now);

            for (var day : menstruationList) {
                LocalDate start = LocalDate.parse(day.getStartDay());
                LocalDate finish = LocalDate.parse(day.getLastDay());

                if(!(date.isBefore(start) || date.isAfter(finish))) {
                    holder.background.setBackgroundResource(R.drawable.cell_menstruation);
                    holder.getDayOfMonth().setTextColor(Color.WHITE);
                }
            }

            for (var day : predictedList) {
                LocalDate start = LocalDate.parse(day.getStartDay());
                LocalDate finish = LocalDate.parse(day.getLastDay());

                if(!(date.isBefore(start) || date.isAfter(finish))) {
                    holder.background.setBackgroundResource(R.drawable.cell_predicted);
                    holder.getDayOfMonth().setTextColor(Color.WHITE);
                }
            }

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

    static class CalendarViewHolder extends RecyclerView.ViewHolder{

        private final View parentView, background;
        private final TextView dayOfMonth;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener,
                                  List<LocalDate> days) {
            super(itemView);
            parentView = itemView.findViewById(R.id.parentView);
            dayOfMonth = itemView.findViewById(R.id.cellDayText);
            background = itemView.findViewById(R.id.background);

            itemView.setOnClickListener((View v)->{
                onItemListener.onItemClick(getAdapterPosition(), days.get(getAdapterPosition()));
            });
        }

        public View getParentView() {
            return parentView;
        }

        public TextView getDayOfMonth() {
            return dayOfMonth;
        }

    }

    public interface OnItemListener {

        void onItemClick(int position, LocalDate date);
    }
}
