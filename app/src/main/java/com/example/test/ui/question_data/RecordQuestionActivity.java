package com.example.test.ui.question_data;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.SpeechRecognitionCallback;
import com.example.test.SpeechRecognitionHelper;
import com.example.test.api.*;
import com.example.test.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RecordQuestionActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    LinearLayout imgVoice;
    private ImageView imgReset;
    private TextView tvTranscription, key;
    private SpeechRecognitionHelper speechRecognitionHelper;
    private ObjectAnimator animator;
    private List<String> userAnswers = new ArrayList<>();
    List<String> correctAnswers = new ArrayList<>();
    private List<Question> questions;
    private int currentQuestionIndex;
    private int currentStep = 0;
    private int lessonID, courseID, enrollmentId;
    private  String questype;
    private int totalSteps;
    QuestionManager quesManager = new QuestionManager(this);
    private MediaRecorder recorder;
    private String filePath;
    private Handler handler = new Handler();
    private File recordedFile;
    private Runnable updateSeekBar;
    private boolean isPlaying = false;
    TextView tvQuestion;
    private AudioManager audioManager = new AudioManager(this);
    private ProgressDialog progressDialog;
    // Thêm vào đầu class SpeakingActivity
    private View wave1, wave2, wave3;
    private ObjectAnimator animator1ScaleX, animator1ScaleY, animator1Alpha;
    private ObjectAnimator animator2ScaleX, animator2ScaleY, animator2Alpha;
    private ObjectAnimator animator3ScaleX, animator3ScaleY, animator3Alpha;
    private boolean isRecordingAnimation = false; // Trạng thái animation khi ghi âm
    private double confidence=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_question);
        imgVoice = findViewById(R.id.imgVoice);
        tvTranscription = findViewById(R.id.tvTranscription);
        Button btnCheckResult = findViewById(R.id.btnCheckResult);
        tvQuestion = findViewById(R.id.tvQuestion);
        key = findViewById(R.id.key);
        wave1 = findViewById(R.id.wave_1);
        wave2 = findViewById(R.id.wave_2);
        wave3 = findViewById(R.id.wave_3);
        imgReset= findViewById(R.id.imgReset);
        setupWaveAnimators();
        currentQuestionIndex = getIntent().getIntExtra("currentQuestionIndex", 0);
        questions = (List<Question>) getIntent().getSerializableExtra("questions");
        courseID = getIntent().getIntExtra("courseID", 1);
        lessonID = getIntent().getIntExtra("lessonID", 1);
        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        totalSteps= questions.size();
        loadQuestion(currentQuestionIndex);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            imgVoice.setOnClickListener(v -> {
                if(!isRecordingAnimation){
                    startRecording();
                    startWaves();
                    isRecordingAnimation = true;
//                    Toast.makeText(this, "Đang ghi âm...", Toast.LENGTH_SHORT).show();
                }
                else{
                    stopRecording();
                    stopWaves();
                    isRecordingAnimation = false;
                }
            });
            imgReset.setOnClickListener(v -> resetRecording()); // Sự kiện cho nút reset
        }

        btnCheckResult.setOnClickListener(v -> {
            audioManager.uploadAndTranscribeM4A(recordedFile, new ApiCallback<SpeechResult>() {

                @Override
                public void onSuccess() {

                }

                @Override
                public void onSuccess(SpeechResult result) {
                    Log.d("SPEECH_TO_TEXT", result.toString());
                    confidence= result.getConfidence();
                    checkAnswer(result.getTranscript());
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("SPEECH_TO_TEXT", errorMessage);
                }
            });
        });
    }
    private void checkAnswer(String userAnswer) {
        String questionContent = tvQuestion.getText().toString().trim();
        ApiService apiService = new ApiService(this);


        runOnUiThread(() -> {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.load));
            progressDialog.setCancelable(false);
            progressDialog.show();
        });

        apiService.sendAnswerToApi(questionContent, userAnswer, new ApiCallback<EvaluationResult>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(EvaluationResult result) {

                double point = result.getPoint() * 0.7 + confidence * 0.3;
                // Lưu kết quả vào hệ thống
                quesManager.saveUserAnswer(questions.get(currentStep).getId(), userAnswer, point, result.getimprovements(),enrollmentId, new ApiCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("RecordQuestionActivity.this", "Lưu thành công!");
                        resetRecording();
                        progressDialog.dismiss();
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(RecordQuestionActivity.this, questype, null, null, result.getPoint(), result.getimprovements(), result.getevaluation(), () -> {
                                tvTranscription.setText("");
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
                Log.e("WritingActivity", "Lỗi lưu câu trả lời: " + errorMessage);
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

    private void checkAnswer1(String userAnswer) {
        // Hiển thị ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        EvaluationResult result = new EvaluationResult(confidence);

        // Lưu kết quả vào hệ thống
        quesManager.saveUserAnswer(questions.get(currentStep).getId(), userAnswer, result.getPoint(), result.getimprovements(),enrollmentId, new ApiCallback() {
            @Override
            public void onSuccess() {
                Log.d("RecordQuestionActivity.this", "Lưu thành công!");
                progressDialog.dismiss();
                runOnUiThread(() -> {
                    PopupHelper.showResultPopup(RecordQuestionActivity.this, questype, null, null, result.getPoint(), result.getimprovements(), result.getevaluation(), () -> {
                        tvTranscription.setText("");
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


    private void showErrorDialog(String message) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(RecordQuestionActivity.this)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        tvTranscription.setText("");
                    })
                    .show();
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
                            TextView tvQuestion = findViewById(R.id.tvQuestion);
                            tvQuestion.setText(question.getQuesContent());

                            List<QuestionChoice> choices = question.getQuestionChoices();
                            correctAnswers.clear();
                            for (QuestionChoice choice : choices) {
                                if (choice.isChoiceKey()) {
                                    correctAnswers.add(choice.getChoiceContent());
                                }
                            }
                        });
                    } else {
                        Log.e("RecordQuestionActivity", "Câu hỏi trả về là null.");
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("GrammarPick1QuestionActivity", errorMessage);
                }

                @Override
                public void onSuccess() {
                }
            });
        } else {
            finishLesson();
        }
    }

    private void finishLesson() {
        Intent intent = new Intent(RecordQuestionActivity.this, PointResultLessonActivity.class);
        intent.putExtra("lessonId", lessonID);
        intent.putExtra("courseId", courseID);
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
                bar.setBackgroundColor(Color.parseColor("#C4865E")); // Màu đã hoàn thành
            } else {
                bar.setBackgroundColor(Color.parseColor("#E0E0E0")); // Màu chưa hoàn thành
            }
            progressContainer.addView(bar);
        }
    }


    private void setupWaveAnimators() {
        // Animator cho wave 1
        animator1ScaleX = ObjectAnimator.ofFloat(wave1, "scaleX", 1f, 1.5f);
        animator1ScaleX.setDuration(1500);
        animator1ScaleX.setStartDelay(0);
        animator1ScaleX.setRepeatCount(ObjectAnimator.INFINITE);

        animator1ScaleY = ObjectAnimator.ofFloat(wave1, "scaleY", 1f, 1.5f);
        animator1ScaleY.setDuration(1500);
        animator1ScaleY.setStartDelay(0);
        animator1ScaleY.setRepeatCount(ObjectAnimator.INFINITE);

        animator1Alpha = ObjectAnimator.ofFloat(wave1, "alpha", 0.5f, 0f);
        animator1Alpha.setDuration(1500);
        animator1Alpha.setStartDelay(0);
        animator1Alpha.setRepeatCount(ObjectAnimator.INFINITE);

        // Animator cho wave 2
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

        // Animator cho wave 3
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
        if (animator1ScaleX.isPaused()) {
            animator1ScaleX.resume();
            animator1ScaleY.resume();
            animator1Alpha.resume();
        } else if (!animator1ScaleX.isRunning()) {
            animator1ScaleX.start();
            animator1ScaleY.start();
            animator1Alpha.start();
        }

        if (animator2ScaleX.isPaused()) {
            animator2ScaleX.resume();
            animator2ScaleY.resume();
            animator2Alpha.resume();
        } else if (!animator2ScaleX.isRunning()) {
            animator2ScaleX.start();
            animator2ScaleY.start();
            animator2Alpha.start();
        }

        if (animator3ScaleX.isPaused()) {
            animator3ScaleX.resume();
            animator3ScaleY.resume();
            animator3Alpha.resume();
        } else if (!animator3ScaleX.isRunning()) {
            animator3ScaleX.start();
            animator3ScaleY.start();
            animator3Alpha.start();
        }
    }

    private void stopWaves() {
        animator1ScaleX.pause();
        animator1ScaleY.pause();
        animator1Alpha.pause();

        animator2ScaleX.pause();
        animator2ScaleY.pause();
        animator2Alpha.pause();

        animator3ScaleX.pause();
        animator3ScaleY.pause();
        animator3Alpha.pause();
    }


    private void resetRecording() {
        // Dừng ghi âm nếu đang ghi
        if (isRecordingAnimation && speechRecognitionHelper != null) {
            speechRecognitionHelper.stopListening();
            isRecordingAnimation = false;
        }

        // Dừng và đặt lại animation
        resetWaves();

        // Xóa nội dung cũ
        tvTranscription.setText("");
        userAnswers.clear();

        runOnUiThread(() -> {
            Toast.makeText(RecordQuestionActivity.this, "Ghi âm đã được reset", Toast.LENGTH_SHORT).show();
        });

    }

    private void resetWaves() {
        // Dừng các animator nếu đang chạy
        stopWaves();

        // Đặt lại scale và alpha về giá trị ban đầu
        wave1.setScaleX(1f);
        wave1.setScaleY(1f);
        wave1.setAlpha(0.5f);

        wave2.setScaleX(1f);
        wave2.setScaleY(1f);
        wave2.setAlpha(0.3f);

        wave3.setScaleX(1f);
        wave3.setScaleY(1f);
        wave3.setAlpha(0.1f);
    }



    private void startRecording() {
        File outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        String fileName = "recorded_audio_" + System.currentTimeMillis() + ".m4a";
        recordedFile = new File(outputDir, fileName);
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // vẫn giữ MPEG_4
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);    // encoder phù hợp cho m4a
        recorder.setOutputFile(recordedFile.getAbsolutePath());
        try {
            recorder.prepare();
            recorder.start();
            isRecordingAnimation = true;
            startWaves();
            Log.d("Succsse", "Recording started");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Fail", "Recording failed");
        }
    }
    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        if (animator != null) {
            animator.cancel();
        }

        isRecordingAnimation = false;

        audioManager.uploadAndTranscribeM4A(recordedFile, new ApiCallback<SpeechResult>() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(SpeechResult result) {
                Log.d("SPEECH_TO_TEXT", result.toString());
                String resultText = result.toString();  // convert SpeechResult to String
                String extracted = resultText;
                if (resultText.contains("Transcript:") && resultText.contains(",")) {
                    int start = resultText.indexOf("Transcript:") + "Transcript:".length();
                    int end = resultText.indexOf(",", start);
                    extracted = resultText.substring(start, end).trim();
                }

                tvTranscription.setText(extracted);
                confidence= result.getConfidence();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("SPEECH_TO_TEXT", errorMessage);
            }
        });
        Log.d("Succsse", "Recording saved to: " + recordedFile.getAbsolutePath());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recorder != null) {
            recorder.release();
        }
    }
}
