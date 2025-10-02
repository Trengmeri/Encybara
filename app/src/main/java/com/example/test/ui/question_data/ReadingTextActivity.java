package com.example.test.ui.question_data;

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
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.NetworkChangeReceiver;
import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.adapter.ChoiceAdapter;
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
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ReadingTextActivity extends AppCompatActivity {
    String correctAnswers ;
    private List<String> userAnswers = new ArrayList<>();
    private int totalSteps; // Tổng số bước trong thanh tiến trình
    private Button btnCheckAnswer;
    QuestionManager quesManager = new QuestionManager(this);
    LessonManager lesManager = new LessonManager();
    ResultManager resultManager = new ResultManager(this);
    TextView tvContent;
    ImageView imgLessonMaterial;
    LearningMaterialsManager materialsManager = new LearningMaterialsManager(this);
    private EditText etAnswer;
    private int lessonID,courseID,enrollmentId;
    NetworkChangeReceiver networkReceiver;
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
        imgLessonMaterial = findViewById(R.id.imgLessonMaterial);
        LinearLayout progressBar = findViewById(R.id.progressBar);
        createProgressBars(totalSteps, currentQuestionIndex); // Cập nhật thanh tiến trình mỗi lần chuyển câu
        networkReceiver = new NetworkChangeReceiver();

        // Nhận dữ liệu từ Intent
        currentQuestionIndex = getIntent().getIntExtra("currentQuestionIndex", 0);
        questions = (List<Question>) getIntent().getSerializableExtra("questions");
        courseID = getIntent().getIntExtra("courseID",1);
        lessonID = getIntent().getIntExtra("lessonID",1);
        totalSteps= questions.size();
        createProgressBars(totalSteps, currentQuestionIndex);
        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        Log.e("pick1","Lesson ID: "+ lessonID + "courseID: "+ courseID);



        // Hiển thị câu hỏi hiện tại
        loadQuestion(currentQuestionIndex);
        materialsManager.fetchAndLoadImageByLesId(lessonID, imgLessonMaterial);
//        // Lấy lessonId từ intent hoặc một nguồn khác
//        int lessonId = 1;
//        fetchLessonAndQuestions(lessonId); // Gọi phương thức để lấy bài học và câu hỏi

        btnCheckAnswer.setOnClickListener(v -> {
            String userAnswer = etAnswer.getText().toString().trim();
            if (userAnswer.isEmpty()) {
                Toast.makeText(ReadingTextActivity.this, "Vui lòng trả lời câu hỏi!", Toast.LENGTH_SHORT).show();
            } else {
                String answerContent = userAnswer;
                // Lưu câu trả lời của người dùng
                quesManager.saveUserAnswer(questions.get(currentQuestionIndex).getId(), answerContent,0,null,enrollmentId, new ApiCallback() {
                    @Override
                    public void onSuccess() {
                        Log.e("ReadingTextActivity", "Câu trả lời đã được lưu: " + answerContent);
                        // Hiển thị popup
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(ReadingTextActivity.this, questype, answerContent, correctAnswers, null, null, null, () -> {
                                currentQuestionIndex++; // Tăng currentStep
                                etAnswer.setText("");
                                // Kiểm tra nếu hoàn thành
                                if (currentQuestionIndex < questions.size()) {
                                    Question nextQuestion = questions.get(currentQuestionIndex);
                                    createProgressBars(totalSteps, currentQuestionIndex);
                                    if (nextQuestion.getQuesType().equals("CHOICE")) {
                                        Intent intent = new Intent(ReadingTextActivity.this, GrammarPick1QuestionActivity.class);
                                        intent.putExtra("currentQuestionIndex", currentQuestionIndex);
                                        Log.e("pick1", "currentQuestionIndex");
                                        intent.putExtra("questions", (Serializable) questions);
                                        intent.putExtra("courseID", courseID);
                                        intent.putExtra("lessonID", lessonID);
                                        intent.putExtra("enrollmentId", enrollmentId);
                                        startActivity(intent);
                                        finish(); // Đóng Activity hiện tại
                                    } else if (nextQuestion.getQuesType().equals("MULTIPLE")) {
                                        Intent intent = new Intent(ReadingTextActivity.this, GrammarPickManyActivity.class);
                                        intent.putExtra("currentQuestionIndex", currentQuestionIndex);
                                        Log.e("pick1", "currentQuestionIndex");
                                        intent.putExtra("questions", (Serializable) questions);
                                        intent.putExtra("courseID", courseID);
                                        intent.putExtra("lessonID", lessonID);
                                        startActivity(intent);
                                        finish(); // Đóng Activity hiện tại
                                    }
                                    else {
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
                                    Log.e("ReadingTextActivity", "Answer ID từ API: " + answer.getId());
                                    if (answerIds != 0) {
                                        QuestionManager.gradeAnswer(answerIds, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                Log.e("ReadingTextActivity", "Lỗi khi chấm điểm: " + e.getMessage());
                                            }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    Log.e("ReadingTextActivity", "Chấm điểm thành công cho Answer ID: " + answerIds);
                                                } else {
                                                    Log.e("ReadingTextActivity", "Lỗi từ server: " + response.code());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("ReadingTextActivity", "Bài học không có câu trl.");
                                    }
                                } else {
                                    Log.e("ReadingTextActivity", "Không nhận được câu trả lời từ API.");
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

                    }
                });
            }
        });
    }
    private void loadQuestion(int index) {
        if (index < questions.size()) {
            Question question = questions.get(index);
            questype = question.getQuesType();
            quesManager.fetchQuestionContentFromApi(question.getId(), new ApiCallback<Question>() {
                @Override
                public void onSuccess(Question question) {
                    if (question != null) {
                        // Lấy nội dung câu hỏi
                        String questionContent = question.getQuesContent();
                        materialsManager.fetchAndLoadImage(question.getId(), imgLessonMaterial);
                        Log.d("ReadingTextActivity", "Câu hỏi: " + questionContent);

                        List<QuestionChoice> choices = question.getQuestionChoices();
                        if (choices != null && !choices.isEmpty()) {
                            runOnUiThread(() -> {
                                tvContent.setText(questionContent);
                                for (QuestionChoice choice : choices) {
                                    if (choice.isChoiceKey()) {
                                        correctAnswers=(choice.getChoiceContent());
                                    }
                                }
                            });
                        } else {
                            Log.e("ReadingTextActivity", "Câu hỏi không có lựa chọn.");
                        }
                    } else {
                        Log.e("ReadingTextActivity", "Câu hỏi trả về là null.");
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("ReadingTextActivity", errorMessage);
                }

                @Override
                public void onSuccess() {}
            });
        } else {
            finishLesson();
        }
    }

    private void finishLesson() {
        Intent intent = new Intent(ReadingTextActivity.this, PointResultLessonActivity.class);
        intent.putExtra("lessonId",lessonID);
        intent.putExtra("courseId",courseID);
        intent.putExtra("enrollmentId", enrollmentId);
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