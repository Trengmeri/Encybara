package com.example.test.api;

import android.content.Context;

import com.example.test.SharedPreferencesManager;
import com.example.test.model.EnglishLevel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EnglishLevelManager extends BaseApiManager
{
    private final Context context;

    public EnglishLevelManager(Context context) {
        this.context = context;
    }

    public  void fetchEnglishLevels(ApiCallback<List<EnglishLevel>> callback) {

        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        String url = BASE_URL + "/api/v1/user-english-level/levels";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Connection error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    Gson gson = new Gson();

                    // Định nghĩa lớp bọc JSON
                    JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                    JsonArray dataArray = jsonObject.getAsJsonArray("data");

                    // Chuyển đổi JSON thành danh sách EnglishLevel
                    List<EnglishLevel> levels = Arrays.asList(gson.fromJson(dataArray, EnglishLevel[].class));

                    callback.onSuccess(levels);
                } else {
                    callback.onFailure("Request failed: " + response.code());
                }
            }
        });
    }


    public  void updateUserEnglishLevel (String level, ApiCallback callback) {
        String userId = SharedPreferencesManager.getInstance(context).getID();
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        String url = BASE_URL+ "/api/v1/user-english-level/" + userId + "?level=" + level;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .put(RequestBody.create("", MediaType.parse("")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Connection error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Request failed: " + response.code());
                }
            }
        });
    }
}
