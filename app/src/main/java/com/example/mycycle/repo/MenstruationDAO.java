package com.example.mycycle.repo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mycycle.model.Menstruation;

import java.util.List;

@Dao
public interface MenstruationDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Menstruation menstruation);

    @Query("SELECT * FROM Menstruation WHERE userID=:userID ORDER BY startDay DESC LIMIT 1")
    Menstruation getLastMenstruation(String userID);

    @Query("SELECT * FROM Menstruation WHERE userID=:userID")
    List<Menstruation> getMonthlyMenstruation(String userID);

    @Query("DELETE FROM Menstruation WHERE userID=:userID")
    void deleteAll(String userID);
}
