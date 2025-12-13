package com.example.test.response;

import com.example.test.model.Game;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponseGame {
    @SerializedName("statusCode")
    private int statusCode;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Game> data;

    // Getters and Setters
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<Game> getData() { return data; }
    public void setData(List<Game> data) { this.data = data; }
}