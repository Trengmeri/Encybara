package com.example.test.api;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.test.SharedPreferencesManager;
import com.example.test.model.MediaFile;
import com.example.test.response.ApiResponseMedia;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LearningMaterialsManager extends BaseApiManager {
    private final Context context;

    public LearningMaterialsManager(Context context) {
        this.context = context;
    }

    public void fetchAndLoadImage(int questionId, ImageView imageView) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/material/questions/" + questionId)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("ImageAPI", "JSON trả về: " + responseBody);

                    try {
                        Gson gson = new Gson();
                        ApiResponseMedia apiResponse = gson.fromJson(responseBody, ApiResponseMedia.class);
                        List<MediaFile> mediaFiles = apiResponse.getData();

                        if (mediaFiles != null && !mediaFiles.isEmpty()) {
                            for (MediaFile media : mediaFiles) {
                                String mediaUrl = BaseApiManager.replaceHost(media.getMaterLink());
                                if (mediaUrl.matches(".*\\.(jpg|png|jpeg)$")) {  // Chỉ lấy ảnh
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        imageView.setVisibility(View.VISIBLE);
                                        Glide.with(context).load(mediaUrl).into(imageView);
                                    });
                                    return;  // Dừng sau khi load ảnh đầu tiên
                                }
                            }
                        }

                        // Nếu không có ảnh => Ẩn ImageView
                        new Handler(Looper.getMainLooper()).post(() -> imageView.setVisibility(View.GONE));

                    } catch (JsonSyntaxException e) {
                        Log.e("ImageAPI", "Lỗi khi parse JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("ImageAPI", "Lỗi từ server: Mã lỗi " + response.code()+ response.body());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ImageAPI", "Lỗi kết nối: " + e.getMessage());
            }
        });
    }



    public void fetchAudioByQuesId(int questionId, MediaPlayer mediaPlayer) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/material/questions/" + questionId)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("QuestionManager", "JSON trả về: " + responseBody);

                    try {
                        Gson gson = new Gson();
                        ApiResponseMedia apiResponse = gson.fromJson(responseBody, ApiResponseMedia.class);
                        List<MediaFile> mediaFiles = apiResponse.getData();

                        if (mediaFiles != null && !mediaFiles.isEmpty()) {
                            // Collect all MP3 URLs
                            List<String> mp3Urls = new ArrayList<>();
                            for (MediaFile media : mediaFiles) {
                                String mediaUrl = BaseApiManager.replaceHost(media.getMaterLink());
                                if (mediaUrl.endsWith(".mp3")) {
                                    mp3Urls.add(mediaUrl);
                                }
                            }

                            if (!mp3Urls.isEmpty()) {
                                // Define a Runnable to play MP3 files sequentially
                                final Handler handler = new Handler(Looper.getMainLooper());
                                final Runnable playNext = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mp3Urls.isEmpty()) {
                                            return; // No more files to play
                                        }
                                        try {
                                            if (mediaPlayer.isPlaying()) {
                                                mediaPlayer.stop();
                                                mediaPlayer.reset();
                                            }
                                            mediaPlayer.setDataSource(mp3Urls.get(0));
                                            mediaPlayer.prepareAsync();
                                            mediaPlayer.setOnPreparedListener(mp -> mp.start());
                                            mediaPlayer.setOnCompletionListener(mp -> {
                                                mp3Urls.remove(0);
                                                mediaPlayer.reset();
                                                handler.post(this); // Play the next file
                                            });
                                            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                                                Log.e("AudioError", "Lỗi MediaPlayer: what=" + what + ", extra=" + extra);
                                                mp3Urls.remove(0);
                                                mediaPlayer.reset();
                                                handler.post(this); // Skip to next file on error
                                                return true;
                                            });
                                        } catch (IOException e) {
                                            Log.e("AudioError", "Lỗi khi phát nhạc: " + e.getMessage());
                                            mp3Urls.remove(0);
                                            mediaPlayer.reset();
                                            handler.post(this); // Skip to next file on error
                                        }
                                    }
                                };
                                // Start playing the first file
                                handler.post(playNext);
                            } else {
                                Log.d("QuestionManager", "Không tìm thấy file MP3 nào.");
                            }
                        } else {
                            Log.d("QuestionManager", "Không có media files trong response.");
                        }
                    } catch (JsonSyntaxException e) {
                        Log.e("QuestionManager", "Lỗi khi parse JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("QuestionManager", "Lỗi từ server: Mã lỗi " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("QuestionManager", "Lỗi kết nối: " + e.getMessage());
            }
        });
    }

    public void fetchAndLoadImageByLesId(int lessonId, ImageView imageView) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/material/lessons/" + lessonId)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("ImageAPI", "JSON trả về: " + responseBody);

                    try {
                        Gson gson = new Gson();
                        ApiResponseMedia apiResponse = gson.fromJson(responseBody, ApiResponseMedia.class);
                        List<MediaFile> mediaFiles = apiResponse.getData();

                        if (mediaFiles != null && !mediaFiles.isEmpty()) {
                            for (MediaFile media : mediaFiles) {
                                String mediaUrl = BaseApiManager.replaceHost(media.getMaterLink());
                                if (mediaUrl.matches(".*\\.(jpg|png|jpeg)$")) {  // Chỉ lấy ảnh
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        imageView.setVisibility(View.VISIBLE);
                                        Glide.with(context).load(mediaUrl).into(imageView);
                                    });
                                    return;  // Dừng sau khi load ảnh đầu tiên
                                }
                            }
                        }

                        // Nếu không có ảnh => Ẩn ImageView
                        new Handler(Looper.getMainLooper()).post(() -> imageView.setVisibility(View.GONE));

                    } catch (JsonSyntaxException e) {
                        Log.e("ImageAPI", "Lỗi khi parse JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("ImageAPI", "Lỗi từ server: Mã lỗi " + response.code()+ response.body());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ImageAPI", "Lỗi kết nối: " + e.getMessage());
            }
        });
    }



    public void fetchAudioByLesId(int lessonId, ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/material/lessons/" + lessonId)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("QuestionManager", "JSON trả về: " + responseBody);

                    try {
                        Gson gson = new Gson();
                        ApiResponseMedia apiResponse = gson.fromJson(responseBody, ApiResponseMedia.class);
                        List<MediaFile> mediaFiles = apiResponse.getData();

                        if (mediaFiles != null && !mediaFiles.isEmpty()) {
                            for (MediaFile media : mediaFiles) {
                                Log.d("MediaFile", media.getFileType() + "   " + media.getMaterLink());
                                String mediaUrl = BaseApiManager.replaceHost(media.getMaterLink());

                                if (mediaUrl.endsWith(".mp3")) {  // So sánh đúng cách
                                    callback.onSuccess(mediaUrl);
                                    Log.d("AudioTest", "Gửi URL về callback: " + mediaUrl);
                                    return; // Chỉ gửi URL của file đầu tiên và thoát khỏi vòng lặp
                                }
                            }
                        }

                        // Nếu không có file audio hợp lệ, gọi callback thất bại
                        callback.onFailure("Không tìm thấy file audio phù hợp.");
                        Log.e("AudioTest", "Không tìm thấy file audio phù hợp.");
                    } catch (JsonSyntaxException e) {
                        Log.e("QuestionManager", "Lỗi khi parse JSON: " + e.getMessage());
                        callback.onFailure("Lỗi phân tích JSON");
                    }
                } else {
                    Log.e("QuestionManager", "Lỗi từ server: Mã lỗi " + response.code());
                    callback.onFailure("Lỗi từ server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("QuestionManager", "Lỗi kết nối: " + e.getMessage());
                callback.onFailure("Lỗi kết nối: " + e.getMessage());
            }
        });
    }


}
