package com.example.mycycle.repo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mycycle.model.Menstruation;

import java.util.List;

@Dao
public interface MenstruationDAO {

//    @Query("INSERT OR IGNORE INTO Menstruation (userID, startDay, lastDay)" +
//            " VALUES (:userID, :startDay, :lastDay)")
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Menstruation menstruation);

    @Query("SELECT * FROM Menstruation WHERE userID=:userID ORDER BY startDay DESC LIMIT 1")
    Menstruation getLastMenstruation(String userID);

    @Query("SELECT * FROM Menstruation WHERE userID=:userID")
    List<Menstruation> getMonthlyMenstruation(String userID);
}
