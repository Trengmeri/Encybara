package com.example.test.response;

import com.example.test.model.Flashcard;

public class ApiResponseOneFlashcard {
    private int statusCode;
    private String message;
    private Flashcard data;

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

    public Flashcard getData() {
        return data;
    }

    public void setData(Flashcard data) {
        this.data = data;
    }
}