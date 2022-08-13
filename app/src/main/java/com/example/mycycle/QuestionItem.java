package com.example.mycycle;


import android.net.Uri;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class QuestionItem {

    private String questionTitle, questionDescription, nickname, userID, uri;
    private ArrayList<ReplyItem> questionReplies;
    private long timestamp;

    public QuestionItem() {
        this.questionReplies = new ArrayList<>();
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public QuestionItem setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
        return this;
    }

    public String getUserID() {
        return userID;
    }

    public QuestionItem setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public String getQuestionDescription() {
        return questionDescription;
    }

    public QuestionItem setQuestionDescription(String questionDescription) {
        this.questionDescription = questionDescription;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public QuestionItem setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public ArrayList<ReplyItem> getQuestionReplies() {
        return questionReplies;
    }

    public QuestionItem setQuestionReplies(ArrayList<ReplyItem> questionReplies) {
        this.questionReplies = questionReplies;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public QuestionItem setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public QuestionItem setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "QuestionItem{" +
                "questionTitle='" + questionTitle + '\'' +
                ", questionDescription='" + questionDescription + '\'' +
                ", nickname='" + nickname + '\'' +
                ", userID='" + userID + '\'' +
                ", questionReplies=" + questionReplies +
                ", timestamp=" + timestamp +
                ", uri=" + uri +
                '}';
    }
}
