package com.example.mycycle.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Note {

    @PrimaryKey (autoGenerate = true)
    public long id;

    @ColumnInfo (name = "note")
    public String note;

    @ColumnInfo(name = "date")
    public String date;

    public Note() {
    }

    public long getId() {
        return id;
    }

    public Note setId(long id) {
        this.id = id;
        return this;
    }

    public String getNote() {
        return note;
    }

    public Note setNote(String note) {
        this.note = note;
        return this;
    }

    public String getDate() {
        return date;
    }

    public Note setDate(String date) {
        this.date = date;
        return this;
    }
}
