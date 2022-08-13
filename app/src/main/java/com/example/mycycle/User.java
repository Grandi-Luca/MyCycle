package com.example.mycycle;

import android.net.Uri;

public class User {

    private String nickname, durationPeriod, durationMenstruation, firstDay, reminder, profilePicture, userID;
    private boolean pill;

    public User() {
    }

    public String getUserID() {
        return userID;
    }

    public User setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public User setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getDurationPeriod() {
        return durationPeriod;
    }

    public User setDurationPeriod(String durationPeriod) {
        this.durationPeriod = durationPeriod;
        return this;
    }

    public String getDurationMenstruation() {
        return durationMenstruation;
    }

    public User setDurationMenstruation(String durationMenstruation) {
        this.durationMenstruation = durationMenstruation;
        return this;
    }

    public String getFirstDay() {
        return firstDay;
    }

    public User setFirstDay(String firstDay) {
        this.firstDay = firstDay;
        return this;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public User setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
        return this;
    }
}
