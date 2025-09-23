package com.example.test.model;

import com.google.gson.annotations.SerializedName;

public class Answer {
    private int id; // ID của câu trả lời

    @SerializedName("answerContent")
    private String answerContent;

//    @SerializedName("point_achieved") // Ánh xạ với trường trong JSON
    private int pointAchieved; // Điểm đạt được
    private int sessionId; // ID của phiên

    // Getter và Setter cho id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAnswerContent(){
        return answerContent;
    }

    public void setAnswerContent(String answerContent){
        this.answerContent = answerContent;
    }

    // Getter và Setter cho pointAchieved
    public int getPointAchieved() {
        return pointAchieved;
    }

    public void setPointAchieved(int pointAchieved) {
        this.pointAchieved = pointAchieved;
    }

    // Getter và Setter cho sessionId
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
}