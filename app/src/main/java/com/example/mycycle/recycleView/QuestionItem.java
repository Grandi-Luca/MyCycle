package com.example.mycycle.recycleView;


import androidx.annotation.NonNull;

import com.example.mycycle.ReplyItem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("unused")
public class QuestionItem {

    private String questionTitle, questionDescription, nickname, userID, uri, postID;
    private Queue<ReplyItem> questionReplies;
    private long timestamp;
    private boolean isExpand;

    public QuestionItem() {
        this.questionReplies = new ArrayDeque<>();
        this.isExpand = false;
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

    public QuestionItem setQuestionReplies(Queue<ReplyItem> questionReplies) {
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

    public boolean isExpand() {
        return isExpand;
    }

    public void toggleExpand() {
        this.isExpand = !this.isExpand;
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
