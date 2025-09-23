package com.example.test.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.example.test.model.FlashcardGroup;
import com.example.test.response.ApiResponseFlashcard;
import com.example.test.response.ApiResponseFlashcardGroup;
import com.example.test.response.ApiResponseOneFlashcard;
import com.example.test.response.FlashcardGroupResponse;
import com.example.test.model.WordData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

public class FlashcardManager extends BaseApiManager {
    private Gson gson;
    public FlashcardManager() {
        gson = new Gson();
    }

    public void fetchFlashcardGroups(Context context,int userId, int page,int size, FlashcardApiCallback callback) {
        String url = BASE_URL + "/api/v1/flashcard-groups/user/" + userId + "?page=" + page+ "&size=" + size;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    FlashcardGroupResponse apiResponse = gson.fromJson(responseBody, FlashcardGroupResponse.class);
                    callback.onSuccess(apiResponse);
                    // Trích xuất danh sách tên nhóm và gọi phương thức mới
                    List<String> groupNames = new ArrayList<>();
                    if (apiResponse != null && apiResponse.getData() != null) {
                        for (FlashcardGroup group : apiResponse.getData().getContent()) {
                            groupNames.add(group.getName());
                        }
                    }
                    SharedPreferences sharedPreferences = context.getSharedPreferences("FlashcardPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    Gson gson = new Gson();
                    String json = gson.toJson(groupNames); // Chuyển danh sách thành chuỗi JSON
                    editor.putString("group_list", json);
                    editor.apply();
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        });
    }
    public void createFlashcardGroup(String groupName, int userId, FlashcardApiCallback callback) {
        String url = BASE_URL + "/api/v1/flashcard-groups";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        // Tạo JSON body
        String jsonBody = "{\"name\":\"" + groupName + "\", \"userId\":" + userId + "}";
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.e("API_RESPONSE", "Dữ liệu trả về: " + responseBody); // Kiểm tra JSON từ API

                if (response.isSuccessful()) {
                    try {
                        Gson gson = new Gson();
                        ApiResponseFlashcardGroup apiResponse = gson.fromJson(responseBody, ApiResponseFlashcardGroup.class);

                        if (apiResponse != null && apiResponse.getData() != null) {
                            callback.onSuccess(apiResponse); // Chỉ gọi một lần với dữ liệu đúng
                        } else {
                            callback.onFailure("Dữ liệu nhóm flashcard bị null từ API.");
                        }
                    } catch (JsonSyntaxException e) {
                        callback.onFailure("Lỗi phân tích JSON: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        });
    }



    public void fetchFlashcardsInGroup(int groupId,int page,int size, FlashcardApiCallback callback) {
        String url = BASE_URL + "/api/v1/flashcard-groups/" + groupId + "?page=" + page+ "&size=" + size;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    ApiResponseFlashcard apiResponse = gson.fromJson(responseBody, ApiResponseFlashcard.class);
                    callback.onSuccess(apiResponse);
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        });
    }

    public void updateFlashcardGroup(int groupId, String newName, FlashcardApiCallback callback) {
        String url = BASE_URL + "/api/v1/flashcard-groups/" + groupId + "?newName=" + newName;
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create("", MediaType.parse("application/json; charset=utf-8"))) // Body rỗng cho PUT
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(new ApiResponseFlashcardGroup()); // Tạo một đối tượng thành công
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        });
    }

    public void deleteFlashcardGroup(int groupId, FlashcardApiCallback callback) {
        String url = BASE_URL + "/api/v1/flashcard-groups/" + groupId;
        Request request = new Request.Builder()
                .url(url)
                .delete() // Gọi phương thức DELETE
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(new ApiResponseFlashcardGroup()); // Tạo một đối tượng thành công
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        });
    }

    public void fetchFlashcardById(int flashcardId, FlashcardApiCallback callback) {
        Log.d("FlashcardManager", "Starting API call for flashcard ID: " + flashcardId);

        String url = BASE_URL + "/api/v1/flashcards/" + flashcardId;
        Log.d("FlashcardManager", "API URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FlashcardManager", "API call failed", e);
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("FlashcardManager", "Received API response. Code: " + response.code());

                if (!response.isSuccessful()) {
                    Log.e("FlashcardManager", "API error response: " + response.code());
                    callback.onFailure("Server returned " + response.code());
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    Log.d("FlashcardManager", "API response body: " + jsonData);

                    Gson gson = new Gson();
                    ApiResponseOneFlashcard apiResponse = gson.fromJson(jsonData, ApiResponseOneFlashcard.class);

                    if (apiResponse != null && apiResponse.getData() != null) {
                        Log.d("FlashcardManager", "Successfully parsed flashcard data");
                        callback.onSuccess(apiResponse);
                    } else {
                        Log.e("FlashcardManager", "API response parsing error: response or data is null");
                        callback.onFailure("Invalid response format");
                    }
                } catch (Exception e) {
                    Log.e("FlashcardManager", "Error parsing API response", e);
                    callback.onFailure("Error parsing response: " + e.getMessage());
                }
            }
        });
    }

    public void fetchWordDefinition(String word, AddFlashCardApiCallback<WordData> callback) {
        String url = BASE_URL + "/api/v1/dictionary/" + word;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    //String responseData = new String(response.body().bytes(), StandardCharsets.UTF_8);
                    Log.d("API Response", responseData);

                    // Phân tích JSON
                    JsonObject jsonObject = new Gson().fromJson(responseData, JsonObject.class);
                    JsonArray dataArray = jsonObject.getAsJsonArray("data");

                    if (dataArray != null && dataArray.size() > 0) {
                        WordData wordData = new Gson().fromJson(dataArray.get(0), WordData.class);
                        callback.onSuccess(wordData);
                    } else {
                        callback.onFailure("No data found");
                    }
                } else {
                    callback.onFailure("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Error: " + e.getMessage());
            }
        });
    }

    // Phương thức để dịch nghĩa
    public void translateDefinition(String definition, AddFlashCardApiCallback<String> callback) throws UnsupportedEncodingException {
        try {
            if (definition == null || definition.trim().isEmpty()) {
                callback.onFailure("Definition is null or empty");
                return;
            }

            String sanitizedDefinition = definition
                    .replace(";", " ") // Thay dấu chấm phẩy bằng khoảng trắng
                    .replaceAll("[\\n\\r\\t]", " ") // Thay ký tự xuống dòng, tab bằng khoảng trắng
                    .trim(); // Xóa khoảng trắng thừa

            // Mã hóa chuỗi để gửi qua URL
            String encodedText = URLEncoder.encode(sanitizedDefinition, StandardCharsets.UTF_8.toString());
            String url = BASE_URL + "/api/v1/dictionary/translate/vi/" + encodedText;

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            Log.d("API_RESPONSE", "Response JSON: " + responseBody);

                            String vietnameseMeaning = parseJson(responseBody);

                            vietnameseMeaning = Html.fromHtml(vietnameseMeaning).toString();

                            vietnameseMeaning = vietnameseMeaning
                                    .replaceAll("\\+\\+", ",") // Thay "++" bằng dấu phẩy
                                    .replaceAll("\\+\\s*", " ") // Thay "+" còn lại bằng khoảng trắng
                                    .replaceAll("[\\n\\r\\t]", " ") // Thay ký tự xuống dòng, tab bằng khoảng trắng
                                    .replaceAll("\\s+", " ") // Thay nhiều khoảng trắng bằng một khoảng trắng
                                    .trim(); // Xóa khoảng trắng thừa

                            if (vietnameseMeaning.contains("hoặc") && !vietnameseMeaning.contains("hoặc một")) {
                                vietnameseMeaning = vietnameseMeaning.replace("hoặc", "hoặc một");
                            }

                            callback.onSuccess(vietnameseMeaning);
                        } else {
                            callback.onFailure("Error: " + response.code() + " - " + response.message());
                        }
                    } catch (IOException e) {
                        callback.onFailure("Response parsing error: " + e.getMessage());
                    } finally {
                        response.close(); // Đóng Response để tránh rò rỉ bộ nhớ
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            callback.onFailure("Encoding error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void createFlashcard(Context context,String word, List<Integer> definitionIndices, int partOfSpeechIndex, int userId, AddFlashCardApiCallback<String> callback) {
        String url = BASE_URL + "/api/v1/flashcards";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        // Chuyển đổi danh sách definitionIndices thành chuỗi JSON
        Gson gson = new Gson();
        String definitionIndicesJson = gson.toJson(definitionIndices);

        // Tạo JSON body
        String jsonBody = "{"
                + "\"word\":\"" + word + "\","
                + "\"definitionIndices\":" + definitionIndicesJson + ","
                + "\"partOfSpeechIndex\":" + partOfSpeechIndex + ","
                + "\"userId\":\"" + userId + "\""
                + "}";
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                byte[] responseBytes = response.body().bytes();
                String responseBody = new String(responseBytes, StandardCharsets.UTF_8);
                Log.d("DEBUG", "Raw bytes: " + Arrays.toString(responseBytes)); // Kiểm tra byte gốc
                Log.d("DEBUG", "UTF-8 Decoded: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        JSONObject responseJson = new JSONObject(responseBody);

                        // ✅ 4. Kiểm tra "data" có tồn tại không
                        if (responseJson.has("data")) {
                            JSONObject data = responseJson.getJSONObject("data");
                            String rawPhoneticText = data.optString("phoneticText", ""); // Lấy dữ liệu gốc

                            // Kiểm tra xem dữ liệu có lỗi encoding không
                            Log.d("DEBUG_RAW_PHONETIC", "Raw phonetic: " + rawPhoneticText);
                            SharedPreferences sharedPreferences = context.getSharedPreferences("FlashcardPrefs", Context.MODE_PRIVATE);
                            String existingPhonetic = sharedPreferences.getString("phoneticText" + word, "");
                            Log.d("DEBUG_PHONETIC_SAVE", "Saved phonetic for " + word + ": " + rawPhoneticText);

                            // Chỉ lưu nếu phonetic chưa tồn tại
                            if (!existingPhonetic.contains(rawPhoneticText)) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("phoneticText" + word, rawPhoneticText);
                                editor.apply();
                            }


                            String flashcardID = data.optString("id", ""); // Tránh lỗi nếu "id" không có
                            callback.onSuccess(flashcardID);
                        } else {
                            callback.onFailure("Lỗi: Không tìm thấy 'data' trong phản hồi.");
                        }

                    } catch (JSONException e) {
                        callback.onFailure("Lỗi phân tích JSON: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        });
    }
    public void addFlashcardToGroup(int flashcardId, int groupId, AddFlashCardApiCallback<String> callback) {
        // URL của API với flashcardId và groupId
        String url = BASE_URL + "/api/v1/flashcard-groups/" + flashcardId + "/group/" + groupId;

        // Yêu cầu POST với body rỗng vì API có thể chỉ cần ID trong URL
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", MediaType.parse("application/json; charset=utf-8")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FlashcardManager", "Kết nối thất bại: " + e.getMessage());
                callback.onFailure("Không thể kết nối đến API.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("FlashcardManager", "Phản hồi từ server: " + responseBody);
                if (response.isSuccessful()) {
                    try {
                        JSONObject responseJson = new JSONObject(responseBody);
                        String message = responseJson.optString("message", "Thêm flashcard vào group thành công!");
                        callback.onSuccess(message);
                    } catch (JSONException e) {
                        callback.onFailure("Lỗi xử lý JSON: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Thêm flashcard vào group thất bại! " + response.message());
                }
            }
        });
    }

    public void deleteFlashcardById(int flashcardId, FlashcardApiCallback callback) {
        Log.d("FlashcardManager", "Starting API call to delete flashcard ID: " + flashcardId);

        String url = BASE_URL + "/api/v1/flashcards/" + flashcardId;
        Log.d("FlashcardManager", "API URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .delete() // Sử dụng DELETE request
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FlashcardManager", "API call failed", e);
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("FlashcardManager", "Received API response. Code: " + response.code());

                if (!response.isSuccessful()) {
                    Log.e("FlashcardManager", "API error response: " + response.code());
                    callback.onFailure("Server returned " + response.code());
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    Log.d("FlashcardManager", "API response body: " + jsonData);

                    Gson gson = new Gson();
                    ApiResponseOneFlashcard apiResponse = gson.fromJson(jsonData, ApiResponseOneFlashcard.class);

                    if (apiResponse != null && apiResponse.getData() != null) {
                        Log.d("FlashcardManager", "Successfully deleted flashcard");
                        callback.onSuccess(apiResponse);
                    } else {
                        Log.e("FlashcardManager", "API response parsing error: response is null or unsuccessful");
                        callback.onFailure("Invalid response format");
                    }
                } catch (Exception e) {
                    Log.e("FlashcardManager", "Error parsing API response", e);
                    callback.onFailure("Error parsing response: " + e.getMessage());
                }
            }
        });
    }

    public void markFlashcardAsLearned(Context context, int flashcardId, FlashcardApiCallback callback) {
        Log.d("FlashcardManager", "Starting API call to mark as unlearned for flashcard ID: " + flashcardId);

        String url = BASE_URL + "/api/v1/flashcards/" + flashcardId + "/learned";
        Log.d("FlashcardManager", "API URL: " + url);

        // Lấy token từ SharedPreferences
        String accessToken = SharedPreferencesManager.getInstance(context).getAccessToken();
        Log.d("FlashcardManager", "Access Token: " + accessToken);

        if (accessToken == null || accessToken.isEmpty()) {
            Log.e("FlashcardManager", "Access token is missing!");
            callback.onFailure("Access token is missing!");
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(null, new byte[0])) // Không có body
                .addHeader("Authorization", "Bearer " + accessToken) // Thêm token vào request
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FlashcardManager", "API call failed", e);
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string(); // Lấy nội dung phản hồi
                Log.d("FlashcardManager", "Response Code: " + response.code());
                Log.d("FlashcardManager", "Response Body: " + responseBody);

                if (!response.isSuccessful()) {
                    callback.onFailure("Server returned " + response.code() + ": " + responseBody);
                    return;
                }

                try {
                    // Phân tích JSON phản hồi
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    int statusCode = jsonResponse.getInt("statusCode");
                    String message = jsonResponse.getString("message");

                    if (statusCode == 200) {
                        callback.onSuccess(message); // Thành công
                    } else {
                        callback.onFailure("Unexpected response: " + message);
                    }
                } catch (JSONException e) {
                    Log.e("FlashcardManager", "Error parsing JSON response", e);
                    callback.onFailure("Error parsing response: " + e.getMessage());
                }
            }
        });
    }
    public void markFlashcardAsUnlearned(Context context, int flashcardId, FlashcardApiCallback callback) {
        Log.d("FlashcardManager", "Starting API call to mark as unlearned for flashcard ID: " + flashcardId);

        String url = BASE_URL + "/api/v1/flashcards/" + flashcardId + "/unlearned";
        Log.d("FlashcardManager", "API URL: " + url);

        // Lấy token từ SharedPreferences
        String accessToken = SharedPreferencesManager.getInstance(context).getAccessToken();
        Log.d("FlashcardManager", "Access Token: " + accessToken);

        if (accessToken == null || accessToken.isEmpty()) {
            Log.e("FlashcardManager", "Access token is missing!");
            callback.onFailure("Access token is missing!");
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(null, new byte[0])) // Không có body
                .addHeader("Authorization", "Bearer " + accessToken) // Thêm token vào request
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FlashcardManager", "API call failed", e);
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string(); // Lấy nội dung phản hồi
                Log.d("FlashcardManager", "Response Code: " + response.code());
                Log.d("FlashcardManager", "Response Body: " + responseBody);

                if (!response.isSuccessful()) {
                    callback.onFailure("Server returned " + response.code() + ": " + responseBody);
                    return;
                }

                try {
                    // Phân tích JSON phản hồi
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    int statusCode = jsonResponse.getInt("statusCode");
                    String message = jsonResponse.getString("message");

                    if (statusCode == 200) {
                        callback.onSuccess(message); // Thành công
                    } else {
                        callback.onFailure("Unexpected response: " + message);
                    }
                } catch (JSONException e) {
                    Log.e("FlashcardManager", "Error parsing JSON response", e);
                    callback.onFailure("Error parsing response: " + e.getMessage());
                }
            }
        });
    }

    public void translateToEnglish(String vietnameseWord, AddFlashCardApiCallback<String> callback) {
        String url = BASE_URL + "/api/v1/dictionary/translate/en/" + vietnameseWord;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d("API_RESPONSE", "Response JSON: " + responseBody);

                        JSONObject jsonObject = new JSONObject(responseBody);
                        if (jsonObject.has("data")) {
                            String translatedWord = jsonObject.getString("data").trim();
                            callback.onSuccess(translatedWord);
                        } else {
                            callback.onFailure("Invalid response format");
                        }
                    } else {
                        callback.onFailure("Error: " + response.code() + " - " + response.message());
                    }
                } catch (IOException | JSONException e) {
                    callback.onFailure("Parsing error: " + e.getMessage());
                } finally {
                    response.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        });
    }

    // Phương thức phân tích JSON
    private String parseJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString("data");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface TranslateCallback {
        void onSuccess(String vietnameseMeaning);

        void onFailure(String errorMessage);
    }
}