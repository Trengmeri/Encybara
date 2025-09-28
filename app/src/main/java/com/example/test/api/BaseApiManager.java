package com.example.test.api;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public abstract class BaseApiManager {

    public static final String BASE_URL = "http://52.77.224.170:8080"; // Thay đổi URL của bạn nếu cần

    protected static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS) // Thời gian chờ kết nối
            .readTimeout(20, TimeUnit.SECONDS)    // Thời gian chờ đọc dữ liệu
            .writeTimeout(10, TimeUnit.SECONDS)   // Thời gian chờ ghi dữ liệu
            .build();

    public static String replaceHost(String url) {
        try {
            URI uri = new URI(BASE_URL);
            String host = uri.getHost();
            return url.replace("0.0.0.0", host);
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }
    }

}