package com.example.test;

import android.content.Context;
import android.content.SharedPreferences;

public class VideoStatusManager {

    private static final String PREFS_NAME = "VideoStatusPrefs";
    private SharedPreferences sharedPreferences;

    public VideoStatusManager(Context context) {
        // Lấy context của ứng dụng để tránh memory leak
        this.sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Hàm kiểm tra xem video của một course đã được click chưa
    public boolean hasVideoBeenClicked(int courseId) {
        String key = "video_clicked_course_" + courseId;
        // Mặc định là false nếu chưa có key
        return sharedPreferences.getBoolean(key, false);
    }

    // Hàm để đánh dấu video của một course là đã được click
    public void setVideoClicked(int courseId) {
        String key = "video_clicked_course_" + courseId;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, true);
        editor.apply(); // Lưu thay đổi
    }
}