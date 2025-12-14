package com.example.test.response;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuestionDetailRespone {
    @SerializedName("statusCode")
    private int statusCode;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private QuestionDetail data;

    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public QuestionDetail getData() { return data; }

    public static class QuestionDetail {
        @SerializedName("id")
        private int id;
        @SerializedName("quesContent")
        private String quesContent;
        @SerializedName("quesType")
        private String quesType;
        @SerializedName("point")
        private int point;
        @SerializedName("questionChoices")
        private List<QuestionChoice> questionChoices;
        private int lessonId;

        public int getLessonId() { return lessonId; }
        public void setLessonId(int lessonId) { this.lessonId = lessonId;}
        public int getId() { return id; }
        public String getQuesContent() { return quesContent; }
        public String getQuesType() { return quesType; }
        public int getPoint() { return point; }
        public List<QuestionChoice> getQuestionChoices() { return questionChoices; }
    }

    public static class QuestionChoice {
        @SerializedName("id")
        private int id;
        @SerializedName("choiceContent")
        private String choiceContent;
        @SerializedName("choiceKey")
        private boolean choiceKey; // true if it's the correct answer

        public int getId() { return id; }
        public String getChoiceContent() { return choiceContent; }
        public boolean isChoiceKey() { return choiceKey; }
    }
}