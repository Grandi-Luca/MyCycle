package com.example.mycycle.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycycle.R;
import com.example.mycycle.model.Note;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;

    public NoteAdapter() {
        notes = new ArrayList<>();
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);

        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.textView.setText(note.getNote());
        holder.timeTextView.setText(note.getDate());

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView, timeTextView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.description);
            timeTextView = itemView.findViewById(R.id.time);
        }
    }
}
