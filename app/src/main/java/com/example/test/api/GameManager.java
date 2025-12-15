package com.example.test.api;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.test.response.ApiResponseGame;
import com.google.gson.Gson;
import com.example.test.SharedPreferencesManager;
import com.example.test.model.Course;
import com.example.test.model.Game;
import com.example.test.model.Lesson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameManager extends BaseApiManager{
    private final Context context;

    public GameManager(Context context) {
        this.context = context;
    }
    public void sendCreateGameRequest(int courseId, String name, String description, String gameType, int maxQuestions, int timeLimit, ApiCallback callback) {
        // Lấy access token từ SharedPreferences
        String accessToken = SharedPreferencesManager.getInstance(context).getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            callback.onFailure("Không tìm thấy Access Token! Vui lòng đăng nhập lại.");
            return;
        }

        // Tạo JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("courseId", courseId);
            jsonBody.put("name", name);
            jsonBody.put("description", description);
            jsonBody.put("gameType", gameType);
            jsonBody.put("maxQuestions", maxQuestions);
            jsonBody.put("timeLimit", timeLimit);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onFailure("Lỗi khi tạo request body JSON: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));

        // Tạo request
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/game/create")
                .header("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GameManager", "Kết nối thất bại: " + e.getMessage());
                callback.onFailure("Kết nối thất bại! Không thể kết nối tới API.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("GameManager", "Phản hồi từ server: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        if (jsonResponse.has("data")) {
                            JSONObject dataObj = jsonResponse.getJSONObject("data");
                            int gameId = dataObj.getInt("id");

                            Log.d("CreateGame", "Tạo game thành công, ID = " + gameId);
                            callback.onSuccess(gameId);
                        } else {
                            callback.onFailure("Phản hồi không có trường 'data'!");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onFailure("Lỗi khi phân tích phản hồi JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("GameManager", "Lỗi từ server: Mã lỗi " + response.code() + ", Nội dung: " + responseBody);
                    callback.onFailure("Tạo game thất bại! Mã lỗi: " + response.code() + ", Nội dung: " + responseBody);
                }
            }
        });
    }
    public void sendStartGameRequest(int gameId, ApiCallback callback) {
        String accessToken = SharedPreferencesManager.getInstance(context).getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            callback.onFailure("Không tìm thấy Access Token! Vui lòng đăng nhập lại.");
            return;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/game/" + gameId + "/start")
                .header("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create("", MediaType.parse("application/json; charset=utf-8")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GameManager", "Kết nối thất bại: " + e.getMessage());
                callback.onFailure("Kết nối thất bại! Không thể kết nối tới API.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("GameManager", "Phản hồi từ server: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        // Lấy đối tượng 'data'
                        if (jsonResponse.has("data")) {
                            JSONObject data = jsonResponse.getJSONObject("data");

                            // ✅ Lấy sessionId từ đối tượng 'data'
                            if (data.has("sessionId")) {
                                int sessionId = data.getInt("sessionId");
                                Log.d("StartGame", "Game started successfully. Session ID: " + sessionId);

                                // 🔑 Gọi onSuccess(Object result) và truyền sessionId (dưới dạng Integer)
                                callback.onSuccess(sessionId);

                            } else {
                                callback.onFailure("Phản hồi thành công nhưng không có sessionId trong dữ liệu.");
                            }
                        } else {
                            // Trường hợp không có data, nhưng status code là 200
                            Log.d("StartGame", "Game started successfully (No data)");
                            callback.onSuccess();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onFailure("Lỗi khi phân tích phản hồi JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("GameManager", "Lỗi từ server: Mã lỗi " + response.code() + ", Nội dung: " + responseBody);
                    callback.onFailure("Bắt đầu game thất bại! Mã lỗi: " + response.code() + ", Nội dung: " + responseBody);
                }
            }
        });
    }

    public void sendEndGameRequest(int sessionId, ApiCallback callback) {
        // 1. Kiểm tra Access Token
        String accessToken = SharedPreferencesManager.getInstance(context).getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            callback.onFailure("Không tìm thấy Access Token! Vui lòng đăng nhập lại.");
            return;
        }

        // 2. Xây dựng URL và Request
        // Endpoint: POST /api/v1/game/{sessionId}/end
        String url = BASE_URL + "/api/v1/game/" + sessionId + "/end";

        // Request POST, sử dụng body rỗng
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create("", MediaType.parse("application/json; charset=utf-8")))
                .build();

        // 3. Gửi yêu cầu bất đồng bộ
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GameManager", "Kết nối thất bại khi kết thúc game: " + e.getMessage());
                callback.onFailure("Kết nối thất bại! Không thể kết nối tới API kết thúc game.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("GameManager", "Phản hồi kết thúc game từ server: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        // Thường API kết thúc game chỉ trả về status 200/201,
                        // nhưng chúng ta vẫn nên kiểm tra JSON nếu có body trả về (ví dụ: điểm số cuối cùng)
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        Log.d("EndGame", "Game ended successfully for session ID: " + sessionId);

                        // Gọi onSuccess() không tham số để báo hiệu kết thúc thành công
                        callback.onSuccess();

                    } catch (JSONException e) {
                        Log.w("GameManager", "Phản hồi thành công nhưng không phải JSON hợp lệ. Tiếp tục coi là thành công.");
                        // Nếu không cần dữ liệu cụ thể từ body, có thể coi 200 là thành công
                        callback.onSuccess();
                    }
                } else {
                    Log.e("GameManager", "Lỗi từ server khi kết thúc game: Mã lỗi " + response.code() + ", Nội dung: " + responseBody);
                    callback.onFailure("Kết thúc game thất bại! Mã lỗi: " + response.code() + ", Nội dung: " + responseBody);
                }
            }
        });
    }

