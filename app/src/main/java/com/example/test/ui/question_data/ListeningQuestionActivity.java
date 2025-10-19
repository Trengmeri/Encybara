package com.example.test.ui.question_data;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.BaseActivity;
import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.ApiService;
import com.example.test.api.BaseApiManager;
import com.example.test.api.LearningMaterialsManager;
import com.example.test.api.LessonManager;
import com.example.test.api.MediaManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Answer;
import com.example.test.model.EvaluationResult;
import com.example.test.model.MediaFile;
import com.example.test.model.Question;
import com.example.test.model.QuestionChoice;
import com.example.test.ui.entrance_test.ListeningActivity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ListeningQuestionActivity extends BaseActivity {
    String correctAnswers ;
    private MediaPlayer mediaPlayer;
    private EditText etAnswer;
    private List<Question> questions; // Danh sách câu hỏi
    private int currentQuestionIndex; // Vị trí câu hỏi hiện tại
    private List<String> userAnswers = new ArrayList<>();
    private int currentStep = 0; // Bước hiện tại (bắt đầu từ 0)
    private int totalSteps; // Tổng số bước trong thanh tiến trình
    private int lessonID,courseID,enrollmentId;
    private int answerIds;
    private  String questype, audioUrl;
    ImageView imgLessonMaterial,btnReplay;
    TextView tvQuestion;
    Button btnCheckResult;
    private LinearLayout btnListen;
    // Thêm các View cho vòng tròn sóng âm
    private View wave1, wave2, wave3;
    private ObjectAnimator animator1ScaleX, animator1ScaleY, animator1Alpha;
    private ObjectAnimator animator2ScaleX, animator2ScaleY, animator2Alpha;
    private ObjectAnimator animator3ScaleX, animator3ScaleY, animator3Alpha;
    private boolean isPlayingAnimation = false;
    private int currentPosition = 0; // Lưu vị trí hiện tại của âm thanh
    private boolean isPaused = false; // Trạng thái tạm dừng của âm thanh
    private String lessonAudioUrl; // Cache for lesson audio
    private boolean isUsingLessonAudio = false; // Track if lesson audio is in use

    QuestionManager quesManager = new QuestionManager(this);
    LessonManager lesManager = new LessonManager();
    ResultManager resultManager = new ResultManager(this);
    MediaManager mediaManager = new MediaManager(this);
    LearningMaterialsManager materialsManager = new LearningMaterialsManager(this);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listening_question);

        btnListen = findViewById(R.id.btnListen);
        tvQuestion = findViewById(R.id.tvQuestion);
        btnCheckResult = findViewById(R.id.btnCheckResult);
        etAnswer = findViewById(R.id.etAnswer);
        createProgressBars(totalSteps, currentQuestionIndex);
        imgLessonMaterial = findViewById(R.id.imgLessonMaterial);
        btnReplay=findViewById(R.id.btnReplay);
        // Ánh xạ các vòng tròn sóng âm
        wave1 = findViewById(R.id.wave_1);
        wave2 = findViewById(R.id.wave_2);
        wave3 = findViewById(R.id.wave_3);

        // Khởi tạo các animator nhưng không chạy ngay
        setupWaveAnimators();
        // Nhận dữ liệu từ Intent
        currentQuestionIndex = getIntent().getIntExtra("currentQuestionIndex", 0);
        questions = (List<Question>) getIntent().getSerializableExtra("questions");
        courseID = getIntent().getIntExtra("courseID",1);
        lessonID = getIntent().getIntExtra("lessonID",1);
        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        totalSteps= questions.size();
        createProgressBars(totalSteps, currentQuestionIndex);

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.seekTo(currentPosition);
                mediaPlayer.start();
                btnCheckResult.setEnabled(true);
                startWaves();
                isPlayingAnimation = true;
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("MediaPlayerError", "Error occurred: what=" + what + ", extra=" + extra);
                stopWaves();
                isPlayingAnimation = false;
                Toast.makeText(this, "Lỗi khi phát audio", Toast.LENGTH_SHORT).show();
                return true;
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                stopWaves();
                isPlayingAnimation = false;
                currentPosition = 0;
                isPaused = false;
            });
        }
        btnReplay.setOnClickListener(v -> {
            replayAudio();
        });

        // Hiển thị câu hỏi hiện tại
        btnListen.setOnClickListener(v -> {
            Log.d("AudioTest", "Đã click vào nút nghe");
            toggleAudioAndAnimation();
        });
        loadQuestion(currentQuestionIndex);
        materialsManager.fetchAndLoadImageByLesId(lessonID, imgLessonMaterial);

        int questionID = questions.get(currentQuestionIndex).getId();
        fetchAudio(questionID, lessonID);

        LinearLayout progressBar = findViewById(R.id.progressBar); // Ánh xạ ProgressBar

        btnCheckResult.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pauseAudio();
                stopWaves();
                isPlayingAnimation = false;
            }
            String userAnswer = etAnswer.getText().toString().trim();
            Log.d("ListeningQuestionActivity", "User Answers: " + userAnswers);
            if (userAnswer.isEmpty()) {
                Toast.makeText(ListeningQuestionActivity.this, "Vui lòng trả lời câu hỏi!", Toast.LENGTH_SHORT).show();
            } else {
                String answerContent = userAnswer;
                // Lưu câu trả lời của người dùng
                quesManager.saveUserAnswer(questions.get(currentQuestionIndex).getId(), answerContent,0,null,enrollmentId, new ApiCallback() {
                    @Override
                    public void onSuccess() {
                        Log.e("ListeningQuestionActivity", "Câu trả lời đã được lưu: " + answerContent);
                        // Hiển thị popup
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(ListeningQuestionActivity.this, questype, answerContent, correctAnswers, null, null, null, () -> {
                                currentQuestionIndex++; // Tăng currentStep
                                etAnswer.setText("");
                                // Kiểm tra nếu hoàn thành
                                if (currentQuestionIndex < questions.size()) {
                                    Question nextQuestion = questions.get(currentQuestionIndex);
                                    createProgressBars(totalSteps, currentQuestionIndex);
                                    if (nextQuestion.getQuesType().equals("CHOICE")) {
                                        Intent intent = new Intent(ListeningQuestionActivity.this, ListeningChoiceActivity.class);
                                        intent.putExtra("currentQuestionIndex", currentQuestionIndex);
                                        Log.e("pick1", "currentQuestionIndex");
                                        intent.putExtra("questions", (Serializable) questions);
                                        intent.putExtra("courseID", courseID);
                                        intent.putExtra("lessonID", lessonID);
                                        intent.putExtra("enrollmentId", enrollmentId);
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
                                    Log.e("ListeningQuestionActivity", "Answer ID từ API: " + answer.getId());
                                    if (answerIds != 0) {
                                        QuestionManager.gradeAnswer(answerIds, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                Log.e("ListeningQuestionActivity", "Lỗi khi chấm điểm: " + e.getMessage());
                                            }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    Log.e("ListeningQuestionActivity", "Chấm điểm thành công cho Answer ID: " + answerIds);
                                                } else {
                                                    Log.e("ListeningQuestionActivity", "Lỗi từ server: " + response.code());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("ListeningQuestionActivity", "Bài học không có câu trl.");
                                    }
                                } else {
                                    Log.e("ListeningQuestionActivity", "Không nhận được câu trả lời từ API.");
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
    private void fetchAudio(int questionId, int lessonId) {
        mediaManager.fetchMediaByQuesId(questionId, new ApiCallback<MediaFile>() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onSuccess(MediaFile mediaFile) {
                runOnUiThread(() -> {
                    if (mediaFile != null && mediaFile.getMaterLink() != null && mediaFile.getMaterLink().endsWith(".mp3")) {
                        audioUrl = BaseApiManager.replaceHost(mediaFile.getMaterLink());
                        isUsingLessonAudio = false;
                        currentPosition = 0; // Reset position for new question-specific audio
                        resetMediaPlayer(audioUrl);
                    } else {
                        fetchAudioByLessonId(lessonId);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    fetchAudioByLessonId(lessonId);
                });
            }
        });
    }
    private void fetchAudioByLessonId(int lessonId) {
        if (lessonAudioUrl != null && !lessonAudioUrl.isEmpty()) {
            // Use cached lesson audio
            audioUrl = lessonAudioUrl;
            isUsingLessonAudio = true;
            // Do not reset MediaPlayer to preserve currentPosition
            if (mediaPlayer == null) {
                initializeMediaPlayer(audioUrl);
            }
            return;
        }

        materialsManager.fetchAudioByLesId(lessonId, new ApiCallback<String>() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    if (result != null && !result.isEmpty()) {
                        lessonAudioUrl = result; // Cache the lesson audio URL
                        audioUrl = lessonAudioUrl;
                        isUsingLessonAudio = true;
                        resetMediaPlayer(audioUrl); // Only reset on first fetch
                    } else {
                        Toast.makeText(ListeningQuestionActivity.this, "Không tìm thấy audio", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(ListeningQuestionActivity.this, "Lỗi khi lấy audio: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private void playAudio(String audioUrl) {
        if (audioUrl == null || audioUrl.isEmpty()) {
            Log.e("MediaPlayerError", "Audio URL is null or empty");
            Toast.makeText(this, "Không có audio để phát", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (mediaPlayer == null) {
                initializeMediaPlayer(audioUrl);
            } else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(currentPosition);
                mediaPlayer.start();
                startWaves();
                isPlayingAnimation = true;
            }
        } catch (IllegalStateException e) {
            Log.e("MediaPlayerError", "IllegalStateException: " + e.getMessage());
            resetMediaPlayer(audioUrl);
        }
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
                            tvQuestion.setText(question.getQuesContent());
                            List<QuestionChoice> choices = question.getQuestionChoices();
                            for (QuestionChoice choice : choices) {
                                if (choice.isChoiceKey()) {
                                    correctAnswers=(choice.getChoiceContent());
                                }
                            }
                        });
                        fetchAudio(question.getId(), lessonID);
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
        Intent intent = new Intent(ListeningQuestionActivity.this, PointResultLessonActivity.class);
        intent.putExtra("lessonId",lessonID);
        intent.putExtra("courseId",courseID);
        intent.putExtra("enrollmentId", enrollmentId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                Log.e("MediaPlayerError", "Error in onDestroy: " + e.getMessage());
            }
            mediaPlayer = null;
        }
        ObjectAnimator[] animators = {animator1ScaleX, animator1ScaleY, animator1Alpha,
                animator2ScaleX, animator2ScaleY, animator2Alpha,
                animator3ScaleX, animator3ScaleY, animator3Alpha};
        for (ObjectAnimator animator : animators) {
            if (animator != null) animator.cancel();
        }
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

    // Bắt đầu hoạt hình
    private void startWaves() {
        // Kiểm tra trạng thái của animator và quyết định start hoặc resume
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

    // Dừng hoạt hình
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
    // Đồng bộ âm thanh và hoạt hình
    private void toggleAudioAndAnimation() {
        if (!isPlayingAnimation) {
            if (isPaused && mediaPlayer != null) {
                mediaPlayer.start();
                startWaves();
                isPlayingAnimation = true;
                isPaused = false;
            } else {
                playAudio(audioUrl);
            }
        } else {
            pauseAudio();
            stopWaves();
            isPlayingAnimation = false;
        }
    }
    private void replayAudio() {
        if (audioUrl == null) return;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        stopWaves();
        resetWaveViews();
        currentPosition = 0;
        isPaused = false;
        initializeMediaPlayer(audioUrl);
    }
    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currentPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            isPaused = true;
        }
    }
    private void initializeMediaPlayer(String audioUrl) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.seekTo(currentPosition);
                mediaPlayer.start();
                btnCheckResult.setEnabled(true);
                startWaves();
                isPlayingAnimation = true;
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("MediaPlayerError", "Error occurred: what=" + what + ", extra=" + extra);
                stopWaves();
                isPlayingAnimation = false;
                Toast.makeText(ListeningQuestionActivity.this, "Lỗi khi phát audio", Toast.LENGTH_SHORT).show();
                return true;
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                stopWaves();
                isPlayingAnimation = false;
                currentPosition = 0;
                isPaused = false;
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("MediaPlayerError", "Error initializing: " + e.getMessage());
            Toast.makeText(this, "Lỗi khi khởi tạo audio", Toast.LENGTH_SHORT).show();
        }
    }
    private void resetWaveViews() {
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
    private void resetMediaPlayer(String audioUrl) {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
            } catch (IllegalStateException e) {
                Log.e("MediaPlayerError", "Error resetting: " + e.getMessage());
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        initializeMediaPlayer(audioUrl);
    }
}