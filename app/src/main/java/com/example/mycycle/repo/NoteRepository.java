package com.example.mycycle.repo;

import android.app.Application;

import com.example.mycycle.database.MyDatabase;
import com.example.mycycle.model.Note;

import java.util.List;

public class NoteRepository {

    private final NoteDAO noteDAO;

    public NoteRepository(Application application) {
        MyDatabase myDatabase = MyDatabase.getDatabase(application);
        this.noteDAO = myDatabase.noteDAO();
    }

    public void insertNote(Note note) {
        MyDatabase.executor.execute(() -> noteDAO.insertNote(note));
    }

    public List<Note> getNotes(String userID) {
        return noteDAO.getNotes(userID);
    }

    public void deleteAll(String userID) {
        noteDAO.deleteAll(userID);
    }

    public List<Note> getImportantNote(String userID) {
        return noteDAO.getImportantNote(userID);
    }

}
