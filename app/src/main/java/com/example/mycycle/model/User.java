package com.example.mycycle.model;

public class User {

    private String nickname;
    private String firstDay;
    private String profilePicture;
    private String userID;
    private int durationPeriod, durationMenstruation;

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

    public int getDurationPeriod() {
        return durationPeriod;
    }

    public User setDurationPeriod(int durationPeriod) {
        this.durationPeriod = durationPeriod;
        return this;
    }

    public int getDurationMenstruation() {
        return durationMenstruation;
    }

    public User setDurationMenstruation(int durationMenstruation) {
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

    @Override
    public String toString() {
        return "User{" +
                "nickname='" + nickname + '\'' +
                ", firstDay='" + firstDay + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", userID='" + userID + '\'' +
                ", durationPeriod=" + durationPeriod +
                ", durationMenstruation=" + durationMenstruation +
                '}';
    }
}
