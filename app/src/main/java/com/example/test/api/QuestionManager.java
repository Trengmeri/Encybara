package com.example.test.api;

import android.content.Context;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.example.test.model.Answer;
import com.example.test.model.MediaFile;
import com.example.test.model.Question;
import com.example.test.model.Result;
import com.example.test.response.ApiResponseAnswer;
import com.example.test.response.ApiResponseMedia;
import com.example.test.response.ApiResponseQuestion;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuestionManager extends BaseApiManager {

    private final Context context;

    public QuestionManager(Context context) {
        this.context = context;
    }

    public void fetchQuestionContentFromApi(int questionId, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/questions/" + questionId)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body()!= null? response.body().string(): null;
                    if (responseBody!= null &&!responseBody.isEmpty()) {
                        Log.d("QuestionManager", "JSON trả về: " + responseBody);
                        try {
                            Gson gson = new Gson();
                            ApiResponseQuestion apiResponse = gson.fromJson(responseBody, ApiResponseQuestion.class);
                            Question question = apiResponse.getData();
                            if (question!= null && question.getQuestionChoices()!= null) {
                                callback.onSuccess(question);
                            } else {
                                Log.e("QuestionManager", "Câu hỏi hoặc câu trả lời không hợp lệ.");
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

    public void saveUserAnswer(int questionId, String answerContent, double point, String improvement, int enrollmentId, ApiCallback callback) {
        String userId = SharedPreferencesManager.getInstance(context).getID();

        try {
            // Chuyển answerContent thành List<String>
//            String[] answerParts = answerContent.split(", ");
//            List<String> answerList = new ArrayList<>(Arrays.asList(answerParts));

            // Tạo JSON object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("questionId", questionId);
            jsonObject.put("answerContent", answerContent); // Đảm bảo là mảng JSON
            jsonObject.put("pointAchieved", point);
            jsonObject.put("improvement", improvement);
            jsonObject.put("enrollmentId", enrollmentId);

            // Tạo RequestBody từ JSON
            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(BASE_URL + "/api/v1/answers/user/" + userId)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Không thể lưu câu trả lời: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Lỗi: " + response.code() + " - " + response.message());
                        Log.e("SaveAns", "Chi tiết lỗi: " + response.body().string()); // In nội dung lỗi
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure("JSON error: " + e.getMessage());
        }
    }

    public static void gradeAnswer(int answerId, Callback callback) {
        String url = BASE_URL + "/api/v1/answers/grade/" + answerId;

        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create("", MediaType.parse(""))) // Nếu không có body, có thể để trống
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void fetchAnswerId(int answerId, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/answers/" + answerId)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("QuestionManager", "Phản hồi từ server: " + responseBody);

                    Gson gson = new Gson();
                    ApiResponseAnswer apiResponse = gson.fromJson(responseBody, ApiResponseAnswer.class);
                    Answer answer = apiResponse.getData();

                    if (answer != null) {
                        Log.d("QuestionManager", "Answer ID: " + answer.getId() + "Cau tra loi: "+ answer.getAnswerContent() +", Điểm đạt được: " + answer.getPointAchieved() + ", Session ID: " + answer.getSessionId());
                        callback.onSuccess(answer); // Thay đổi ở đây
                    } else {
                        callback.onFailure("Không có câu trả lời nào.");
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