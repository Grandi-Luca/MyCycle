package com.example.mycycle;

public class User {

    private String nickname, durationPeriod, durationMenstruation, firstDay, reminder;
    private boolean pill;

    public User() {
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
}
