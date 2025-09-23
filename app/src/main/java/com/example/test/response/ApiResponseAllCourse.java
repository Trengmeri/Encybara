package com.example.test.response;

import com.example.test.model.Course;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponseAllCourse {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    // Getters và Setters
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        @SerializedName("content")
        private List<Course> courses;

        @SerializedName("totalPages")
        private int totalPages;

        // Getters and setters
        public List<Course> getCourses() {
            return courses;
        }

        public void setCourses(List<Course> courses) {
            this.courses = courses;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }
}