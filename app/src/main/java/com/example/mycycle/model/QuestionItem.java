package com.example.mycycle.model;


import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class QuestionItem {

    private String questionTitle, questionDescription, nickname, userID, uri, postID;
    private List<ReplyItem> questionReplies;
    private long timestamp;

    public QuestionItem() {
        this.questionReplies = new ArrayList<>();
    }

    public String getPostID() {
        return postID;
    }

    public QuestionItem setPostID(String postID) {
        this.postID = postID;
        return this;
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

    public List<ReplyItem> getQuestionReplies() {
        return new ArrayList<>(questionReplies);
    }

    public QuestionItem setQuestionReplies(List<ReplyItem> questionReplies) {
        this.questionReplies.addAll(questionReplies);
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
