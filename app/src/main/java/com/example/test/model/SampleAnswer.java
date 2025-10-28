package com.example.test.model;

import com.google.gson.annotations.SerializedName;

public class SampleAnswer {

    @SerializedName("id")
    private int id;

    @SerializedName("answerContent")
    private String answerContent;

    @SerializedName("description")
    private String description;

    @SerializedName("difficultyLevel")
    private int difficultyLevel;

    @SerializedName("estimatedScore")
    private int estimatedScore;

    @SerializedName("questionId")
    private int questionId;

    @SerializedName("difficultyLevelText")
    private String difficultyLevelText;

    @SerializedName("scoreRange")
    private String scoreRange;

    // Các trường khác có thể là null
    @SerializedName("questionContent")
    private String questionContent;

    @SerializedName("audioLink")
    private String audioLink;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ... Thêm các getter và setter còn lại cho tất cả các trường
}
