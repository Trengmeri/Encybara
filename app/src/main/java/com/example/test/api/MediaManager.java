package com.example.test.api;

import android.content.Context;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.example.test.model.MediaFile;
import com.example.test.response.ApiResponseMedia;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MediaManager extends BaseApiManager {

    private final Context context;

    public MediaManager(Context context) {
        this.context = context;
    }

    public void fetchMediaByQuesId(int questionId, ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/material/questions/" + questionId)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : null;
                    if (responseBody != null && !responseBody.isEmpty()) {
                        Log.d("QuestionManager", "JSON trả về: " + responseBody);
                        try {
                            Gson gson = new Gson();
                            ApiResponseMedia apiResponse = gson.fromJson(responseBody, ApiResponseMedia.class);

                            // Lấy danh sách MediaFile từ apiResponse.getData()
                            List<MediaFile> mediaFiles = apiResponse.getData();
                            String jsonResults = gson.toJson(mediaFiles);
                            Log.d("ResultManager", "Dữ liệu JSON: " + jsonResults);

                            if (!mediaFiles.isEmpty()) {
                                MediaFile media = mediaFiles.get(0);
                                Log.d("QuestionManager", "Link media: " + media.getMaterLink());
                                callback.onSuccess(media);
                            } else {
                                Log.e("QuestionManager", "Dữ liệu không hợp lệ từ server.");
                                callback.onFailure("Dữ liệu không hợp lệ từ server.");
                            }
                        } catch (JsonSyntaxException e) {
                            Log.e("QuestionManager", "Lỗi khi parse JSON: " + e.getMessage());
                            callback.onFailure("Lỗi khi parse JSON.");
                        }
                    } else {
                        Log.e("QuestionManager", "Body trả về rỗng hoặc không hợp lệ.");
                        callback.onFailure("Dữ liệu không hợp lệ từ server.");
                    }
                } else {
                    Log.e("QuestionManager", "Lỗi từ server: Mã lỗi " + response.code());
                    callback.onFailure("Lỗi từ server: Mã lỗi " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("QuestionManager", "Lỗi kết nối: " + e.getMessage());
                callback.onFailure("Không thể kết nối tới API.");
            }
        });
    }
}
