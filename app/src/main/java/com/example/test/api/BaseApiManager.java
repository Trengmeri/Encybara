package com.example.test.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public abstract class BaseApiManager {

    public static final String BASE_URL = "http://14.225.198.3:8080"; // Thay đổi URL của bạn nếu cần
    protected static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS) // Thời gian chờ kết nối
            .readTimeout(20, TimeUnit.SECONDS)    // Thời gian chờ đọc dữ liệu
            .writeTimeout(10, TimeUnit.SECONDS)   // Thời gian chờ ghi dữ liệu
            .build();
}