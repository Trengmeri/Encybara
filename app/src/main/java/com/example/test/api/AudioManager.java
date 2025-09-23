package com.example.test.api;


import static com.example.test.api.BaseApiManager.BASE_URL;
import static com.example.test.api.BaseApiManager.client;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.example.test.SharedPreferencesManager;
import com.example.test.model.SpeechResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AudioManager extends BaseApiManager{
    private final Context context;

    public AudioManager(Context context) {
        this.context = context;
    }

    public void uploadAndTranscribeM4A(File file, ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file",
                        file.getName(),
                        RequestBody.create(file,MediaType.parse("audio/x-m4a")
                        )
                )
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/speech/convert") // Đây là endpoint tích hợp bạn nói
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();

                    try {
                        JSONObject root = new JSONObject(json);
                        JSONObject data = root.optJSONObject("data");

                        if (data != null) {
                            String transcript = data.optString("transcript");
                            double confidence = data.optDouble("confidence");

                            SpeechResult result = new SpeechResult(transcript, confidence);
                            callback.onSuccess(result);
                        } else {
                            callback.onFailure("Không tìm thấy kết quả trong phản hồi.");
                        }

                    } catch (JSONException e) {
                        callback.onFailure("Lỗi JSON: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure("Kết nối API thất bại: " + e.getMessage());
            }
        });
    }

//    public void convert(File file, ApiCallback callback){
//        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("file", file.getName(),
//                        RequestBody.create(file, MediaType.parse("audio/m4a")))
//                .build();
//        Request request = new Request.Builder()
//                .url(BASE_URL + "/api/v1/speech/conert") // Cập nhật BASE_URL của bạn
//                .addHeader("Authorization", "Bearer " + token)
//                .post(requestBody)
//                .build();
//        client.newCall(request).enqueue(new Callback() {
//
//        });
//    }


}
