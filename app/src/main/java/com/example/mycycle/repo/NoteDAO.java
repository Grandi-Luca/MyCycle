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

    @Query("SELECT * FROM Note WHERE userID=:userID")
    List<Note> getNotes(String userID);

    @Query("DELETE FROM Note WHERE userID=:userID")
    void deleteAll(String userID);

    @Query("SELECT * FROM Note WHERE userID=:userID AND importance > 0")
    List<Note> getImportantNote(String userID);
}
