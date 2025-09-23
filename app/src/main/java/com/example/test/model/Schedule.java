package com.example.test.model;

public class Schedule {

    private int id;
    private String userId;
    private String scheduleTime; // Định dạng ISO 8601 (yyyy-MM-ddTHH:mm:ssZ)
    private boolean isDaily;
    private Integer courseId; // Có thể null nếu không liên quan đến khóa học

    // Constructor
    public Schedule() {
    }

    public Schedule(String userId, String scheduleTime, boolean isDaily, Integer courseId) {
        this.userId = userId;
        this.scheduleTime = scheduleTime;
        this.isDaily = isDaily;
        this.courseId = courseId;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public boolean isDaily() {
        return isDaily;
    }

    public void setDaily(boolean daily) {
        isDaily = daily;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", userId=" + userId +
                ", scheduleTime='" + scheduleTime + '\'' +
                ", isDaily=" + isDaily +
                ", courseId=" + courseId +
                '}';
    }
}