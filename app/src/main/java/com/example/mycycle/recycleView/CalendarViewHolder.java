package com.example.mycycle.recycleView;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycycle.R;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarViewHolder extends RecyclerView.ViewHolder{

    private final View parentView;
    private final TextView dayOfMonth;

    public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener, ArrayList<LocalDate> days) {
        super(itemView);
        parentView = itemView.findViewById(R.id.parentView);
        dayOfMonth = itemView.findViewById(R.id.cellDayText);

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
