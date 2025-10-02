package com.example.test.ui.entrance_test;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.NetworkChangeReceiver;
import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.LearningMaterialsManager;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Answer;
import com.example.test.model.Lesson;
import com.example.test.model.Question;
import com.example.test.model.QuestionChoice;
import com.example.test.ui.question_data.GrammarPick1QuestionActivity;
import com.example.test.ui.question_data.GrammarPickManyActivity;
import com.example.test.ui.question_data.PointResultCourseActivity;
import com.example.test.ui.question_data.PointResultLessonActivity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TextReadingActivity extends AppCompatActivity {
    String correctAnswers;
    private List<String> userAnswers = new ArrayList<>();
    private int currentStep =0;
    private int totalSteps; // Tổng số bước trong thanh tiến trình
    private List<Integer> questionIds;
    private Button btnCheckAnswer;
    private ImageView imgLessonMaterial;
    QuestionManager quesManager = new QuestionManager(this);
    LessonManager lesManager = new LessonManager();
    ResultManager resultManager = new ResultManager(this);
    TextView tvContent;
    private EditText etAnswer;
    LinearLayout progressBar;
    private int lessonID,courseID,enrollmentId;
    NetworkChangeReceiver networkReceiver;
    LearningMaterialsManager materialsManager = new LearningMaterialsManager(this);
    private int answerIds;
    private  String questype;
    private List<Question> questions; // Danh sách câu hỏi
    private int currentQuestionIndex; // Vị trí câu hỏi hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_text);

        // Ánh xạ các thành phần UI
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        tvContent = findViewById(R.id.tvContent);
        etAnswer = findViewById(R.id.etAnswer);
         progressBar = findViewById(R.id.progressBar);
         imgLessonMaterial = findViewById(R.id.imgLessonMaterial);
        createProgressBars(totalSteps, currentStep); // Cập nhật thanh tiến trình mỗi lần chuyển câu
        networkReceiver = new NetworkChangeReceiver();

        // Lấy lessonId từ intent hoặc một nguồn khác
        int lessonId = 1;
        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        Log.d("TextReadingActivity", String.valueOf(enrollmentId));
        fetchLessonAndQuestions(lessonId); // Gọi phương thức để lấy bài học và câu hỏi

        btnCheckAnswer.setOnClickListener(v -> {
            String userAnswer = etAnswer.getText().toString().trim();

            Log.d("TextReadingActivity", "User Answers: " + userAnswer);
            if (userAnswer.isEmpty()) {
                Toast.makeText(TextReadingActivity.this, "Vui lòng trả lời câu hỏi!", Toast.LENGTH_SHORT).show();
            } else {
                String answerContent = userAnswer;
                // Lưu câu trả lời của người dùng
                quesManager.saveUserAnswer(questionIds.get(currentStep), answerContent, 0, null, enrollmentId, new ApiCallback() {
                    @Override
                    public void onSuccess() {
                        Log.e("TextReadingActivity", "Câu trả lời đã được lưu: " + answerContent);
                        // Hiển thị popup
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(TextReadingActivity.this, questype, userAnswer, correctAnswers, null, null, null, () -> {
                                currentStep++; // Tăng currentStep
                                etAnswer.setText("");
                                // Kiểm tra nếu hoàn thành
                                if (currentStep < totalSteps) {
                                    fetchQuestion(questionIds.get(currentStep)); // Lấy câu hỏi tiếp theo
                                    createProgressBars(totalSteps, currentStep); // Cập nhật thanh tiến trình mỗi lần chuyển câu
                                } else {
                                    Intent intent = new Intent(TextReadingActivity.this, Pick1Activity.class);
                                    intent.putExtra("status", "test");
                                    intent.putExtra("enrollmentId", enrollmentId);
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
                                    Log.e("TextReadingActivity", "Answer ID từ API: " + answer.getId());
                                    if (answerIds != 0) {
                                        QuestionManager.gradeAnswer(answerIds, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                Log.e("TextReadingActivity", "Lỗi khi chấm điểm: " + e.getMessage());
                                            }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    Log.e("TextReadingActivity", "Chấm điểm thành công cho Answer ID: " + answerIds);
                                                } else {
                                                    Log.e("TextReadingActivity", "Lỗi từ server: " + response.code());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("TextReadingActivity", "Bài học không có câu trl.");
                                    }
                                } else {
                                    Log.e("TextReadingActivity", "Không nhận được câu trả lời từ API.");
                                }
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Log.e("TextReadingActivity", errorMessage);
                            }
                        });
                    }

                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e("TextReadingActivity", errorMessage);
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
                    // Lấy danh sách questionIds từ lesson
                    questionIds = lesson.getQuestionIds(); // Lưu trữ danh sách questionIds
                    runOnUiThread(() -> {
                        totalSteps = questionIds.size(); // Cập nhật tổng số câu hỏi thực tế từ API
                        createProgressBars(totalSteps, currentStep); // Tạo progress bar dựa trên số câu hỏi thực tế
                    });

                    if (questionIds != null && !questionIds.isEmpty()) {
                        materialsManager.fetchAndLoadImageByLesId(lessonId, imgLessonMaterial);
                        fetchQuestion(questionIds.get(currentStep));
                    } else {
                        Log.e("Pick1Activity", "Bài học không có câu hỏi.");
                    }
                } else {
                    Log.e("Pick1Activity", "Bài học trả về là null.");
                }
            }



            @Override
            public void onFailure(String errorMessage) {
                Log.e("Pick1Activity", errorMessage);
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
                    // Lấy nội dung câu hỏi
                    questype = question.getQuesType();
                    materialsManager.fetchAndLoadImage(questionId, imgLessonMaterial);
                    String questionContent = question.getQuesContent();
                    runOnUiThread(() -> tvContent.setText(questionContent));
                    List<QuestionChoice> choices = question.getQuestionChoices();
                    for (QuestionChoice choice : choices) {
                        if (choice.isChoiceKey()) {
                            correctAnswers= choice.getChoiceContent();
                        }
                    }
                } else {
                    Log.e("TextReadingActivity", "Câu hỏi trả về là null.");
                }
            }



            @Override
            public void onFailure(String errorMessage) {
                Log.e("Pick1Activity", errorMessage);
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
                bar.setBackgroundColor(Color.parseColor("#436EEE")); // Màu đã hoàn thành
            } else {
                bar.setBackgroundColor(Color.parseColor("#E0E0E0")); // Màu chưa hoàn thành
            }
            progressContainer.addView(bar);
        }
    }
}