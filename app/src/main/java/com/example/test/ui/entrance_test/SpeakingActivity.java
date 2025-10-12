package com.example.test.ui.entrance_test;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.ApiService;
import com.example.test.api.AudioManager;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;
import com.example.test.model.EvaluationResult;
import com.example.test.model.Lesson;
import com.example.test.model.Question;
import com.example.test.model.SpeechResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Đổi tên class nếu cần thiết, ví dụ thành RecordQuestionActivity để khớp với file layout
public class SpeakingActivity extends AppCompatActivity {

    private TextView tvQuestion, tvTalk; // tvTalk thay thế cho tvTranscription và key
    private LinearLayout imgVoice;
    private ProgressDialog progressDialog;

    private List<Integer> questionIds;
    private int currentStep = 0;
    private int totalSteps, enrollmentId;

    // Managers
    private QuestionManager quesManager = new QuestionManager(this);
    private LessonManager lesManager = new LessonManager();
    private AudioManager audioManager = new AudioManager(this);

    // Recording
    private MediaRecorder recorder;
    private File recordedFile;
    private boolean isRecording = false;

    // Wave Animations
    private View wave1, wave2, wave3;
    private ObjectAnimator animator1ScaleX, animator1ScaleY, animator1Alpha;
    private ObjectAnimator animator2ScaleX, animator2ScaleY, animator2Alpha;
    private ObjectAnimator animator3ScaleX, animator3ScaleY, animator3Alpha;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo bạn đang sử dụng đúng layout đã cung cấp
        setContentView(R.layout.activity_record_question);

        // Ánh xạ các view từ layout mới
        imgVoice = findViewById(R.id.imgVoice);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTalk = findViewById(R.id.tvTalk); // View mới để hiển thị text
        wave1 = findViewById(R.id.wave_1);
        wave2 = findViewById(R.id.wave_2);
        wave3 = findViewById(R.id.wave_3);

        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        int lessonId = 6; // ID này có thể được truyền qua Intent
        fetchLessonAndQuestions(lessonId);

        setupWaveAnimators();

