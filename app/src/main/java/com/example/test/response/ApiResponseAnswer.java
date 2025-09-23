package com.example.test.response;

import com.example.test.model.Answer;
import com.example.test.model.Lesson;
import com.google.gson.annotations.SerializedName;

public class ApiResponseAnswer {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Answer data;

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

    public Answer getData() {
        return data;
    }

    public void setData(Answer data) {
        this.data = data;
    }
}
