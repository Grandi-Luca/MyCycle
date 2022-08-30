package com.example.mycycle.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity(indices = {@Index(value = {"startDay", "userID"},
        unique = true)})
public class Menstruation {

    @ColumnInfo(name = "startDay")
    public String startDay;

    @ColumnInfo(name = "lastDay")
    public String lastDay;

    @PrimaryKey (autoGenerate = true)
    public int id;

    @ColumnInfo(name = "userID")
    public String userID;

    public Menstruation() {
    }

    public String getStartDay() {
        return startDay;
    }

    public Menstruation setStartDay(String startDay) {
        this.startDay = startDay;
        return this;
    }

    public String getLastDay() {
        return lastDay;
    }

    public Menstruation setLastDay(String lastDay) {
        this.lastDay = lastDay;
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public Menstruation setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    @Override
    public String toString() {
        return "Menstruation{" +
                "startDay=" + startDay +
                ", lastDay=" + lastDay +
                ", id=" + id +
                ", userID='" + userID + '\'' +
                '}';
    }
}