        // Kiểm tra và yêu cầu quyền ghi âm
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // Thiết lập sự kiện click cho nút ghi âm
        imgVoice.setOnClickListener(v -> {
            if (!isRecording) {
                startRecording();
            } else {
                stopRecording(); // Logic kiểm tra kết quả sẽ được gọi từ đây
            }
        });
    }

    private void startRecording() {
        File outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (outputDir == null) {
            Toast.makeText(this, "Không thể truy cập bộ nhớ", Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = "recorded_audio_" + System.currentTimeMillis() + ".m4a";
        recordedFile = new File(outputDir, fileName);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(recordedFile.getAbsolutePath());

        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            startWaves();
            tvTalk.setText("Đang lắng nghe..."); // Phản hồi cho người dùng
            Log.d("Recording", "Recording started");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Recording", "Recording failed to start");
            Toast.makeText(this, "Không thể bắt đầu ghi âm", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
            } catch (RuntimeException stopException) {
                Log.e("Recording", "Stop failed", stopException);
            } finally {
                recorder = null;
                isRecording = false;
                stopWaves();
                Log.d("Recording", "Recording stopped. File: " + recordedFile.getAbsolutePath());
                // Bắt đầu quá trình chuyển đổi và kiểm tra
                processAudioAndCheckAnswer();
            }
        }
    }

    private void processAudioAndCheckAnswer() {
        if (recordedFile == null || !recordedFile.exists()) {
            Toast.makeText(this, "Không tìm thấy file ghi âm.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Hiển thị dialog chờ xử lý
        runOnUiThread(() -> {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.load));
            progressDialog.setCancelable(false);
            progressDialog.show();
        });

        audioManager.uploadAndTranscribeM4A(recordedFile, new ApiCallback<SpeechResult>() {
            @Override
            public void onSuccess(SpeechResult result) {
                String transcript = result.getTranscript();
                Log.d("SPEECH_TO_TEXT", "Transcript: " + transcript);
                runOnUiThread(() -> tvTalk.setText(transcript)); // Hiển thị kết quả nhận dạng
                // **LOGIC MỚI: Gọi kiểm tra câu trả lời ngay lập tức**
                checkAnswer(transcript);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("SPEECH_TO_TEXT", errorMessage);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                showErrorDialog("Lỗi nhận dạng giọng nói. Vui lòng thử lại.");
            }

            @Override
            public void onSuccess() { }
        });
    }


    private void checkAnswer(String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            showErrorDialog("Không nhận dạng được giọng nói. Vui lòng thử lại.");
            return;
        }

        String questionContent = tvQuestion.getText().toString().trim();
        ApiService apiService = new ApiService(this);

        apiService.sendAnswerToApi(questionContent, userAnswer, new ApiCallback<EvaluationResult>() {
            @Override
            public void onSuccess(EvaluationResult result) {
                // Lưu kết quả
                quesManager.saveUserAnswer(questionIds.get(currentStep), userAnswer, result.getPoint(), result.getimprovements(), enrollmentId, new ApiCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("CheckAnswer", "Lưu câu trả lời thành công!");
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        // Hiển thị popup kết quả và chuyển câu
                        runOnUiThread(() -> showResultAndMoveNext(result));
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e("CheckAnswer", "Lỗi lưu câu trả lời: " + errorMessage);
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        showErrorDialog("Lỗi khi lưu kết quả. Vui lòng thử lại.");
                    }
                    @Override public void onSuccess(Object result) { }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("CheckAnswer", "API đánh giá thất bại: " + errorMessage);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                showErrorDialog(getString(R.string.invalidans));
            }
            @Override public void onSuccess() { }
        });
    }

    private void showResultAndMoveNext(EvaluationResult result) {
        String quesType = "speaking"; // Giả định
        PopupHelper.showResultPopup(SpeakingActivity.this, quesType, null, null, result.getPoint(), result.getimprovements(), result.getevaluation(), () -> {
            tvTalk.setText(""); // Xóa text cũ
            currentStep++;
            if (currentStep < totalSteps) {
                fetchQuestion(questionIds.get(currentStep));
                createProgressBars(totalSteps, currentStep);
            } else {
                // Chuyển sang màn hình tiếp theo, ví dụ WritingActivity hoặc màn hình kết quả
                Toast.makeText(this, "Hoàn thành bài nói!", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(SpeakingActivity.this, WritingActivity.class);
                // intent.putExtra("enrollmentId", enrollmentId);
                // startActivity(intent);
                finish();
            }
        });
    }

    private void showErrorDialog(String message) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(SpeakingActivity.this)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        tvTalk.setText(""); // Xóa text khi có lỗi
                    })
                    .show();
        });
    }

    private void fetchLessonAndQuestions(int lessonId) {
        lesManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
            @Override
            public void onSuccess(Lesson lesson) {
                if (lesson != null && lesson.getQuestionIds() != null && !lesson.getQuestionIds().isEmpty()) {
                    questionIds = lesson.getQuestionIds();
                    totalSteps = questionIds.size();
                    runOnUiThread(() -> createProgressBars(totalSteps, currentStep));
                    fetchQuestion(questionIds.get(currentStep));
                } else {
                    showErrorDialog("Không thể tải câu hỏi cho bài học này.");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                showErrorDialog("Lỗi tải dữ liệu bài học: " + errorMessage);
            }
            @Override public void onSuccess() {}
        });
    }

    private void fetchQuestion(int questionId) {
        quesManager.fetchQuestionContentFromApi(questionId, new ApiCallback<Question>() {
            @Override
            public void onSuccess(Question question) {
                if (question != null) {
                    runOnUiThread(() -> tvQuestion.setText(question.getQuesContent()));
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                showErrorDialog("Lỗi tải câu hỏi: " + errorMessage);
            }
            @Override public void onSuccess() {}
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

    // --- Các hàm xử lý animation sóng (giữ nguyên không đổi) ---
    private void setupWaveAnimators() {
        animator1ScaleX = ObjectAnimator.ofFloat(wave1, "scaleX", 1f, 1.5f);
        animator1ScaleX.setDuration(1500);
        animator1ScaleX.setRepeatCount(ObjectAnimator.INFINITE);
        animator1ScaleY = ObjectAnimator.ofFloat(wave1, "scaleY", 1f, 1.5f);
        animator1ScaleY.setDuration(1500);
        animator1ScaleY.setRepeatCount(ObjectAnimator.INFINITE);
        animator1Alpha = ObjectAnimator.ofFloat(wave1, "alpha", 0.5f, 0f);
        animator1Alpha.setDuration(1500);
        animator1Alpha.setRepeatCount(ObjectAnimator.INFINITE);

        animator2ScaleX = ObjectAnimator.ofFloat(wave2, "scaleX", 1f, 1.5f);
        animator2ScaleX.setDuration(1500);
        animator2ScaleX.setStartDelay(200);
        animator2ScaleX.setRepeatCount(ObjectAnimator.INFINITE);
        animator2ScaleY = ObjectAnimator.ofFloat(wave2, "scaleY", 1f, 1.5f);
        animator2ScaleY.setDuration(1500);
        animator2ScaleY.setStartDelay(200);
        animator2ScaleY.setRepeatCount(ObjectAnimator.INFINITE);
        animator2Alpha = ObjectAnimator.ofFloat(wave2, "alpha", 0.3f, 0f);
        animator2Alpha.setDuration(1500);
        animator2Alpha.setStartDelay(200);
        animator2Alpha.setRepeatCount(ObjectAnimator.INFINITE);

        animator3ScaleX = ObjectAnimator.ofFloat(wave3, "scaleX", 1f, 1.5f);
        animator3ScaleX.setDuration(1500);
        animator3ScaleX.setStartDelay(400);
        animator3ScaleX.setRepeatCount(ObjectAnimator.INFINITE);
        animator3ScaleY = ObjectAnimator.ofFloat(wave3, "scaleY", 1f, 1.5f);
        animator3ScaleY.setDuration(1500);
        animator3ScaleY.setStartDelay(400);
        animator3ScaleY.setRepeatCount(ObjectAnimator.INFINITE);
        animator3Alpha = ObjectAnimator.ofFloat(wave3, "alpha", 0.1f, 0f);
        animator3Alpha.setDuration(1500);
        animator3Alpha.setStartDelay(400);
        animator3Alpha.setRepeatCount(ObjectAnimator.INFINITE);
    }

    private void startWaves() {
        animator1ScaleX.start(); animator1ScaleY.start(); animator1Alpha.start();
        animator2ScaleX.start(); animator2ScaleY.start(); animator2Alpha.start();
        animator3ScaleX.start(); animator3ScaleY.start(); animator3Alpha.start();
    }

    private void stopWaves() {
        animator1ScaleX.cancel(); animator1ScaleY.cancel(); animator1Alpha.cancel();
        animator2ScaleX.cancel(); animator2ScaleY.cancel(); animator2Alpha.cancel();
        animator3ScaleX.cancel(); animator3ScaleY.cancel(); animator3Alpha.cancel();

        wave1.setScaleX(1f); wave1.setScaleY(1f); wave1.setAlpha(0.5f);
        wave2.setScaleX(1f); wave2.setScaleY(1f); wave2.setAlpha(0.3f);
        wave3.setScaleX(1f); wave3.setScaleY(1f); wave3.setAlpha(0.1f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}