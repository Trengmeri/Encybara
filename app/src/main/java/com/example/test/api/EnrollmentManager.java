package com.example.test.api;

import android.content.Context;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.example.test.model.Course;
import com.example.test.model.Enrollment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class EnrollmentManager extends BaseApiManager
{
    private final Context context;

    public EnrollmentManager(Context context) {
        this.context = context;
    }

    public void fetchAllEnrolledCourseIds( ApiCallback<List<Integer>> callback) {
        List<Integer> allCourseIds = new ArrayList<>();
        fetchEnrollmentsByPage(0, allCourseIds, callback);
    }

    private void fetchEnrollmentsByPage(int page, List<Integer> allCourseIds, ApiCallback<List<Integer>> callback) {
        String userId = SharedPreferencesManager.getInstance(context).getID();
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        String url = BASE_URL + "/api/v1/enrollments/user/" + userId +"?page=" + page ;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
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
                Log.d("EnrollmentManager", "📌 Phản hồi từ server: " + responseBody);
                Gson gson = new Gson();

                try {
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                    int totalPages = jsonResponse.getAsJsonObject("data").get("totalPages").getAsInt();
                    Type listType = new TypeToken<List<Enrollment>>() {}.getType();
                    List<Enrollment> enrollments = gson.fromJson(jsonResponse.getAsJsonObject("data").get("content"), listType);


                    for (Enrollment enrollment : enrollments) {
                        allCourseIds.add(enrollment.getCourseId());
                    }

                    if (page + 1 < totalPages) {
                        fetchEnrollmentsByPage(page + 1, allCourseIds, callback);
                    } else {
                        callback.onSuccess(allCourseIds);
                    }

                } catch (Exception e) {
                    callback.onFailure("Lỗi khi xử lý JSON: " + e.getMessage());
                }
            }
        });
    }

}
