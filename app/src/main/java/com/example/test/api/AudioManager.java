package com.example.test.api;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.test.model.PhonemeScore;
import com.example.test.model.PronunciationResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private static final String PRONUNCIATION_API_URL = BASE_URL.replace(":8080", ":5000") +"/api/pronunciation-assessment";

    /**
     * Gửi file âm thanh và transcript để chấm điểm phát âm.
     * @param audioFile File âm thanh (nên là .mp3 hoặc .wav)
     * @param transcript Văn bản mà người dùng đã đọc
     * @param callback Callback để xử lý kết quả
     */
    public void assessPronunciation(File audioFile, String transcript, ApiCallback<PronunciationResult> callback) {
        // Tạo một MultipartBody để gửi cả file và text
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "audio", // Key cho file âm thanh
                        audioFile.getName(),
                        RequestBody.create(audioFile, MediaType.parse("audio/mpeg")) // Giả định là file mp3
                )
                .addFormDataPart("transcript", transcript) // Key cho văn bản
                .build();

        Request request = new Request.Builder()
                .url(PRONUNCIATION_API_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Lỗi server: " + response.code() + " " + response.message());
                    return;
                }

                String json = response.body().string();
                Log.d("PRONUNCIATION_API", "Response JSON: " + json);

                try {
                    JSONObject root = new JSONObject(json);
                    JSONObject data = root.optJSONObject("data");

                    if (data != null) {
                        // Lấy điểm tổng thể
                        double overallScore = data.optDouble("overall_score");

                        // Xử lý mảng phoneme_scores
                        JSONArray phonemeScoresArray = data.optJSONArray("phoneme_scores");
                        List<PhonemeScore> phonemeScoresList = new ArrayList<>();

                        if (phonemeScoresArray != null) {
                            for (int i = 0; i < phonemeScoresArray.length(); i++) {
                                JSONObject phonemeObject = phonemeScoresArray.getJSONObject(i);
                                String character = phonemeObject.optString("character");
                                String phoneme = phonemeObject.optString("phoneme");
                                String quality = phonemeObject.optString("quality");
                                int wordIndex = phonemeObject.optInt("word_index"); // <-- THÊM DÒNG NÀY

                                // Cập nhật lại lệnh khởi tạo
                                phonemeScoresList.add(new PhonemeScore(character, phoneme, quality, wordIndex));
                            }
                        }

                        // Tạo đối tượng kết quả cuối cùng
                        PronunciationResult result = new PronunciationResult(overallScore, phonemeScoresList);
                        callback.onSuccess(result);

                    } else {
                        callback.onFailure("Không tìm thấy trường 'data' trong phản hồi.");
                    }

                } catch (JSONException e) {
                    callback.onFailure("Lỗi phân tích JSON: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure("Kết nối API thất bại: " + e.getMessage());
            }
        });
    }
}
