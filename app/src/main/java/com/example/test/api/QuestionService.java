package com.example.test.api;

import android.content.Context;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.example.test.response.ApiResponseCourse;
import com.google.gson.Gson;
import com.example.test.response.QuestionDetailRespone;
import com.example.test.response.LessonDetailRespone;
import com.example.test.response.ApiResponeGameCourse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuestionService {

    private static final String base_URL = "http://18.136.223.96:8080/api/v1";
    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executorService;
    private final Context context;

    // Hằng số cho loại câu hỏi trắc nghiệm
    private static final String REQUIRED_QUES_TYPE = "CHOICE";

    public QuestionService(Context context) {
        this.context = context.getApplicationContext();
        // ✅ Thêm Interceptor để tự động đính kèm token vào mọi yêu cầu
        client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();

                    String currentAuthToken = SharedPreferencesManager.getInstance(this.context).getAccessToken();

                    if (currentAuthToken != null && !currentAuthToken.isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + currentAuthToken);
                    }

                    requestBuilder.method(original.method(), original.body());
                    return chain.proceed(requestBuilder.build());
                })
                .build();
        gson = new Gson();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public interface QuestionFetchCallback {
        void onSuccess(List<QuestionDetailRespone.QuestionDetail> questions);
        void onError(String message);
    }

    public void getRandomReviewQuestionsForCourse(int courseId, int numberOfQuestions, QuestionFetchCallback callback) {
        executorService.execute(() -> {
            try {
                // 1. Lấy thông tin khóa học để có danh sách các Lesson ID (Giữ nguyên)
                Request courseRequest = new Request.Builder()
                        .url(base_URL + "/game/course/" + courseId)
                        .build();
                Response courseResponse = client.newCall(courseRequest).execute();
                if (!courseResponse.isSuccessful()) {
                    throw new IOException("Unexpected code " + courseResponse);
                }
                String courseJson = courseResponse.body().string();
                ApiResponeGameCourse courseDataWrapper = gson.fromJson(courseJson, ApiResponeGameCourse.class);

                if (courseDataWrapper.getStatusCode() != 200 || courseDataWrapper.getData() == null || courseDataWrapper.getData().isEmpty()) {
                    callback.onError("Lỗi: Không thể lấy thông tin khóa học hoặc không tìm thấy khóa học.");
                    return;
                }

                ApiResponeGameCourse.Course courseData = courseDataWrapper.getData().get(0).getCourse();
                if (courseData == null || courseData.getLessons() == null || courseData.getLessons().isEmpty()) {
                    callback.onError("Khóa học không có bài học nào.");
                    return;
                }

                //Set<Integer> allQuestionIds = new HashSet<>();
                final Map<Integer, Integer> questionLessonMap = new ConcurrentHashMap<>();
                // 2. Lặp qua từng bài học để lấy tất cả các Question ID (Giữ nguyên)
                List<Future<?>> lessonFutures = new ArrayList<>();
                for (ApiResponeGameCourse.LessonSummary lessonSummary : courseData.getLessons()) {
                    lessonFutures.add(executorService.submit(() -> {
                        try {
                            int lessonId = lessonSummary.getId();
                            Request lessonRequest = new Request.Builder()
                                    .url(base_URL + "/lessons/" + lessonId)
                                    .build();
                            Response lessonResponse = client.newCall(lessonRequest).execute();
                            if (!lessonResponse.isSuccessful()) {
                                Log.w("QuestionService", "Cảnh báo: Không thể lấy Lesson Detail cho ID: " + lessonId + ", Code: " + lessonResponse.code());
                                return;
                            }
                            String lessonJson = lessonResponse.body().string();
                            LessonDetailRespone lessonDetailWrapper = gson.fromJson(lessonJson, LessonDetailRespone.class);

                            if (lessonDetailWrapper.getStatusCode() == 200 && lessonDetailWrapper.getData() != null && lessonDetailWrapper.getData().getQuestionIds() != null) {
                                // SỬA ĐỔI: Thay vì synchronized (allQuestionIds), chúng ta lặp qua danh sách ID
                                List<Integer> questionIds = lessonDetailWrapper.getData().getQuestionIds();
                                for (int questionId : questionIds) {
                                    // ✅ LƯU TRỮ KEY (Question ID) và VALUE (Lesson ID)
                                    questionLessonMap.put(questionId, lessonId);
                                }
                            } else {
                                Log.w("QuestionService", "Cảnh báo: Không thể lấy Question IDs cho bài học ID: " + lessonId);
                            }
                        } catch (IOException e) {
                            Log.e("QuestionService", "Lỗi khi gọi API Lesson Detail: " + e.getMessage());
                        }
                    }));
                }

                // Đợi tất cả các tác vụ lấy lesson hoàn thành
                for (Future<?> future : lessonFutures) {
                    future.get();
                }


                if (questionLessonMap.isEmpty()) { // ✅ Dùng map để kiểm tra
                    callback.onError("Không tìm thấy Question ID nào trong các bài học của khóa học này.");
                    return;
                }

                // 3. Chọn ngẫu nhiên các Question ID (Giữ nguyên)
                List<Integer> uniqueShuffledQuestionIds = new ArrayList<>(questionLessonMap.keySet());
                Collections.shuffle(uniqueShuffledQuestionIds);

                // Chọn một số lượng câu hỏi ngẫu nhiên lớn hơn numberOfQuestions
                // để tăng khả năng tìm thấy đủ câu hỏi CHOICE sau khi lọc.
                int fetchLimit = Math.min(numberOfQuestions * 3, uniqueShuffledQuestionIds.size());
                List<Integer> idsToFetch = uniqueShuffledQuestionIds.subList(0, fetchLimit);


                List<QuestionDetailRespone.QuestionDetail> finalQuestions = new ArrayList<>();
                List<Future<?>> questionFutures = new ArrayList<>();

                // 4. Lấy nội dung đầy đủ của các câu hỏi và áp dụng bộ lọc
                for (int questionId : idsToFetch) {
                    questionFutures.add(executorService.submit(() -> {
                        try {
                            Request questionRequest = new Request.Builder()
                                    .url(base_URL + "/questions/" + questionId)
                                    .build();
                            Response questionResponse = client.newCall(questionRequest).execute();
                            if (!questionResponse.isSuccessful()) {
                                Log.w("QuestionService", "Cảnh báo: Không thể lấy Question Detail cho ID: " + questionId + ", Code: " + questionResponse.code());
                                return;
                            }
                            String questionJson = questionResponse.body().string();
                            QuestionDetailRespone questionDetailWrapper = gson.fromJson(questionJson, QuestionDetailRespone.class);

                            if (questionDetailWrapper.getStatusCode() == 200 && questionDetailWrapper.getData() != null) {
                                QuestionDetailRespone.QuestionDetail questionDetail = questionDetailWrapper.getData();
                                Integer lessonId = questionLessonMap.get(questionId);
                                if (lessonId != null) {
                                    // Cần đảm bảo setter này đã có trong QuestionDetailRespone.QuestionDetail
                                    questionDetail.setLessonId(lessonId);
                                }
                                // ✅ BỘ LỌC CHỈ LẤY QUES_TYPE LÀ "CHOICE"
                                if (REQUIRED_QUES_TYPE.equalsIgnoreCase(questionDetail.getQuesType())) {
                                    synchronized (finalQuestions) {
                                        finalQuestions.add(questionDetail);
                                    }
                                } else {
                                    // Log nếu câu hỏi bị loại bỏ
                                    Log.d("QuestionService", "Đã loại bỏ câu hỏi ID: " + questionId + " vì quesType là: " + questionDetail.getQuesType());
                                }

                            } else {
                                Log.w("QuestionService", "Cảnh báo: Không thể lấy nội dung cho Question ID: " + questionId);
                            }
                        } catch (IOException e) {
                            Log.e("QuestionService", "Lỗi khi gọi API Question Detail: " + e.getMessage());
                        }
                    }));
                }

                // Đợi tất cả các tác vụ lấy question hoàn thành
                for (Future<?> future : questionFutures) {
                    future.get();
                }

                // 5. Cắt danh sách cuối cùng theo số lượng yêu cầu (numberOfQuestions)
                List<QuestionDetailRespone.QuestionDetail> limitedQuestions;
                if (finalQuestions.size() > numberOfQuestions) {
                    // Nếu sau khi lọc có nhiều hơn số lượng yêu cầu, cắt bớt
                    limitedQuestions = finalQuestions.subList(0, numberOfQuestions);
                } else {
                    // Nếu có ít hơn hoặc bằng số lượng yêu cầu
                    limitedQuestions = finalQuestions;
                }


                if (limitedQuestions.isEmpty()) {
                    callback.onError("Không tìm thấy câu hỏi trắc nghiệm (" + REQUIRED_QUES_TYPE + ") nào trong khóa học này.");
                } else {
                    // Chuyển kết quả về luồng chính để cập nhật UI
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> callback.onSuccess(limitedQuestions));
                }

            } catch (Exception e) {
                Log.e("QuestionService", "Lỗi tổng thể trong quá trình lấy câu hỏi ôn tập:", e);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError("Lỗi hệ thống: " + e.getMessage()));
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}