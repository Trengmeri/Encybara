package com.example.test.model;

import com.google.gson.annotations.SerializedName;

public class Result {
    private int id;
    private int stuTime;
    private double comLevel; // Thêm trường comLevel
    private int sessionId; // Thêm trường sessionId
    private int totalPoints;
    private int lessionId;
    private int enrollmentId;

    @SerializedName("user")
    private User user;

    @SerializedName("lesson")
    private Lesson lesson;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLessionId() {
        return lessionId;
    }

    public void setLessionId(int lessionId) {
        this.lessionId = lessionId;
    }

    public int getStuTime() {
        return stuTime;
    }

    public void setStuTime(int stuTime) {
        this.stuTime = stuTime;
    }

    public double getComLevel() { // Getter cho comLevel
        return comLevel;
    }

    public void setComLevel(double comLevel) { // Setter cho comLevel
        this.comLevel = comLevel;
    }

    public int getSessionId() { // Getter cho sessionId
        return sessionId;
    }

    public void setSessionId(int sessionId) { // Setter cho sessionId
        this.sessionId = sessionId;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
}