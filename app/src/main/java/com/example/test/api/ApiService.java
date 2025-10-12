package com.example.test.api;

import static com.example.test.api.BaseApiManager.BASE_URL;

import android.content.Context;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.example.test.model.Enrollment;
import com.example.test.model.EvaluationResult;
import com.example.test.response.ApiResponseEnrollment;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {
    private final Context context;
    private final OkHttpClient client  = new OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)  // Thời gian kết nối tối đa
            .readTimeout(90, TimeUnit.SECONDS)     // Thời gian đọc dữ liệu tối đa
            .writeTimeout(90, TimeUnit.SECONDS)    // Thời gian ghi dữ liệu tối đa
            .build();


    public ApiService(Context context) {
        this.context = context;
    }

    public void sendAnswerToApi(String question, String userAnswer, ApiCallback<EvaluationResult> callback) {

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("question", question);
            jsonBody.put("answer", userAnswer);

            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

            String scoringUrl = BASE_URL.replace(":8080", ":5001") + "/api/content-scoring";

            Request request = new Request.Builder()
                    .url(scoringUrl)
                    .post(body)
                    .build();

            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        callback.onFailure("API error: " + response.message());
                        return;
                    }
                    String responseBody = response.body().string();
                    Log.d("API_RESPONSE", "JSON: " + responseBody);

                    JSONObject responseObject = new JSONObject(responseBody);

                    // --- PHẦN SỬA LẠI ---

                    // 1. Lấy "score" trực tiếp từ object gốc, không thông qua "data"
                    double point = responseObject.getDouble("score");

                    // 2. Lấy object con "advanced_answer"
                    JSONObject advancedAnswerObject = responseObject.getJSONObject("advanced_answer");

                    // 3. Lấy "suggestion" từ object con
                    String improvements = advancedAnswerObject.getString("suggestion");

                    // 4. Lấy mảng "improvement_points" và chuyển thành một String duy nhất
                    JSONArray improvementPointsArray = advancedAnswerObject.getJSONArray("improvement_points");
                    StringBuilder evaluationBuilder = new StringBuilder();
                    for (int i = 0; i < improvementPointsArray.length(); i++) {
                        // Nối các phần tử trong mảng lại, có thể thêm dấu gạch đầu dòng cho đẹp
                        evaluationBuilder.append("- ").append(improvementPointsArray.getString(i));
                        // Thêm ký tự xuống dòng nếu không phải là phần tử cuối cùng
                        if (i < improvementPointsArray.length() - 1) {
                            evaluationBuilder.append("\n");
                        }
                    }
                    String evaluation = evaluationBuilder.toString();

                    // Tạo đối tượng kết quả với dữ liệu đã được đọc chính xác
                    EvaluationResult result = new EvaluationResult(improvements, evaluation, point);
                    callback.onSuccess(result);

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure("Request error: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            callback.onFailure("JSON creation error: " + e.getMessage());
        }
    }

    public void getSuggestionFromApi(String question, ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("question", question);
            jsonBody.put("prompt", "For student at basic level");

            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(BASE_URL + "/api/v1/perplexity/suggest")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
                    .build();

            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        callback.onFailure("API error: " + response.message());
                        return;
                    }
                    String responseBody = response.body().string(); // Lưu body vào biến trước
                    Log.d("API_RESPONSE", "JSON: " + responseBody);

                    JSONObject responseObject = new JSONObject(responseBody);
                    JSONObject dataObject = responseObject.getJSONObject("data");
                    String tip = dataObject.getString("tips");// Lấy object `data`

                    callback.onSuccess(tip);

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure("Request error: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            callback.onFailure("JSON creation error: " + e.getMessage());
        }
    }
    public void startTest(ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        String userId = SharedPreferencesManager.getInstance(context).getID();

        RequestBody emptyBody = RequestBody.create("", MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/initial-assessment/" + userId + "/start")
                .addHeader("Authorization", "Bearer " + token)
                .post(emptyBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Start Test Failed: " + e.getMessage());
                callback.onFailure("Request error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_RESPONSE", "Start Test Response: " + responseBody);

                try {
                    JSONObject responseObject = new JSONObject(responseBody);
                    int statusCode = responseObject.optInt("statusCode", -1);

                    if (statusCode == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Failed with status: " + statusCode);
                    }
                } catch (Exception e) {
                    callback.onFailure("JSON parsing error: " + e.getMessage());
                }
            }
        });
    }
    public void skipTest(ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        String userId = SharedPreferencesManager.getInstance(context).getID();

        RequestBody emptyBody = RequestBody.create("", MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/initial-assessment/" + userId + "/skip")
                .addHeader("Authorization", "Bearer " + token)
                .post(emptyBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Skip Test Failed: " + e.getMessage());
                callback.onFailure("Request error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_RESPONSE", "Skip Test Response: " + responseBody);

                try {
                    JSONObject responseObject = new JSONObject(responseBody);
                    int statusCode = responseObject.optInt("statusCode", -1);

                    if (statusCode == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Failed with status: " + statusCode);
                    }
                } catch (Exception e) {
                    callback.onFailure("JSON parsing error: " + e.getMessage());
                }
            }
        });
    }

    public void completeTest(int enrollmentId, double comp, int point, int r, int l, int s, int w, ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();

        String json = "{ " +
                "\"comLevel\":" + comp
                + ", \"totalPoints\":" + point
                + ", \"enrollmentId\":" + enrollmentId
                + ", \"readingTotalPoints\":" + r
                + ", \"listeningTotalPoints\":" + l
                + ", \"speakingTotalPoints\":" + s
                + ", \"writingTotalPoints\":" + w
                + "}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/initial-assessment/complete")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Complete Test Failed: " + e.getMessage());
                callback.onFailure("Request error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_RESPONSE", "Complete Test Response: " + responseBody);

                try {
                    JSONObject responseObject = new JSONObject(responseBody);
                    int statusCode = responseObject.optInt("statusCode", -1);

                    if (statusCode == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Failed with status: " + statusCode);
                    }
                } catch (Exception e) {
                    callback.onFailure("JSON parsing error: " + e.getMessage());
                }
            }
        });
    }

    public void saveEnrollment(int enrollmentId, ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();

        RequestBody emptyBody = RequestBody.create("", MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/enrollments/"+ enrollmentId +"/save-completion")
                .addHeader("Authorization", "Bearer " + token)
                .post(emptyBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("saveEnrollment", "Phản hồi từ server: " + responseBody);

                    Gson gson = new Gson();

                    ApiResponseEnrollment apiResponse = gson.fromJson(responseBody, ApiResponseEnrollment.class);

                    if (apiResponse.getStatusCode() == 200) {
                        Enrollment enrollment = apiResponse.getData(); // Lấy `data` trong JSON

                        if (enrollment != null) {
                            Log.d("saveEnrollment", "Enrollment ID: " + enrollment.getId() +
                                    ", Point: " + enrollment.getTotalPoints() +
                                    ", Comp: " + enrollment.getComLevel());
                            callback.onSuccess(enrollment);
                        } else {
                            callback.onFailure("Không có dữ liệu Enrollment.");
                        }
                    } else {
                        callback.onFailure("Lỗi từ server: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e("saveEnrollment", "Lỗi từ server: Mã lỗi " + response.code());
                    callback.onFailure("Lỗi từ server: Mã lỗi " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("saveEnrollment", "Lỗi kết nối: " + e.getMessage());
                callback.onFailure("Không thể kết nối tới API.");
            }
        });
    }


    public void recomment(int enrollmentId, ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();

        RequestBody emptyBody = RequestBody.create("", MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/enrollments/"+ enrollmentId +"/recommendations")
                .addHeader("Authorization", "Bearer " + token)
                .post(emptyBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Save Enrollment Failed: " + e.getMessage());
                callback.onFailure("Request error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_RESPONSE", "Save Enrollment Response: " + responseBody);

                try {
                    JSONObject responseObject = new JSONObject(responseBody);
                    int statusCode = responseObject.optInt("statusCode", -1);

                    if (statusCode == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Failed with status: " + statusCode);
                    }
                } catch (Exception e) {
                    callback.onFailure("JSON parsing error: " + e.getMessage());
                }
            }
        });
    }

    public void updateResult(int enrollmentId, ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();

        RequestBody emptyBody = RequestBody.create("", MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/enrollments/"+ enrollmentId +"/update-learning")
                .addHeader("Authorization", "Bearer " + token)
                .post(emptyBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Save Enrollment Failed: " + e.getMessage());
                callback.onFailure("Request error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_RESPONSE", "Save Enrollment Response: " + responseBody);

                try {
                    JSONObject responseObject = new JSONObject(responseBody);
                    int statusCode = responseObject.optInt("statusCode", -1);

                    if (statusCode == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Failed with status: " + statusCode);
                    }
                } catch (Exception e) {
                    callback.onFailure("JSON parsing error: " + e.getMessage());
                }
            }
        });
    }
}
