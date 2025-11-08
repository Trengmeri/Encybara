package com.example.test.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponeGameCourse {
    @SerializedName("statusCode")
    private int statusCode;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<GameData> data; // Changed to List<GameData> as per your JSON structure

    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public List<GameData> getData() { return data; }

    public static class GameData {
        @SerializedName("course")
        private Course course;
        // Other fields like id, name, description, gameType etc. can be added if needed

        public Course getCourse() { return course; }
    }

    public static class Course {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("lessons")
        private List<LessonSummary> lessons; // Summary of lessons

        public int getId() { return id; }
        public String getName() { return name; }
        public List<LessonSummary> getLessons() { return lessons; }
    }

    public static class LessonSummary {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        // Other fields like skillType, sumQues can be added if needed

        public int getId() { return id; }
        public String getName() { return name; }
    }
}