// Giả định bạn có các lớp BASE_URL, client, SharedPreferencesManager, và ApiCallback
// trong bối cảnh ứng dụng Android của bạn.

    public void sendAnswerRequest(Long sessionId, Long questionId, Long choiceId, ApiCallback callback) {
        // 1. Lấy Access Token (Giống như mẫu trước của bạn)
        String accessToken = SharedPreferencesManager.getInstance(context).getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            callback.onFailure("Không tìm thấy Access Token! Vui lòng đăng nhập lại.");
            return;
        }

        // 2. Tạo JSON Request Body
        JSONObject jsonBody = new JSONObject();
        try {
            // Cấu trúc body yêu cầu questionId và choiceId
            jsonBody.put("questionId", questionId);
            jsonBody.put("choiceId", choiceId);

            // Bạn có thể thêm các trường tùy chọn khác nếu cần, ví dụ:
            // jsonBody.put("timeTakenMs", System.currentTimeMillis() - questionStartTime);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onFailure("Lỗi khi tạo request body JSON: " + e.getMessage());
            return;
        }

        // Định nghĩa loại nội dung là JSON
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));

        // 3. Tạo Request
        // Đường dẫn: BASE_URL + /api/v1/game/{sessionId}/answer
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/game/" + sessionId + "/answer")
                .header("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        // 4. Thực hiện cuộc gọi không đồng bộ
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GameManager", "Kết nối thất bại khi gửi câu trả lời: " + e.getMessage());
                callback.onFailure("Kết nối thất bại! Không thể gửi câu trả lời.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("GameManager", "Phản hồi gửi câu trả lời: " + responseBody);

                if (response.isSuccessful()) {
                    // Xử lý phản hồi thành công (HTTP 200 OK)
                    handleSuccessfulAnswerResponse(responseBody, callback);
                } else {
                    // Xử lý phản hồi lỗi HTTP (4xx, 5xx)
                    Log.e("GameManager", "Lỗi từ server: Mã lỗi " + response.code());
                    callback.onFailure("Gửi câu trả lời thất bại! Mã lỗi: " + response.code() + ", Nội dung: " + responseBody);
                }
            }
        });
    }

    // Hàm tách riêng để xử lý logic phân tích JSON
    private void handleSuccessfulAnswerResponse(String responseBody, ApiCallback callback) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONObject dataObj = jsonResponse.optJSONObject("data");

            if (dataObj != null && dataObj.has("error")) {
                // Trường hợp lỗi nghiệp vụ: game đã kết thúc
                String errorMessage = dataObj.getString("error");
                Log.e("API_ANSWER", "Lỗi nghiệp vụ: " + errorMessage);
                callback.onFailure("Trò chơi đã kết thúc: " + errorMessage);

            } else if (dataObj != null) {
                // Trường hợp thành công bình thường (hoặc kết thúc game thành công)
                boolean isCorrect = dataObj.optBoolean("correct", false);
                int currentScore = dataObj.optInt("score", 0);
                boolean gameCompleted = dataObj.optBoolean("gameCompleted", false);

                if (gameCompleted) {
                    int finalScore = dataObj.optInt("finalScore", currentScore);
                    // Chuyển toàn bộ JSON dataObj cho callback để xử lý logic kết thúc game
                    callback.onSuccess(dataObj);
                } else {
                    // Chuyển điểm số hoặc toàn bộ dataObj
                    callback.onSuccess(currentScore);
                }
            } else {
                callback.onFailure("Phản hồi thành công nhưng không có trường 'data'!");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            callback.onFailure("Lỗi khi phân tích phản hồi JSON: " + e.getMessage());
        }
    }
}
