package com.example.test.response;

import com.example.test.model.Answer;
import com.example.test.model.Enrollment;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponseEnrollment {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Enrollment data; // Sử dụng lớp Data để chứa danh sách Answer

    // Thêm thông tin phân trang
    @SerializedName("page")
    private int page;

    @SerializedName("totalPages")
    private int totalPages;

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

    public Enrollment getData() {
        return data;
    }

    public void setData(Enrollment data) {
        this.data = data;
    }
}
