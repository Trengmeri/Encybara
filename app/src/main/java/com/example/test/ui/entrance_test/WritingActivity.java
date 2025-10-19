package com.example.test.ui.entrance_test;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.BaseActivity;
import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.ApiService;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.model.EvaluationResult;
import com.example.test.model.Lesson;
import com.example.test.model.Question;
import com.example.test.ui.question_data.PointResultCourseActivity;

import java.util.List;

public class WritingActivity extends BaseActivity {

    private TextView tvContent, key;
    private EditText etAnswer;
    private Button btnCheckAnswers;
    private QuestionManager quesManager;
    private LessonManager lessonManager = new LessonManager();
    private  String questype;
    int lessonId = 5;
    private boolean isImageVisible = true;
    private List<Integer> questionIds;
    private int currentStep = 0;
    private int totalSteps;
    private ApiService apiService = new ApiService(this);
    private ProgressDialog progressDialog;
//    public static final String EXTRA_MODE = "mode";
//    public static final int MODE_TEST = 1; // Test đầu vào
//   // public static final int MODE_COURSE = 2; // Khóa học bình thường


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writting);

        tvContent = findViewById(R.id.tvContent);
        key = findViewById(R.id.key);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheckAnswers = findViewById(R.id.btnCheckAnswers);
        quesManager = new QuestionManager(this);
        int enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        createProgressBars(totalSteps, currentStep); // Cập nhật thanh tiến trình mỗi lần chuyển câu
        anHienAnh();

        fetchLessonAndQuestions(lessonId);

        btnCheckAnswers.setOnClickListener(view -> {
            String userAnswer = etAnswer.getText().toString().trim();

            if (userAnswer.isEmpty()) {
                Toast.makeText(this, "Please enter an answer!", Toast.LENGTH_SHORT).show();
            } else {
                checkAnswer(userAnswer,enrollmentId);
            }
        });
    }

    private void fetchLessonAndQuestions(int lessonId) {
        lessonManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(Lesson lesson) {
                if (lesson != null) {
                    questionIds = lesson.getQuestionIds();
                    if (questionIds == null) {
                        Log.e("WritingActivity", "Lỗi: lesson.getQuestionIds() trả về null!");
                        return;
                    }
                    Log.d("WritingActivity", "Số lượng câu hỏi: " + questionIds.size()); // Kiểm tra số lượng câu hỏi

                    if (questionIds.isEmpty()) {
                        Log.e("WritingActivity", "Danh sách câu hỏi rỗng!");
                        return;
                    }
                    runOnUiThread(() -> {
                        totalSteps = questionIds.size(); // Cập nhật tổng số câu hỏi thực tế từ API
                        createProgressBars(totalSteps, currentStep); // Tạo progress bar dựa trên số câu hỏi thực tế
                    });

                    if (questionIds != null && !questionIds.isEmpty()) {
                        fetchQuestion(questionIds.get(currentStep));
                    }
                }
            }
            @Override
            public void onFailure(String errorMessage) {}
        });
    }

    private void fetchQuestion(int questionId) {
        quesManager.fetchQuestionContentFromApi(questionId, new ApiCallback<Question>() {
            @Override
            public void onSuccess(Question question) {
                if (question != null) {
                    questype = question.getQuesType();
                    String questionContent = question.getQuesContent();
                    Log.d("WritingActivity", "Câu hỏi: " + questionContent);
                    runOnUiThread(() -> tvContent.setText(questionContent));
                } else {
                    Log.e("WritingActivity", "Không tìm thấy câu hỏi.");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("WritingActivity", errorMessage);
            }

            @Override
            public void onSuccess() {}
        });
    }

    private void checkAnswer(String userAnswer, int enrollmentId) {
        String questionContent = tvContent.getText().toString().trim();
        ApiService apiService = new ApiService(this);


        // Hiển thị ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.load));
        progressDialog.setCancelable(false);
        progressDialog.show();

        apiService.sendAnswerToApi(questionContent, userAnswer, new ApiCallback<EvaluationResult>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(EvaluationResult result) {

                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Lưu kết quả vào hệ thống
                quesManager.saveUserAnswer(questionIds.get(currentStep), userAnswer, result.getPoint(), result.getimprovements(),enrollmentId,new ApiCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("WritingActivity.this", "Lưu thành công!");
                        Log.d("improve","improve:"+ result.getimprovements());
                        editor.putString("improvement_suggestion", result.getimprovements());
                        editor.apply(); // Lưu thay đổi

                        progressDialog.dismiss();
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(WritingActivity.this, "WRITING", null, null, result.getPoint(), result.getimprovements(), result.getevaluation(), () -> {
                                etAnswer.setText("");
                                key.setText("");
                                currentStep++;
                                if (currentStep < totalSteps) {
                                    fetchQuestion(questionIds.get(currentStep));
                                    createProgressBars(totalSteps, currentStep); // Cập nhật thanh tiến trình mỗi lần chuyển câu
                                } else {
                                    Intent intent = new Intent(WritingActivity.this, PointResultCourseActivity.class);
                                    intent.putExtra("status", "test");
                                    intent.putExtra("enrollmentId", enrollmentId);
                                    intent.putExtra("EXTRA_MODE", "MODE_TEST"); // hoặc MODE_COURSE
                                    startActivity(intent);
                                    finish();
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
                        showErrorDialog(getString(R.string.invalidans));
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                progressDialog.dismiss();
                Log.e("WritingActivity", "Câu trả lời khong hop le: " + errorMessage);
                showErrorDialog(getString(R.string.invalidans));
            }
        });
    }
    private void showErrorDialog(String message) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(WritingActivity.this)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        etAnswer.setText("");
                    })
                    .show();
        });
    }
    private void anHienAnh() {
        ImageView imgLessonMaterial = findViewById(R.id.imgLessonMaterial);
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
