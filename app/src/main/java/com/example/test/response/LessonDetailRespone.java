package com.example.test.response;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LessonDetailRespone {
    @SerializedName("statusCode")
    private int statusCode;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private LessonDetail data;

    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public LessonDetail getData() { return data; }

    public static class LessonDetail {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("questionIds")
        private List<Integer> questionIds; // List of question IDs

        public int getId() { return id; }
        public String getName() { return name; }
        public List<Integer> getQuestionIds() { return questionIds; }
    }
}