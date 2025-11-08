package com.example.test.model;

import com.google.gson.annotations.SerializedName;

public class Game {
    private int id;
    private String name;
    private String description;
    @SerializedName("gameType")
    private String gameType;
    private Course course; // Chứa đối tượng Course
    @SerializedName("maxQuestions")
    private int maxQuestions;
    @SerializedName("timeLimit")
    private int timeLimit;
    @SerializedName("createBy")
    private String createBy;
    @SerializedName("createAt")
    private String createAt;
    @SerializedName("updateBy")
    private String updateBy;
    @SerializedName("updateAt")
    private String updateAt;
    private boolean active;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public int getMaxQuestions() { return maxQuestions; }
    public void setMaxQuestions(int maxQuestions) { this.maxQuestions = maxQuestions; }
    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public String getCreateAt() { return createAt; }
    public void setCreateAt(String createAt) { this.createAt = createAt; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public String getUpdateAt() { return updateAt; }
    public void setUpdateAt(String updateAt) { this.updateAt = updateAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}