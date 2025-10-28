package com.example.test.ui.entrance_test;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.AudioManager;
import com.example.test.api.QuestionManager;
import com.example.test.model.EvaluationResult;
import com.example.test.model.PhonemeScore;
import com.example.test.model.PronunciationResult;
import com.example.test.model.Question;
import com.example.test.model.SampleAnswer;
import com.example.test.model.Schedule;
import com.example.test.response.ApiResponseSampleAns;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class SpeakingActivity extends AppCompatActivity {

    // Views
    private LinearLayout imgVoice;
    private TextView tvQuestion, tvTalk, tvphoneme; // tvTalk thay thế cho tvTranscription và key
    private ProgressDialog progressDialog;
    private Button btnRetry, btnNextQuestion;
    private LinearLayout actionButtonsContainer;

    // Data
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int lessonID, courseID, enrollmentId;
    private int totalSteps;

    // Managers
    private QuestionManager quesManager = new QuestionManager(this);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_question);

        // Ánh xạ các view từ layout mới
        imgVoice = findViewById(R.id.imgVoice);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTalk = findViewById(R.id.tvTalk); // View mới để hiển thị text
        tvphoneme = findViewById(R.id.tvphoneme); // View mới để hiển thị text
        wave1 = findViewById(R.id.wave_1);
        wave2 = findViewById(R.id.wave_2);
        wave3 = findViewById(R.id.wave_3);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        btnRetry = findViewById(R.id.btnRetry);
        actionButtonsContainer = findViewById(R.id.actionButtonsContainer);

        // Lấy dữ liệu từ Intent
        currentQuestionIndex = getIntent().getIntExtra("currentQuestionIndex", 0);
        questions = (List<Question>) getIntent().getSerializableExtra("questions");
        lessonID = 6;
        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        totalSteps = questions.size();

        setupWaveAnimators();
        createProgressBars(totalSteps, currentQuestionIndex);
        loadQuestion(currentQuestionIndex);
        tvTalk.setText(""); // Khởi đầu với text rỗng
        tvphoneme.setText("");

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

    private enum DisplayType {
        CHARACTER, PHONEME
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

        runOnUiThread(() -> {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.load));
            progressDialog.setCancelable(false);
            progressDialog.show();
        });

        // Lấy transcript từ câu hỏi đang hiển thị
        String transcript = tvTalk.getText().toString();

        // GỌI HÀM MỚI (ĐÃ SỬA HOÀN CHỈNH)
        audioManager.assessPronunciation(recordedFile, transcript, new ApiCallback<PronunciationResult>() {
            @Override
            public void onSuccess(PronunciationResult result) {
                if (recordedFile != null) {
                    recordedFile.delete(); // Xóa file sau khi gửi
                }

                // BẮT BUỘC: Chuyển mọi thao tác UI vào luồng chính
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    Log.d("PRONUNCIATION_RESULT", "Overall Score: " + result.getOverallScore());

                    // 1. Hiển thị kết quả chấm điểm (tô màu text)
                    SpannableStringBuilder characterSpannable = buildColoredSpannable(result.getPhonemeScores(), DisplayType.CHARACTER, false);
                    tvTalk.setText(characterSpannable);

                    SpannableStringBuilder phonemeSpannable = buildColoredSpannable(result.getPhonemeScores(), DisplayType.PHONEME, true);
                    tvphoneme.setText(phonemeSpannable);

                    // 2. Hiển thị các nút lựa chọn
                    actionButtonsContainer.setVisibility(View.VISIBLE);
                    btnRetry.setEnabled(true); // Đảm bảo nút có thể nhấn
                    btnNextQuestion.setEnabled(true); // Đảm bảo nút có thể nhấn

                    // 3. Gán sự kiện cho nút "Thử lại"
                    btnRetry.setOnClickListener(v -> {
                        // Reset giao diện về trạng thái ban đầu cho câu hỏi hiện tại
                        tvphoneme.setText("");
                        actionButtonsContainer.setVisibility(View.GONE);
                        // Tải lại câu trả lời mẫu ngẫu nhiên mới cho câu hỏi hiện tại
                        loadQuestion(currentQuestionIndex);
                    });

                    // 4. Gán sự kiện cho nút "Câu tiếp theo" (ĐÚNG CÚ PHÁP)
                    btnNextQuestion.setOnClickListener(v -> {

                        // Hiển thị loading khi đang lưu câu trả lời
                        progressDialog = new ProgressDialog(SpeakingActivity.this);
                        progressDialog.setMessage(getString(R.string.load));
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        // Gọi API để lưu câu trả lời
                        quesManager.saveUserAnswer(
                                questions.get(currentQuestionIndex).getId(),
                                transcript,
                                result.getOverallScore(),
                                null,
                                enrollmentId,
                                new ApiCallback<Void>() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d("CheckAnswer", "Lưu câu trả lời thành công!");
                                        // Chuyển sang câu hỏi tiếp theo trên luồng chính
                                        runOnUiThread(() -> {
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }

                                            currentQuestionIndex++;
                                            if (currentQuestionIndex < questions.size()) {
                                                // Reset giao diện hoàn toàn cho câu hỏi mới
                                                tvTalk.setText("");
                                                tvphoneme.setText("");

                                                actionButtonsContainer.setVisibility(View.GONE);

                                                createProgressBars(totalSteps, currentQuestionIndex);
                                                loadQuestion(currentQuestionIndex);
                                            } else {
                                                finishLesson();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Log.e("CheckAnswer", "Lỗi lưu câu trả lời: " + errorMessage);
                                        // Hiển thị lỗi và kích hoạt lại nút trên luồng chính
                                        runOnUiThread(() -> {
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            // Kích hoạt lại nút để người dùng có thể thử lại
                                            btnNextQuestion.setEnabled(true);
                                            btnRetry.setEnabled(true);
                                            showErrorDialog("Lỗi khi lưu kết quả. Vui lòng thử lại.");
                                        });
                                    }

                                    // Phương thức này có thể không cần thiết nếu ApiCallback của bạn
                                    // không yêu cầu, nhưng để đây cho an toàn.
                                    @Override
                                    public void onSuccess(Void result) {
                                        onSuccess(); // Gọi lại phương thức onSuccess() không có tham số
                                    }
                                }
                        );
                    });
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (recordedFile != null) {
                    recordedFile.delete();
                }
                Log.e("PRONUNCIATION_RESULT", errorMessage);

                // Đảm bảo hiển thị lỗi trên luồng chính
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    showErrorDialog("Lỗi chấm điểm phát âm: " + errorMessage);
                });
            }

            // Để trống phương thức này nếu ApiCallback của bạn có 2 hàm onSuccess
            // (một có tham số, một không) và bạn không dùng đến nó.
            @Override
            public void onSuccess() {}
        });
    }

    /**
     * HÀM CHUNG: Xây dựng một chuỗi Spannable được tô màu
     * @param phonemeScores Danh sách điểm của âm vị
     * @param type Loại hiển thị (ký tự hay phiên âm)
     * @param includeSlashes Thêm dấu / ở đầu và cuối (chỉ dùng cho phoneme)
     * @return Một đối tượng SpannableStringBuilder đã được định dạng
     */
    private SpannableStringBuilder buildColoredSpannable(List<PhonemeScore> phonemeScores, DisplayType type, boolean includeSlashes) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();
        if (phonemeScores == null || phonemeScores.isEmpty()) {
            return spannableBuilder;
        }

        // THÊM BƯỚC NÀY: Thêm dấu / ở đầu nếu được yêu cầu
        if (includeSlashes) {
            spannableBuilder.append("/");
        }

        int currentWordIndex = phonemeScores.get(0).getWordIndex();

        int goodColor = ContextCompat.getColor(this, R.color.pronunciation_good);
        int fairColor = ContextCompat.getColor(this, R.color.pronunciation_fair);
        int poorColor = ContextCompat.getColor(this, R.color.pronunciation_poor);
        int defaultColor = ContextCompat.getColor(this, R.color.pronunciation_default);

        for (PhonemeScore score : phonemeScores) {
            if (score.getWordIndex() > currentWordIndex) {
                spannableBuilder.append(" ");
                currentWordIndex = score.getWordIndex();
            }

            String textToAppend = (type == DisplayType.CHARACTER) ? score.getCharacter() : score.getPhoneme();

            int start = spannableBuilder.length();
            spannableBuilder.append(textToAppend);
            int end = spannableBuilder.length();

            int color;
            String quality = score.getQuality();
            if ("good".equalsIgnoreCase(quality)) {
                color = goodColor;
            } else if ("fair".equalsIgnoreCase(quality)) {
                color = fairColor;
            } else if ("poor".equalsIgnoreCase(quality)) {
                color = poorColor;
            } else {
                color = defaultColor;
            }

            spannableBuilder.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // THÊM BƯỚC NÀY: Thêm dấu / ở cuối nếu được yêu cầu
        if (includeSlashes) {
            spannableBuilder.append("/");
        }

        return spannableBuilder;
    }


    private void loadQuestion(int index) {
        if (index < questions.size()) {
            Question question = questions.get(index);
            tvQuestion.setText(question.getQuesContent());

            // Gọi API để lấy danh sách các câu trả lời mẫu
            quesManager.fetchSampleAnswersFromApi(question.getId(), new ApiCallback<List<SampleAnswer>>() {
                @Override
                public void onSuccess() {}

                @Override
                public void onSuccess(List<SampleAnswer> sampleAnswers) {
                    // Kiểm tra xem danh sách có hợp lệ và không rỗng không
                    if (sampleAnswers != null && !sampleAnswers.isEmpty()) {
                        // 1. Tạo một đối tượng Random
                        Random random = new Random();

                        // 2. Lấy một chỉ số ngẫu nhiên trong khoảng từ 0 đến (kích thước danh sách - 1)
                        int randomIndex = random.nextInt(sampleAnswers.size());

                        // 3. Lấy câu trả lời ngẫu nhiên từ danh sách
                        SampleAnswer randomAnswer = sampleAnswers.get(randomIndex);

                        // 4. Hiển thị nội dung của câu trả lời đó lên UI
                        // QUAN TRỌNG: Cập nhật UI phải được thực hiện trên Main Thread
                        runOnUiThread(() -> {
                            if (randomAnswer != null) {
                                // Giả sử tvTalk là TextView để hiển thị câu trả lời
                                tvTalk.setText(randomAnswer.getAnswerContent());
                            }
                        });
                    } else {
                        // Xử lý trường hợp danh sách rỗng hoặc null
                        runOnUiThread(() -> {
                            tvTalk.setText("Không có câu trả lời mẫu.");
                        });
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Xử lý khi API gọi thất bại
                    runOnUiThread(() -> {
                        // Hiển thị thông báo lỗi cho người dùng
                        tvTalk.setText("Lỗi khi tải câu trả lời.");
                        // Hoặc bạn có thể dùng Toast
                        // Toast.makeText(YourActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            finishLesson();
        }
    }

    private void finishLesson() {
        Intent intent = new Intent(SpeakingActivity.this, WritingActivity.class);
        intent.putExtra("enrollmentId", enrollmentId);
        startActivity(intent);
        finish();
    }

    private void showErrorDialog(String message) {
        runOnUiThread(() -> {
            if (isFinishing()) return;
            new AlertDialog.Builder(SpeakingActivity.this)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        tvTalk.setText("");
                    })
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

    // --- Các hàm xử lý animation sóng (giữ nguyên) ---
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