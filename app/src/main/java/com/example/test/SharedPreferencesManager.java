package com.example.test;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.test.model.User;
import com.google.gson.Gson;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "app_preferences";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_OTP_ID = "otp_id";

    private static SharedPreferencesManager instance;
    private final SharedPreferences sharedPreferences;


    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context);
        }
        return instance;
    }

    public void saveRequestCode(int requestCode) {
        sharedPreferences.edit().putInt("requestCode", requestCode).apply();
    }

    public int getRequestCode() {
        return sharedPreferences.getInt("requestCode", 1000); // Mặc định là 1000 nếu chưa có
    }


    public void saveID(String id) {
        sharedPreferences.edit().putString(KEY_USER_ID, id).apply();
    }

    public String getID() {
        return sharedPreferences.getString(KEY_USER_ID, "unknown_user");
    }

    public void saveOTP_ID(String otpID) {
        sharedPreferences.edit().putString(KEY_OTP_ID, otpID).apply();
    }

    public String getOTP_ID() {
        return sharedPreferences.getString(KEY_OTP_ID, "unknown_otp");
    }
    public void saveUser(User user) {
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        sharedPreferences.edit().putString("user", userJson).apply();
    }

    public User getUser() {
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("user", null);
        if (userJson!= null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public void saveAccessToken(String accessToken) {
        sharedPreferences.edit().putString("access_token", accessToken).apply();
    }
    
    public void saveToken(String token) {
        sharedPreferences.edit().putString("TOKEN_KEY", token).apply();
    }

    public String getToken() {
        String token = sharedPreferences.getString("TOKEN_KEY", "");
        return token;
    }
//    public void clearSession() {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", MODE_PRIVATE);
//        //SharedPreferences.Editor editor = sharedPreferences.edit();
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear(); // Xóa toàn bộ dữ liệu
//        editor.commit(); // 🛑 Đảm bảo xóa ngay lập tức
//
//        Log.d("Logout", "Đã xóa SharedPreferences");
//    }

    public String getAccessToken() {
        return sharedPreferences.getString("access_token", null);
    }
}

