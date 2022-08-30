package com.example.mycycle.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.mycycle.model.Menstruation;
import com.example.mycycle.model.Note;
import com.example.mycycle.repo.MenstruationDAO;
import com.example.mycycle.repo.NoteDAO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class, Menstruation.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {

    public abstract NoteDAO noteDAO();

    public abstract MenstruationDAO menstruationDAO();

    ///Singleton instance to retrieve when the db is needed
    private static volatile MyDatabase INSTANCE;

    public static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static MyDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            //The synchronized is to prevent multiple instances being created.
            synchronized (MyDatabase.class) {
                //If the db has not yet been created, the builder creates it.
                if (INSTANCE == null){
                    INSTANCE = Room
                            .databaseBuilder(context.getApplicationContext(),
                                    MyDatabase.class, "CycleDatabase")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
