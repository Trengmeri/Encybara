package com.example.test.response;

import com.example.test.model.SampleAnswer;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponseSampleAns {

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<SampleAnswer> data;

    // Getters and Setters
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

    public List<SampleAnswer> getData() {
        return data;
    }

    public void setData(List<SampleAnswer> data) {
        this.data = data;
    }
}