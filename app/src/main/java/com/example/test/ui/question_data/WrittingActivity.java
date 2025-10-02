package com.example.test.ui.question_data;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.ApiService;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.model.EvaluationResult;
import com.example.test.model.Lesson;
import com.example.test.model.Question;

import java.util.List;

public class WrittingActivity extends AppCompatActivity {

    private TextView tvContent, key;
    private EditText etAnswer;
    private Button btnCheckAnswers;
    private QuestionManager quesManager;
    private LessonManager lessonManager = new LessonManager();
    private  String questype;
    int lessonId = 5;
    private List<Integer> questionIds;
    private int currentStep = 0;
    private int totalSteps;
    private ApiService apiService = new ApiService(this);
    private List<Question> questions; // Danh sách câu hỏi
    private int currentQuestionIndex;
    private int lessonID,courseID;
    private ProgressDialog progressDialog;
    int enrollmentId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writting);

        tvContent = findViewById(R.id.tvContent);
        key = findViewById(R.id.key);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheckAnswers = findViewById(R.id.btnCheckAnswers);
        quesManager = new QuestionManager(this);
        createProgressBars(totalSteps, currentQuestionIndex);

        currentQuestionIndex = getIntent().getIntExtra("currentQuestionIndex", 0);
        questions = (List<Question>) getIntent().getSerializableExtra("questions");
        courseID = getIntent().getIntExtra("courseID",1);
        lessonID = getIntent().getIntExtra("lessonID",1);
        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        totalSteps= questions.size();
        createProgressBars(totalSteps, currentQuestionIndex);

        // Hiển thị câu hỏi hiện tại
        loadQuestion(currentQuestionIndex);

        btnCheckAnswers.setOnClickListener(view -> {
            String userAnswer = etAnswer.getText().toString().trim();

            if (userAnswer.isEmpty()) {
                Toast.makeText(this, "Please enter an answer!", Toast.LENGTH_SHORT).show();
            } else {
                checkAnswer(userAnswer,enrollmentId);
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
                        runOnUiThread(() -> {
                            tvContent.setText(question.getQuesContent());
                        });
                    } else {
                        Log.e("ListeningQuestionActivity", "Câu hỏi trả về là null.");
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
        Intent intent = new Intent(WrittingActivity.this, PointResultLessonActivity.class);
        intent.putExtra("lessonId",lessonID);
        intent.putExtra("courseId",courseID);
        intent.putExtra("enrollmentId", enrollmentId);
        startActivity(intent);
        finish();
    }

    private void checkAnswer(String userAnswer,int enrollmentId) {
        String questionContent = tvContent.getText().toString().trim();
        ApiService apiService = new ApiService(this);

        // Hiển thị ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.load));
        progressDialog.setCancelable(false);
        progressDialog.show();

        apiService.sendAnswerToApi(questionContent, userAnswer, new ApiCallback<EvaluationResult>() {
            @Override
            public void onSuccess() {}

            @Override
            public void onSuccess(EvaluationResult result) {
                // Lưu kết quả vào hệ thống
                quesManager.saveUserAnswer(questions.get(currentStep).getId(), userAnswer, result.getPoint(), result.getimprovements(),enrollmentId, new ApiCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("WrittingActivity.this", "Lưu thành công!");
                        progressDialog.dismiss();
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(WrittingActivity.this, "WRITING", null, null, result.getPoint(), result.getimprovements(), result.getevaluation(), () -> {
                                etAnswer.setText("");
                                key.setText("");
                                currentStep++; // Tăng currentStep
                                currentQuestionIndex++;

                                if (currentQuestionIndex < questions.size()) {
                                    createProgressBars(totalSteps, currentQuestionIndex);
                                    loadQuestion(currentQuestionIndex);
                                } else {
                                    finishLesson();
                                }
                            });
                        });
                    }

                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        progressDialog.dismiss();
                        Log.e("WritingActivity", "Lỗi lưu câu trả lời: " + errorMessage);
                        showErrorDialog("Lỗi khi lưu câu trả lời. Vui lòng thử lại.");
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                progressDialog.dismiss();
                Log.e("WritingActivity", "Câu trả lời khong hop le: " + errorMessage);
                showErrorDialog(getString(R.string.invalidans));
                apiService.getSuggestionFromApi(questionContent, new ApiCallback<String>(){

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onSuccess(String tip) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                key.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                key.setMovementMethod(new ScrollingMovementMethod());

                                String formattedTip = tip
                                        .replaceAll("(?<!\\d)\\. ", ".\n")
                                        .replaceAll(": ", ":\n");

                                key.setText("Tip: \n" +formattedTip);
                            }
                        });
                    }



                    @Override
                    public void onFailure(String errorMessage) {

                    }
                });
            }
        });
    }
    private void showErrorDialog(String message) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(WrittingActivity.this)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        etAnswer.setText("");
                    })
                    .show();
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
