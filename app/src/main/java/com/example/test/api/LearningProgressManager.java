package com.example.test.api;

import android.content.Context;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class LearningProgressManager extends BaseApiManager {
    private final Context context;
    private final Gson gson;

    public LearningProgressManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    // Trong LearningProgressManager.java
    public void fetchLatestEnrollment(ApiCallback<JsonObject> callback) {
        String userId = SharedPreferencesManager.getInstance(context).getID();
        String url = BASE_URL + "/api/v1/enrollments/user/" + userId + "?page=1&size=100";

        Log.d("LearningProgressManager", "Fetching enrollments for user: " + userId);
        Log.d("LearningProgressManager", "URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + SharedPreferencesManager.getInstance(context).getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("LearningProgressManager", "Network error", e);
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("LearningProgressManager", "Response code: " + response.code());
                Log.d("LearningProgressManager", "Response body: " + responseBody);

                if (response.isSuccessful()) {
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);


                    callback.onSuccess(jsonResponse);
                }
                    else {
                        callback.onFailure("Server error: " + response.code());
                    }
            }
        });
    }

    public void fetchCourseDetails(int courseId, ApiCallback<String> callback) {
        String url = BASE_URL + "/api/v1/courses/" + courseId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + SharedPreferencesManager.getInstance(context).getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    String courseName = jsonResponse.getAsJsonObject("data")
                            .get("name").getAsString();
                    callback.onSuccess(courseName);
                    Log.d("LearningProgressManager", "📌 Course name: " + courseName);
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public void fetchLearningResults(ApiCallback<JsonObject> callback) {
        String userId = SharedPreferencesManager.getInstance(context).getID();
        String url = BASE_URL + "/api/v1/learning-results/user/" + userId + "/detailed";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + SharedPreferencesManager.getInstance(context).getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    JsonObject data = jsonResponse.getAsJsonObject("data");
                    callback.onSuccess(data);
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public void fetchLatestLesson(ApiCallback<JsonObject> callback) {
        String userId = SharedPreferencesManager.getInstance(context).getID();
        String url = BASE_URL + "/api/v1/lesson-results/user/" + userId + "/latest";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + SharedPreferencesManager.getInstance(context).getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    JsonObject data = jsonResponse.getAsJsonObject("data");
                    callback.onSuccess(data);

                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public void fetchCourses(ApiCallback<JsonArray> callback) {
        String url = BASE_URL + "/api/v1/courses";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + SharedPreferencesManager.getInstance(context).getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                    // Extract the "content" field from the "data" object
                    if (jsonResponse.has("data") && jsonResponse.get("data").isJsonObject()) {
                        JsonObject data = jsonResponse.getAsJsonObject("data");
                        if (data.has("content") && data.get("content").isJsonArray()) {
                            JsonArray contentArray = data.getAsJsonArray("content");
                            callback.onSuccess(contentArray);
                        } else {
                            callback.onFailure("Expected a JsonArray in the 'content' field but found something else.");
                        }
                    } else {
                        callback.onFailure("Expected a JsonObject in the 'data' field but found something else.");
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }
}