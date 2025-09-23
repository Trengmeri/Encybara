package com.example.test.api;

import android.content.Context;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.example.test.model.EnglishLevel;
import com.example.test.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.test.model.Field;

import org.json.JSONException;
import org.json.JSONObject;

public class FieldManager extends BaseApiManager {
    private final Context context;

    public FieldManager(Context context) {
        this.context = context;
    }

    public void fetchFields(ApiCallback<List<Field>> callback) {
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        String url = BASE_URL + "/api/v1/courses/special-fields";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
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
                    JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                    JsonArray dataArray = jsonObject.getAsJsonArray("data");

                    List<Field> fields = new ArrayList<>();
                    for (int i = 0; i < dataArray.size(); i++) {
                        String fieldName = dataArray.get(i).getAsString();
                        fields.add(new Field(fieldName));
                    }
                    callback.onSuccess(fields);
                } else {
                    callback.onFailure("Request failed: " + response.code());
                }
            }
        });
    }
    public void updateUserField(String field, ApiCallback callback) {
        String userId = SharedPreferencesManager.getInstance(context).getID();
        String token = SharedPreferencesManager.getInstance(context).getAccessToken();
        String url = BASE_URL + "/api/v1/users/" + userId;
        String urlUp = BASE_URL + "/api/v1/users";
        Log.d("FieldManager", "UserID: " + userId);
        Log.d("FieldManager", "Token: " + token);
        Log.d("FieldManager", "Field to update: " + field);

        Request getRequest = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(getRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FieldManager", "Get user failed", e);
                callback.onFailure("Lỗi kết nối: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonResponse = response.body().string();
                Log.d("FieldManager", "Get user response: " + jsonResponse);

                if (response.isSuccessful()) {
                    try {
                        Gson gson = new Gson();
                        JsonObject responseObj = gson.fromJson(jsonResponse, JsonObject.class);
                        Log.d("FieldManager", "Parsed response: " + responseObj);

                        JsonObject requestBody = new JsonObject();
                        requestBody.addProperty("id", Integer.parseInt(userId));
                        // Lấy name từ response trực tiếp
                        if (responseObj.has("name")) {
                            String name = responseObj.get("name").getAsString();
                            requestBody.addProperty("name", name);
                        }
                        requestBody.addProperty("speciField", field);
                        // Lấy phone từ response


                        Log.d("FieldManager", "Update request body: " + requestBody);
                        updateUserData(urlUp, token, requestBody, callback); // Sử dụng URL với userId

                    } catch (Exception e) {
                        Log.e("FieldManager", "Parse error", e);
                        callback.onFailure("Lỗi xử lý dữ liệu: " + e.getMessage());
                    }
                } else {
                    Log.e("FieldManager", "Get user failed: " + response.code());
                    callback.onFailure("Lỗi lấy thông tin user: " + response.code());
                }
            }
        });
    }

    private void updateUserData(String url, String token, JsonObject requestBody, ApiCallback callback) {
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), requestBody.toString());

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Log update request
        Log.d("FieldManager", "Update request URL: " + url);
        Log.d("FieldManager", "Update request body: " + requestBody);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FieldManager", "Update failed", e);
                callback.onFailure("Lỗi kết nối: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("FieldManager", "Update response code: " + response.code());
                Log.d("FieldManager", "Update response body: " + responseBody);

                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Lỗi cập nhật field: " + response.code());
                }
            }
        });
    }
    public void fetchUserById(String userId, ApiCallback callback) {
        // Lấy access token từ SharedPreferencesManager để xác thực
        String accessToken = SharedPreferencesManager.getInstance(context).getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            callback.onFailure("Không tìm thấy access token. Vui lòng đăng nhập lại.");
            return;
        }

        // Tạo request GET với header Authorization
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/users/" + userId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("AuthenticationManager", "Kết nối thất bại: " + e.getMessage());
                callback.onFailure("Kết nối thất bại! Không thể kết nối tới API.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("AuthenticationManager", "Phản hồi từ server: " + responseBody);
                if (response.isSuccessful()) {
                    try {
                        // Phân tích JSON response
                        JSONObject responseJson = new JSONObject(responseBody);
                        // Giả sử response trả về trực tiếp object user
                        int id = responseJson.getInt("id");
                        String email = responseJson.getString("email");
                        String name = responseJson.getString("name");
                        String speciField = responseJson.optString("speciField", "");
                        String phone = responseJson.isNull("phone") ? null : responseJson.getString("phone");
                        String avatar = responseJson.isNull("avatar") ? null : responseJson.getString("avatar");
                        String englishLevel = responseJson.optString("englishlevel", "");

                        // Tạo đối tượng User
                        User user = new User();
                        user.setId(id);
                        user.setEmail(email);
                        user.setName(name);
                        user.setSpeciField(speciField);
                        user.setPhone(phone);
                        user.setAvatar(avatar);
                        user.setEnglishLevel(englishLevel);

                        // Lưu thông tin người dùng vào SharedPreferences
                        SharedPreferencesManager.getInstance(context).saveUser(user);

                        // Trả về responseJson để sử dụng trong SignInActivity
                        callback.onSuccess(responseJson);
                    } catch (JSONException e) {
                        Log.e("AuthenticationManager", "Lỗi phân tích JSON: " + e.getMessage());
                        callback.onFailure("Lỗi phân tích phản hồi JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("AuthenticationManager", "Lỗi từ server: Mã lỗi " + response.code() + ", Nội dung: " + responseBody);
                    callback.onFailure("Không thể lấy thông tin người dùng. Mã lỗi: " + response.code());
                }
            }
        });
    }
    

}