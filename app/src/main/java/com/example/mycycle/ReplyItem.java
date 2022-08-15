package com.example.mycycle;

import android.net.Uri;

import java.time.LocalDateTime;

public class ReplyItem {

    private String nickname, reply, postID, userID, uri;
    private long timestamp;

    public ReplyItem() {}

    public String getUserID() {
        return userID;
    }

    public ReplyItem setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public ReplyItem setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getReply() {
        return reply;
    }

    public ReplyItem setReply(String reply) {
        this.reply = reply;
        return this;
    }

    public String getPostID() {
        return postID;
    }

    public ReplyItem setPostID(String postID) {
        this.postID = postID;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public ReplyItem setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ReplyItem setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "ReplyItem{" +
                "nickname='" + nickname + '\'' +
                ", reply='" + reply + '\'' +
                ", postID='" + postID + '\'' +
                ", userID='" + userID + '\'' +
                ", uri='" + uri + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
