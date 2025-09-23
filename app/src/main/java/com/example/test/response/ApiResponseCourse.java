package com.example.test.response;

import androidx.annotation.Nullable;

import com.example.test.model.Course;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponseCourse {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Course data;

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

    public Course getData() {
        return data;
    }

    public void setData(Course data) {
        this.data = data;
    }
}