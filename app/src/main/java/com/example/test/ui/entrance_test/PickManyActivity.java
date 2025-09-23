package com.example.test.ui.entrance_test;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.adapter.MultipleAdapter;
import com.example.test.api.ApiCallback;
import com.example.test.api.LearningMaterialsManager;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Answer;
import com.example.test.model.Lesson;
import com.example.test.model.Question;
import com.example.test.model.QuestionChoice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PickManyActivity extends AppCompatActivity {
    String correctAnswers ;
    private List<String> userAnswers = new ArrayList<>();
    private int currentStep = 0;
    private int totalSteps;
    private List<Integer> questionIds;
    private int answerIds;// Danh sách questionIds
    private TextView tvContent;
    private  String questype;
    private ImageView imgLessonMaterial;
    private boolean isImageVisible = true;
    private RecyclerView recyclerViewChoices;
    private LinearLayout progressBar;
    QuestionManager quesManager = new QuestionManager(this);
    LessonManager lesManager = new LessonManager();
    ResultManager resultManager = new ResultManager(this);
    LearningMaterialsManager materialsManager = new LearningMaterialsManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_pick_many);

        recyclerViewChoices = findViewById(R.id.recyclerViewChoices);
        recyclerViewChoices.setLayoutManager(new LinearLayoutManager(this));
        imgLessonMaterial = findViewById(R.id.imgLessonMaterial);
        tvContent = findViewById(R.id.tvContent);
        Button btnCheckAnswers = findViewById(R.id.btnCheckAnswers);
        progressBar = findViewById(R.id.progressBar);

        createProgressBars(totalSteps, currentStep); // Cập nhật thanh tiến trình mỗi lần chuyển câu

        int lessonId = 2;
        fetchLessonAndQuestions(lessonId);

        btnCheckAnswers.setOnClickListener(v -> {
            Log.d("PickManyActivity", "User Answers: " + userAnswers);
            if (userAnswers.isEmpty()) {
                Toast.makeText(PickManyActivity.this, "Vui lòng trả lời câu hỏi!", Toast.LENGTH_SHORT).show();
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < userAnswers.size(); i++) {
                    sb.append(userAnswers.get(i));
                    if (i < userAnswers.size() - 1) {
                        sb.append(", "); // Hoặc ký tự phân cách khác
                    }
                }
                String answerContent = sb.toString();
                // Lưu câu trả lời của người dùng
                quesManager.saveUserAnswer(questionIds.get(currentStep), answerContent,0,null,1, new ApiCallback() {

                    @Override
                    public void onSuccess() {
                        Log.e("PickManyActivity", "Câu trả lời đã được lưu: " + answerContent);
                        // Hiển thị popup
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(PickManyActivity.this, questype, answerContent, correctAnswers, null, null, null, () -> {
                                // Callback khi nhấn Next Question trên popup
                                currentStep++; // Tăng currentStep

                                // Kiểm tra nếu hoàn thành
                                if (currentStep < totalSteps) {
                                    fetchQuestion(questionIds.get(currentStep)); // Lấy câu hỏi tiếp theo
                                    createProgressBars(totalSteps, currentStep); // Cập nhật thanh tiến trình mỗi lần chuyển câu
                                } else {
                                    Intent intent = new Intent(PickManyActivity.this,ListeningActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        });
                        resultManager.fetchAnswerPointsByQuesId(questionIds.get(currentStep), new ApiCallback<Answer>() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onSuccess(Answer answer) {
                                if (answer != null) {
                                    answerIds = answer.getId();
                                    Log.e("PickManyActivity", "Answer ID từ API: " + answer.getId());
                                    if (answerIds != 0) {
                                        QuestionManager.gradeAnswer(answerIds, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                Log.e("PickManyActivity", "Lỗi khi chấm điểm: " + e.getMessage());
                                            }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    Log.e("PickManyActivity", "Chấm điểm thành công cho Answer ID: " + answerIds);
                                                } else {
                                                    Log.e("PickManyActivity", "Lỗi từ server: " + response.code());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("PickManyActivity", "Bài học không có câu trl.");
                                    }
                                } else {
                                    Log.e("PickManyActivity", "Không nhận được câu trả lời từ API.");
                                }
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                            }
                        });
                    }

                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e("PickManyActivity", errorMessage);
                    }

                });
            }
        });
    }

    private void fetchLessonAndQuestions(int lessonId) {
        lesManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
            @Override
            public void onSuccess(Lesson lesson) {
                if (lesson != null) {
                    questionIds = lesson.getQuestionIds();
                    Log.d("PickManyActivity", "Danh sách questionIds: " + questionIds);
                    runOnUiThread(() -> {
                        totalSteps = questionIds.size(); // Cập nhật tổng số câu hỏi thực tế từ API
                        createProgressBars(totalSteps, currentStep); // Tạo progress bar dựa trên số câu hỏi thực tế
                    });
                    if (questionIds != null && !questionIds.isEmpty()) {
                        fetchQuestion(questionIds.get(currentStep));
                    } else {
                        Log.e("PickManyActivity", "Bài học không có câu hỏi.");
                    }
                } else {
                    Log.e("PickManyActivity", "Bài học trả về là null.");
                }
            }


            @Override
            public void onFailure(String errorMessage) {
                Log.e("PickManyActivity", errorMessage);
            }

            @Override
            public void onSuccess() {}

        });
    }

    private void fetchQuestion(int questionId) {
        quesManager.fetchQuestionContentFromApi(questionId, new ApiCallback<Question>() {
            @Override
            public void onSuccess(Question question) {
                if (question != null) {
                    questype = question.getQuesType();
                    String questionContent = question.getQuesContent();
                    Log.d("PickManyActivity", "Câu hỏi: " + questionContent);

                    List<QuestionChoice> choices = question.getQuestionChoices();
                    if (choices != null && !choices.isEmpty()) {
                        runOnUiThread(() -> {
                            tvContent.setText(questionContent);
                            materialsManager.fetchAndLoadImage(questionId, imgLessonMaterial);
                            userAnswers.clear();
                            MultipleAdapter choiceAdapter = new MultipleAdapter(PickManyActivity.this, choices, userAnswers);
                            recyclerViewChoices.setAdapter(choiceAdapter);
                            for (QuestionChoice choice : choices) {
                                if (choice.isChoiceKey()) {
                                    correctAnswers=(choice.getChoiceContent());
                                }
                            }
                        });
                    } else {
                        Log.e("PickManyActivity", "Câu hỏi không có lựa chọn.");
                    }
                } else {
                    Log.e("PickManyActivity", "Câu hỏi trả về là null.");
                }
            }
            @Override
            public void onFailure(String errorMessage) {
                Log.e("PickManyActivity", errorMessage);
            }

            @Override
            public void onSuccess() {}
        });
    }

    private void createProgressBars(int totalQuestions, int currentProgress) {
        LinearLayout progressContainer = findViewById(R.id.progressContainer);
        progressContainer.removeAllViews(); // Xóa thanh cũ nếu có

        for (int i = 0; i < totalQuestions; i++) {
            View bar = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(32, 8); // Kích thước mỗi thanh
            params.setMargins(4, 4, 4, 4); // Khoảng cách giữa các thanh
            bar.setLayoutParams(params);

            if (i < currentProgress) {
                bar.setBackgroundColor(Color.parseColor("#C4865E")); // Màu đã hoàn thành
            } else {
                bar.setBackgroundColor(Color.parseColor("#E0E0E0")); // Màu chưa hoàn thành
            }
            progressContainer.addView(bar);
        }
    }
}