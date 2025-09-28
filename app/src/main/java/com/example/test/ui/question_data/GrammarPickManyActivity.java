package com.example.test.ui.question_data;

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
import com.example.test.model.Question;
import com.example.test.model.QuestionChoice;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class GrammarPickManyActivity extends AppCompatActivity {
    String correctAnswers;
    private List<String> userAnswers = new ArrayList<>();
    private int totalSteps;
    private int answerIds;// Danh sách questionIds
    private TextView tvContent;
    private  String questype;
    private RecyclerView recyclerViewChoices;
    private LinearLayout progressBar;
    private ImageView  imgLessonMaterial;
    QuestionManager quesManager = new QuestionManager(this);
    LearningMaterialsManager materialsManager = new LearningMaterialsManager(this);
    LessonManager lesManager = new LessonManager();
    private int lessonID,courseID,enrollmentId;
    ResultManager resultManager = new ResultManager(this);
    private List<Question> questions; // Danh sách câu hỏi
    private int currentQuestionIndex; // Vị trí câu hỏi hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_pick_many);

        recyclerViewChoices = findViewById(R.id.recyclerViewChoices);
        recyclerViewChoices.setLayoutManager(new LinearLayoutManager(this));
        imgLessonMaterial.findViewById(R.id.imgLessonMaterial);
        tvContent = findViewById(R.id.tvContent);
        Button btnCheckAnswers = findViewById(R.id.btnCheckAnswers);
        progressBar = findViewById(R.id.progressBar);
        createProgressBars(totalSteps, currentQuestionIndex); // Cập nhật thanh tiến trình mỗi lần chuyển câu
// Nhận dữ liệu từ Intent
        currentQuestionIndex = getIntent().getIntExtra("currentQuestionIndex", 0);
        questions = (List<Question>) getIntent().getSerializableExtra("questions");
        courseID = getIntent().getIntExtra("courseID",1);
        lessonID = getIntent().getIntExtra("lessonID",1);
        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        Log.d("pickmany","Lesson ID: "+ lessonID + "courseID: "+ courseID);
        totalSteps= questions.size();
        createProgressBars(totalSteps, currentQuestionIndex); // Cập nhật thanh tiến trình mỗi lần chuyển câu

        // Hiển thị câu hỏi hiện tại
        loadQuestion(currentQuestionIndex);
        materialsManager.fetchAndLoadImageByLesId(lessonID, imgLessonMaterial);

//        // Lấy lessonId từ intent hoặc một nguồn khác
//        int lessonId = 1;
//        fetchLessonAndQuestions(lessonId); // Gọi phương thức để lấy bài học và câu hỏi

        btnCheckAnswers.setOnClickListener(v -> {
            if (userAnswers.isEmpty()) {
                Toast.makeText(GrammarPickManyActivity.this, "Vui lòng trả lời câu hỏi!", Toast.LENGTH_SHORT)
                        .show();
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
                quesManager.saveUserAnswer(questions.get(currentQuestionIndex).getId(), answerContent,0,null,enrollmentId, new ApiCallback() {

                    @Override
                    public void onSuccess() {
                        Log.e("GrammarPick1QuestionActivity", "Câu trả lời đã được lưu: " + answerContent);
                        // Hiển thị popup
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(GrammarPickManyActivity.this, questype, answerContent, correctAnswers, null, null, null, () -> {
                                // Callback khi nhấn Next Question trên popup
//                                resetAnswerColors();
                                currentQuestionIndex++;
                                if (currentQuestionIndex < questions.size()) {
                                    Question nextQuestion = questions.get(currentQuestionIndex);
                                    createProgressBars(totalSteps, currentQuestionIndex);
                                    if (nextQuestion.getQuesType().equals("CHOICE")) {
                                        Intent intent = new Intent(GrammarPickManyActivity.this, GrammarPick1QuestionActivity.class);
                                        intent.putExtra("currentQuestionIndex", currentQuestionIndex);
                                        Log.e("pickmany","currentQuestionIndex");
                                        intent.putExtra("questions", (Serializable) questions);
                                        intent.putExtra("courseID",courseID);
                                        intent.putExtra("lessonID",lessonID);
                                        intent.putExtra("enrollmentId", enrollmentId);
                                        startActivity(intent);
                                        finish(); // Đóng Activity hiện tại
                                    } else {
                                        loadQuestion(currentQuestionIndex);
                                    }
                                } else {
                                    finishLesson();
                                }
                            });
                        });
                        resultManager.fetchAnswerPointsByQuesId(questions.get(currentQuestionIndex).getId(), new ApiCallback<Answer>() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onSuccess(Answer answer) {
                                if (answer != null) {
                                    answerIds = answer.getId();
                                    Log.e("GrammarPickManyActivity", "Answer ID từ API: " + answer.getId());
                                    if (answerIds != 0) {
                                        QuestionManager.gradeAnswer(answerIds, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                Log.e("GrammarPickManyActivity", "Lỗi khi chấm điểm: " + e.getMessage());
                                            }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    Log.e("GrammarPickManyActivity", "Chấm điểm thành công cho Answer ID: " + answerIds);
                                                } else {
                                                    Log.e("GrammarPickManyActivity", "Lỗi từ server: " + response.code());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("GrammarPickManyActivity", "Bài học không có câu trl.");
                                    }
                                } else {
                                    Log.e("GrammarPickManyActivity", "Không nhận được câu trả lời từ API.");
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
                        Log.e("GrammarPickManyActivity", errorMessage);
                    }

                });
            }
        });
    }

    private void loadQuestion(int index) {
        if (index < questions.size()) {
            Question question = questions.get(index);

            quesManager.fetchQuestionContentFromApi(question.getId(), new ApiCallback<Question>() {
                @Override
                public void onSuccess(Question question) {
                    if (question != null) {
                        questype = question.getQuesType();
                        materialsManager.fetchAndLoadImage(lessonID, imgLessonMaterial);
                        String questionContent = question.getQuesContent();
                        Log.d("GrammarPickManyActivity", "Câu hỏi: " + questionContent);

                        List<QuestionChoice> choices = question.getQuestionChoices();
                        if (choices != null && !choices.isEmpty()) {
                            runOnUiThread(() -> {
                                tvContent.setText(questionContent);
                                userAnswers.clear();
                                MultipleAdapter choiceAdapter = new MultipleAdapter(GrammarPickManyActivity.this, choices, userAnswers);
                                recyclerViewChoices.setAdapter(choiceAdapter);
                                for (QuestionChoice choice : choices) {
                                    if (choice.isChoiceKey()) {
                                        correctAnswers=(choice.getChoiceContent());
                                    }
                                }
                            });
                        } else {
                            Log.e("GrammarPickManyActivity", "Câu hỏi không có lựa chọn.");
                        }
                    } else {
                        Log.e("GrammarPickManyActivity", "Câu hỏi trả về là null.");
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("GrammarPick1QuestionActivity", errorMessage);
                }

                @Override
                public void onSuccess() {}
            });
        } else {
            finishLesson();
        }
    }

    private void finishLesson() {
        Intent intent = new Intent(GrammarPickManyActivity.this, PointResultLessonActivity.class);
        intent.putExtra("lessonId",lessonID);
        intent.putExtra("courseId",courseID);
        startActivity(intent);
        finish();
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