package com.example.test.response;

import com.example.test.model.Result;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponseResult {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Result> data; // Danh sách các Result

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

    public List<Result> getData() {
        return data;
    }

    public void setData(List<Result> data) {
        this.data = data;
    }
}