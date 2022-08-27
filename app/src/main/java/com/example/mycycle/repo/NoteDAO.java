package com.example.mycycle.repo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mycycle.model.Note;

import java.util.List;

@Dao
public interface NoteDAO {
    @Insert
    void insertNote(Note note);

    @Query("SELECT * FROM Note WHERE date LIKE '%' || :date  || '%'")
    List<Note> getNotes(String date);

    @Query("DELETE FROM Note")
    void deleteAll();
}
