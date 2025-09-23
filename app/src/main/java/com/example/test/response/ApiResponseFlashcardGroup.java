package com.example.test.response;

import com.example.test.model.FlashcardGroup;
import com.google.gson.annotations.SerializedName;

public class ApiResponseFlashcardGroup {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private FlashcardGroup data;

    // Getters và Setters
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FlashcardGroup getData() {
        return data;
    }

    public void setData(FlashcardGroup data) {
        this.data = data;
    }
}