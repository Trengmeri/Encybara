package com.example.test.ui.question_data;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.example.test.api.QuestionManager;
import com.example.test.model.EvaluationResult;
import com.example.test.model.Question;

import java.util.List;

public class WrittingActivity extends AppCompatActivity {

    private TextView tvContent, key;
    private EditText etAnswer;
    private Button btnCheckAnswers;
    private QuestionManager quesManager;
    private String questype;
    private List<Question> questions; // Danh sách câu hỏi
    private int currentQuestionIndex; // BIẾN DUY NHẤT ĐỂ THEO DÕI CÂU HỎI
    private int totalSteps;
    private int lessonID, courseID, enrollmentId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writting);

        tvContent = findViewById(R.id.tvContent);
        key = findViewById(R.id.key);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheckAnswers = findViewById(R.id.btnCheckAnswers);
        quesManager = new QuestionManager(this);

        // Lấy dữ liệu từ Intent
        currentQuestionIndex = getIntent().getIntExtra("currentQuestionIndex", 0);
        questions = (List<Question>) getIntent().getSerializableExtra("questions");
        courseID = getIntent().getIntExtra("courseID", 1);
        lessonID = getIntent().getIntExtra("lessonID", 1);
        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        totalSteps = questions.size();

        // Cập nhật thanh tiến trình và hiển thị câu hỏi
        createProgressBars(totalSteps, currentQuestionIndex);
        loadQuestion(currentQuestionIndex);

        btnCheckAnswers.setOnClickListener(view -> {
            String userAnswer = etAnswer.getText().toString().trim();
            if (userAnswer.isEmpty()) {
                Toast.makeText(this, "Please enter an answer!", Toast.LENGTH_SHORT).show();
            } else {
                checkAnswer(userAnswer, enrollmentId);
            }
        });
    }

    private void loadQuestion(int index) {
        if (index < questions.size()) {
            Question question = questions.get(index);
            quesManager.fetchQuestionContentFromApi(question.getId(), new ApiCallback<Question>() {
                @Override
                public void onSuccess(Question questionData) {
                    if (questionData != null) {
                        questype = questionData.getQuesType();
                        runOnUiThread(() -> tvContent.setText(questionData.getQuesContent()));
                    } else {
                        Log.e("WrittingActivity", "Câu hỏi trả về là null.");
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("WrittingActivity", "Lỗi tải câu hỏi: " + errorMessage);
                }

                @Override
                public void onSuccess() {}
            });
        } else {
            finishLesson();
        }
    }

    private void checkAnswer(String userAnswer, int enrollmentId) {
        String questionContent = tvContent.getText().toString().trim();
        ApiService apiService = new ApiService(this); // Khởi tạo ApiService

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.load));
        progressDialog.setCancelable(false);
        progressDialog.show();

        apiService.sendAnswerToApi(questionContent, userAnswer, new ApiCallback<EvaluationResult>() {
            @Override
            public void onSuccess(EvaluationResult result) {
                // SỬA Ở ĐÂY: Dùng `currentQuestionIndex` thay vì `currentStep`
                quesManager.saveUserAnswer(questions.get(currentQuestionIndex).getId(), userAnswer, result.getPoint(), result.getimprovements(), enrollmentId, new ApiCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("WrittingActivity", "Lưu thành công!");
                        progressDialog.dismiss();
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(WrittingActivity.this, "WRITING", null, null, result.getPoint(), result.getimprovements(), result.getevaluation(), () -> {
                                etAnswer.setText("");
                                key.setText("");
                                currentQuestionIndex++; // Chỉ tăng biến này

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
                    public void onFailure(String errorMessage) {
                        progressDialog.dismiss();
                        Log.e("WritingActivity", "Lỗi lưu câu trả lời: " + errorMessage);
                        showErrorDialog("Lỗi khi lưu câu trả lời. Vui lòng thử lại.");
                    }
                    @Override public void onSuccess(Object result) {}
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                progressDialog.dismiss();
                Log.e("WritingActivity", "API đánh giá thất bại: " + errorMessage);
                showErrorDialog(getString(R.string.invalidans));
            }
            @Override public void onSuccess() {}
        });
    }

    private void finishLesson() {
        Intent intent = new Intent(WrittingActivity.this, PointResultLessonActivity.class);
        intent.putExtra("lessonId", lessonID);
        intent.putExtra("courseId", courseID);
        intent.putExtra("enrollmentId", enrollmentId);
        startActivity(intent);
        finish();
    }

    private void showErrorDialog(String message) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(WrittingActivity.this)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void createProgressBars(int totalQuestions, int currentProgress) {
        LinearLayout progressContainer = findViewById(R.id.progressContainer);
        progressContainer.removeAllViews();

        for (int i = 0; i < totalQuestions; i++) {
            View bar = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(32, 8);
            params.setMargins(4, 4, 4, 4);
            bar.setLayoutParams(params);
            bar.setBackgroundColor(i < currentProgress ? Color.parseColor("#436EEE") : Color.parseColor("#E0E0E0"));
            progressContainer.addView(bar);
        }
    }
}