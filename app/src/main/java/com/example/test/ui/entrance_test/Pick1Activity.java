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
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.NetworkChangeReceiver;
import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.adapter.ChoiceAdapter;
import com.example.test.api.ApiCallback;
import com.example.test.api.LearningMaterialsManager;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Answer;
import com.example.test.model.Course;
import com.example.test.model.Enrollment;
import com.example.test.model.Discussion;
import com.example.test.model.Lesson;
import com.example.test.model.MediaFile;
import com.example.test.model.Question;
import com.example.test.model.QuestionChoice;
import com.example.test.model.Result;
import com.example.test.ui.question_data.GrammarPick1QuestionActivity;
import com.example.test.ui.question_data.PointResultCourseActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Pick1Activity extends AppCompatActivity {
    String correctAnswers ;
    private List<String> userAnswers = new ArrayList<>();
    private int currentStep = 0; // Bước hiện tại (bắt đầu từ 0)
    private  String questype;
    private int totalSteps; // Tổng số bước trong thanh tiến trình
    private AppCompatButton selectedAnswer = null;
    private Button btnCheckAnswer;
    LinearLayout progressBar;
    private ImageView imgLessonMaterial;
    QuestionManager quesManager = new QuestionManager(this);
    LessonManager lesManager = new LessonManager();
    ResultManager resultManager = new ResultManager(this);
    private LearningMaterialsManager materialsManager;
    TextView tvContent;
    NetworkChangeReceiver networkReceiver;
    private List<Integer> questionIds;
    private int answerIds;// Danh sách questionIds
    private RecyclerView recyclerViewChoices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_question);

        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        tvContent = findViewById(R.id.tvContent);
        imgLessonMaterial= findViewById(R.id.imgLessonMaterial);

        progressBar = findViewById(R.id.progressBar); // Ánh xạ ProgressBar

        recyclerViewChoices = findViewById(R.id.recyclerViewChoices);
        int columnCount = 2; // Số cột
        GridLayoutManager layoutManager = new GridLayoutManager(this, columnCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1; // Mỗi button chiếm 1 cột
            }
        });
        recyclerViewChoices.setLayoutManager(layoutManager);
        recyclerViewChoices.setHasFixedSize(true);
        createProgressBars(totalSteps, currentStep); // Cập nhật thanh tiến trình mỗi lần chuyển câu

        networkReceiver = new NetworkChangeReceiver();
        materialsManager = new LearningMaterialsManager(this);



        // Lấy lessonId từ intent hoặc một nguồn khác
        int lessonId = 2;
        int enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        fetchLessonAndQuestions(lessonId); // Gọi phương thức để lấy bài học và câu hỏi

        btnCheckAnswer.setOnClickListener(v -> {
            if (userAnswers.isEmpty()) {
                Toast.makeText(Pick1Activity.this, "Vui lòng trả lời câu hỏi!", Toast.LENGTH_SHORT)
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
                quesManager.saveUserAnswer(questionIds.get(currentStep), answerContent, 0,null,enrollmentId, new ApiCallback() {

                    @Override
                    public void onSuccess() {
                        Log.e("Pick1Activity", "Câu trả lời đã được lưu: " + answerContent);
                        // Hiển thị popup
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(Pick1Activity.this, questype, answerContent, correctAnswers, null, null, null, () -> {

                                currentStep++; // Tăng currentStep

                                // Kiểm tra nếu hoàn thành
                                if (currentStep < totalSteps) {
                                    fetchQuestion(questionIds.get(currentStep)); // Lấy câu hỏi tiếp theo
                                    createProgressBars(totalSteps, currentStep); // Cập nhật thanh tiến trình mỗi lần chuyển câu

                                } else {
                                    Intent intent = new Intent(Pick1Activity.this, ListeningActivity.class);
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
                                    Log.e("Pick1Activity", "Answer ID từ API: " + answer.getId());
                                    if (answerIds != 0) {
                                        QuestionManager.gradeAnswer(answerIds, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                Log.e("Pick1Activity", "Lỗi khi chấm điểm: " + e.getMessage());
                                            }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    Log.e("Pick1Activity", "Chấm điểm thành công cho Answer ID: " + answerIds +"Diem: "+ answer.getPointAchieved());
                                                } else {
                                                    Log.e("Pick1Activity", "Lỗi từ server: " + response.code());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("Pick1Activity", "Bài học không có câu trl.");
                                    }
                                } else {
                                    Log.e("Pick1Activity", "Không nhận được câu trả lời từ API.");
                                }
                            }

                            @Override
                            public void onFailure(String errorMessage) {

                            }
                        });
                    }

                    @Override
                    public void onSuccess(Object result) {}
                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e("Pick1Activity", errorMessage);
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
                        fetchQuestion(questionIds.get(currentStep)); // Lấy câu hỏi đầu tiên
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
                    String questionContent = question.getQuesContent();
                    Log.d("Pick1Activity", "Câu hỏi: " + questionContent);

                    List<QuestionChoice> choices = question.getQuestionChoices();
                    if (choices != null && !choices.isEmpty()) {
                        runOnUiThread(() -> {
                            materialsManager.fetchAndLoadImage(questionId, imgLessonMaterial);
                            tvContent.setText(questionContent);
                            userAnswers.clear();
                            ChoiceAdapter choiceAdapter = new ChoiceAdapter(Pick1Activity.this, choices, userAnswers);
                            recyclerViewChoices.setAdapter(choiceAdapter);
                            for (QuestionChoice choice : choices) {
                                if (choice.isChoiceKey()) {
                                    correctAnswers=choice.getChoiceContent();
                                }
                            }
                        });

                    } else {
                        Log.e("Pick1Activity", "Câu hỏi không có lựa chọn.");
                    }
                } else {
                    Log.e("Pick1Activity", "Câu hỏi trả về là null.");
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