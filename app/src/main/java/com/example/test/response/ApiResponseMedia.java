package com.example.test.response;

import com.example.test.model.MediaFile;
import com.example.test.model.Result;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponseMedia {

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<MediaFile> data;

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public List<MediaFile> getData() {
        return data;
    }
    public void setData(List<MediaFile> data) {
        this.data = data;
    }
}