package com.example.test.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.example.test.model.Review; // Giả định có lớp Review
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReviewManager extends BaseApiManager {
    private final Context context;

    public ReviewManager(Context context) {
        this.context = context;
    }

    // Kiểm tra token hợp lệ
    private String getValidToken() {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        if (token == null || token.isEmpty()) {
            return null;
        }
        return token;
    }

    // API tạo Review (Cập nhật theo body mới)
    public void createReview(int userId, int courseId, String reContent, String reSubject, int numStar, String status, ApiCallback<Review> callback) {
        String token = getValidToken();
        if (token == null) {
            callback.onFailure("Token không hợp lệ. Vui lòng đăng nhập lại.");
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
            jsonObject.put("courseId", courseId);
            jsonObject.put("reContent", reContent);
            jsonObject.put("reSubject", reSubject);
            jsonObject.put("numStar", numStar);
            jsonObject.put("status", status);
        } catch (JSONException e) {
            callback.onFailure("Lỗi tạo JSON: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/reviews")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Không thể tạo đánh giá: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("ReviewManager", "Tạo đánh giá thành công: " + responseBody);
                    Gson gson = new Gson();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject dataObject = jsonResponse.getJSONObject("data");
                        Review newReview = gson.fromJson(dataObject.toString(), Review.class);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(newReview));
                    } catch (JSONException e) {
                        callback.onFailure("Lỗi parse JSON: " + e.getMessage());
                    }

                } else {
                    String errorBody = response.body().string();
                    Log.e("ReviewManager", "Lỗi từ server: " + response.code() + " - " + errorBody);
                    callback.onFailure("Lỗi server (" + response.code() + "): " + errorBody);

                }
            }
        });
    }

    // API lấy danh sách Review theo courseId
    public void fetchReviewsByCourse(int courseId, int page, ApiCallback<List<Review>> callback) {
        String token = getValidToken();
        if (token == null) {
            callback.onFailure("Token không hợp lệ. Vui lòng đăng nhập lại.");
            return;
        }

        String url = BASE_URL + "/api/v1/reviews/course/" + courseId + "?page=" + page + "&size=10";  // tăng kich thuoc trang

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("API_RESPONSE", "Dữ liệu nhận được: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject dataObject = jsonResponse.getJSONObject("data");
                        JSONArray contentArray = dataObject.getJSONArray("content");

                        Gson gson = new Gson();
                        Type reviewListType = new TypeToken<List<Review>>() {}.getType();
                        List<Review> reviews = gson.fromJson(contentArray.toString(), reviewListType);

                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(reviews));
                    } catch (JSONException e) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Lỗi parse JSON: " + e.getMessage()));
                    }
                } else {
                    callback.onFailure("Lỗi từ server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Lỗi kết nối: " + e.getMessage());
            }
        });
    }



    // Like review
    public void likeReview(int userId, int reviewId, ApiCallback<Void> callback) {
        String token = getValidToken();
        if (token == null) {
            callback.onFailure("Token không hợp lệ. Vui lòng đăng nhập lại.");
            return;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/likes/review?userId=" + userId + "&reviewId=" + reviewId)
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create("", MediaType.get("application/json; charset=utf-8"))) // Body rỗng
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Lỗi kết nối: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                }
                else {
                    callback.onFailure("Lỗi: " + response.code());
                }
            }
        });
    }

    public void unlikeReview(int userId, int reviewId, ApiCallback<Void> callback) {
        String token = getValidToken();
        if (token == null) {
            callback.onFailure("Token không hợp lệ. Vui lòng đăng nhập lại.");
            return;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/likes/review?userId=" + userId + "&reviewId=" + reviewId)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Lỗi kết nối: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure("Lỗi: " + response.code());
                }
            }

        });
    }


    //Checklike
    public void isReviewLiked(int userId, int reviewId, ApiCallback<Boolean> callback) {
        String token = getValidToken();
        if (token == null) {
            callback.onFailure("Token không hợp lệ. Vui lòng đăng nhập lại.");
            return;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/likes/review-is-liked?userId=" + userId + "&reviewId=" + reviewId)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onSuccess(Boolean.parseBoolean(responseBody));
                } else {
                    callback.onFailure("Lỗi: " + response.code());
                }
            }


            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Lỗi kết nối: " + e.getMessage());
            }
        });
    }



}