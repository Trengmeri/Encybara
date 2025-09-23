package com.example.test.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Discussion {
    private int id;

    @SerializedName("userId")
    private int userID;

    @SerializedName("lessonId")
    private int lessonID;

    @SerializedName("content")
    private String content;

    @SerializedName("numLike")
    private int numLike;

    @SerializedName("replies")
    private List<Discussion> replies; // Danh sách các phản hồi (reply)

    private boolean isLiked;

    public Discussion(){}



    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getLessonID() {
        return lessonID;
    }

    public void setLessonID(int lessonID) {
        this.lessonID = lessonID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNumLike() {
        return numLike;
    }

    public void setNumLike(int numLike) {
        this.numLike = numLike;
    }
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean isLiked) { this.isLiked = isLiked; }

    public List<Discussion> getReplies() {
        return replies;
    }

    public void setReplies(List<Discussion> replies) {
        this.replies = replies;
    }
}
