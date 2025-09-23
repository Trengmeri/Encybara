package com.example.test.model;

import com.google.gson.annotations.SerializedName;

public class Enrollment {
    private int id;
    private int enrollmentId;
    private int userId;
    private int courseId;
    private String proStatus;
    private int totalPoints;
    private double comLevel;

    public String getProStatus() {
        return proStatus;
    }

    public void setProStatus(String proStatus) {
        this.proStatus = proStatus;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }
    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
    public double getComLevel() {
        return comLevel;
    }
    public void setComLevel(double comLevel) {
        this.comLevel = comLevel;
    }
    public int getTotalPoints() {
        return totalPoints;
    }
    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getCourseId() {
        return courseId;
    }

}
