package com.example.test.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.example.test.model.Course;
import com.example.test.model.Enrollment;
import com.example.test.response.ApiResponseCourse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CourseManager extends BaseApiManager{
    private final Context context;

    public CourseManager(Context context) {
        this.context = context;
    }

    public void fetchEnrollmentsByUser(int userId, boolean proStatus, int page, int size, ApiCallback<List<Enrollment>> callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();

        // Tạo URL với tham số proStatus
        String url = BASE_URL + "/api/v1/enrollments/user/" + userId +
                "?page=" + page + "&size=" + size + "&proStatus=" + proStatus;

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
                        Type enrollmentListType = new TypeToken<List<Enrollment>>() {}.getType();
                        List<Enrollment> enrollments = gson.fromJson(contentArray.toString(), enrollmentListType);

                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(enrollments));
                    } catch (JSONException e) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Lỗi parse JSON: " + e.getMessage()));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Lỗi: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Lỗi kết nối: " + e.getMessage()));
            }
        });
    }
    public void fetchCourseById(int courseId, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/courses/" + courseId ) // Thay bằng URL máy chủ của bạn
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("LessonManager", "Phản hồi từ server: " + responseBody);

                    Gson gson = new Gson();
                    ApiResponseCourse apiResponse = gson.fromJson(responseBody, ApiResponseCourse.class);

                    if (apiResponse.getStatusCode() == 200) {
                        Course course = apiResponse.getData();
                        callback.onSuccess(course);
                    } else {
                        callback.onFailure("Lỗi từ server: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e("LessonManager", "Lỗi từ server: Mã lỗi " + response.code());
                    callback.onFailure("Lỗi từ server: Mã lỗi " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("LessonManager", "Lỗi kết nối: " + e.getMessage());
                callback.onFailure("Không thể kết nối tới API.");
            }
        });
    }

    public void fetchGroupCourses(ApiCallback<List<String>> callback) {
        String url = BASE_URL + "/api/v1/courses/groups";

        Request request = new Request.Builder()
                .url(url)
                .get()
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

                    try {
                        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                        JsonArray contentArray = jsonObject.getAsJsonObject("data").getAsJsonArray("content");

                        // Chuyển đổi JsonArray thành List<String>
                        List<String> groupCourses = new ArrayList<>();
                        for (JsonElement element : contentArray) {
                            groupCourses.add(element.getAsString());
                        }

                        callback.onSuccess(groupCourses);
                    } catch (Exception e) {
                        callback.onFailure("Parsing error: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Request failed: " + response.code());
                }
            }
        });
    }


    public void fetchCoursesByGroupName(String name, ApiCallback<List<Course>> callback) {
        String url = BASE_URL + "/api/v1/courses/group/" + name;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Lỗi kết nối: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Lỗi từ server: Mã lỗi " + response.code());
                    return;
                }

                String responseBody = response.body().string();
                Log.d("CourseManager", "📌 Phản hồi từ server: " + responseBody);

                Gson gson = new Gson();

                try {
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                    if (!jsonResponse.has("data") || jsonResponse.get("data").isJsonNull()) {
                        callback.onFailure("Dữ liệu không hợp lệ từ server");
                        return;
                    }

                    JsonObject dataObject = jsonResponse.getAsJsonObject("data");
                    JsonArray contentArray = dataObject.getAsJsonArray("content");

                    // Chuyển đổi JSON thành danh sách các khóa học
                    Course[] coursesArray = gson.fromJson(contentArray, Course[].class);
                    List<Course> courses = Arrays.asList(coursesArray);

                    callback.onSuccess(courses);
                } catch (Exception e) {
                    callback.onFailure("Lỗi khi xử lý JSON: " + e.getMessage());
                }
            }
        });
    }

    public void joinCourse(int courserId, ApiCallback callback) {
            String token = SharedPreferencesManager.getInstance(context).getAccessToken();
            String url = BASE_URL + "/api/v1/enrollments/" + courserId + "/join";
            RequestBody body = RequestBody.create("", MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .put(body) // Đúng cú pháp put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Connection error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    if (response.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure("Request failed: " + response.code() + " - " + responseBody);
                    }
                }

            });
        }
    public void creatEnrollment(int userId, int courseId, ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        String url = BASE_URL + "/api/v1/enrollments";

        // Tạo JSON body
        String jsonBody = "{ \"userId\": " + userId + ", \"courseId\": " + courseId + " }";
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .post(body)  // Gửi POST request
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Connection error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure("Request failed: " + response.code() + " - " + responseBody);
                }
            }
        });
    }





}
