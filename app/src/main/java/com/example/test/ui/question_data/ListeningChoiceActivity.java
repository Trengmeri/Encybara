package com.example.test.ui.question_data;

import android.animation.ObjectAnimator;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.adapter.ChoiceAdapter;
import com.example.test.api.ApiCallback;
import com.example.test.api.LearningMaterialsManager;
import com.example.test.api.LessonManager;
import com.example.test.api.MediaManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Answer;
import com.example.test.model.MediaFile;
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

public class ListeningChoiceActivity extends AppCompatActivity {
    String correctAnswers;
    private List<String> userAnswers = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private List<Question> questions; // Danh sách câu hỏi
    private int currentQuestionIndex; // Vị trí câu hỏi hiện tại
    private int currentStep = 0; // Bước hiện tại (bắt đầu từ 0)
    private int totalSteps; // Tổng số bước trong thanh tiến trình
    private int lessonID,courseID,enrollmentId;
    private int answerIds;
    private  String questype,audioUrl;
    private LinearLayout btnListen;
    ImageView btnReplay, imgLessonMaterial;
    TextView tvQuestion;
    Button btnCheckResult;
    // Thêm các View cho vòng tròn sóng âm
    private View wave1, wave2, wave3;
    private ObjectAnimator animator1ScaleX, animator1ScaleY, animator1Alpha;
    private ObjectAnimator animator2ScaleX, animator2ScaleY, animator2Alpha;
    private ObjectAnimator animator3ScaleX, animator3ScaleY, animator3Alpha;
    private boolean isPlayingAnimation = false;
    private String lessonAudioUrl; // Cache for lesson audio
    private boolean isUsingLessonAudio = false; // Track if lesson audio is in use
    private int currentPosition = 0; // Lưu vị trí hiện tại của âm thanh
    private boolean isPaused = false; // Trạng thái tạm dừng của âm thanh
    private boolean isPreparing = false; // Track if MediaPlayer is preparing
    private boolean isSeekable = true; // Mặc định cho phép seek
    QuestionManager quesManager = new QuestionManager(this);
    LessonManager lesManager = new LessonManager();
    ResultManager resultManager = new ResultManager(this);
    MediaManager mediaManager = new MediaManager(this);
    LearningMaterialsManager materialsManager = new LearningMaterialsManager(this);
    private RecyclerView recyclerViewChoices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listening_choice);

        btnListen = findViewById(R.id.btnListen1);
        tvQuestion = findViewById(R.id.tvQuestion);
        btnCheckResult = findViewById(R.id.btnCheckResult);
        recyclerViewChoices = findViewById(R.id.recyclerViewChoices);
        imgLessonMaterial = findViewById(R.id.imgLessonMaterial);
        btnReplay=findViewById(R.id.btnReplay1);
        // Ánh xạ các vòng tròn sóng âm
        wave1 = findViewById(R.id.wave_1);
        wave2 = findViewById(R.id.wave_2);
        wave3 = findViewById(R.id.wave_3);

        // Khởi tạo các animator nhưng không chạy ngay
        setupWaveAnimators();
        createProgressBars(totalSteps, currentQuestionIndex);
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
        LinearLayout progressBar = findViewById(R.id.progressBar);
        // Nhận dữ liệu từ Intent
        currentQuestionIndex = getIntent().getIntExtra("currentQuestionIndex", 0);
        questions = (List<Question>) getIntent().getSerializableExtra("questions");
        courseID = getIntent().getIntExtra("courseID",1);
        lessonID = getIntent().getIntExtra("lessonID",1);
        totalSteps= questions.size();
        createProgressBars(totalSteps, currentQuestionIndex);
        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);

        btnReplay.setOnClickListener(v -> replayAudio());
        btnListen.setOnClickListener(v -> {
            Log.d("AudioTest", "Đã click vào nút nghe");
            toggleAudioAndAnimation();
        });
        // Hiển thị câu hỏi hiện tại
        loadQuestion(currentQuestionIndex);
        materialsManager.fetchAndLoadImageByLesId(lessonID, imgLessonMaterial);
        // Lấy audio theo questionID, fallback sang lessonID
        int questionID = questions.get(currentQuestionIndex).getId();
        fetchAudio(questionID, lessonID);

        btnCheckResult.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pauseAudio();
                stopWaves();
                isPlayingAnimation = false;
            }
            Log.d("ListeningChoiceActivity", "User Answers: " + userAnswers);
            if (userAnswers.isEmpty()) {
                Toast.makeText(ListeningChoiceActivity.this, "Vui lòng trả lời câu hỏi!", Toast.LENGTH_SHORT).show();
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
                quesManager.saveUserAnswer(questions.get(currentQuestionIndex).getId(), answerContent, 0, null,enrollmentId, new ApiCallback() {

                    @Override
                    public void onSuccess() {
                        Log.e("ListeningChoiceActivity", "Câu trả lời đã được lưu: " + answerContent);
                        // Hiển thị popup
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(ListeningChoiceActivity.this, questype, answerContent, correctAnswers, null, null, null, () -> {
                                // Callback khi nhấn Next Question trên popup
                                currentQuestionIndex++;
                                if (currentQuestionIndex < questions.size()) {
                                    Question nextQuestion = questions.get(currentQuestionIndex);
                                    createProgressBars(totalSteps, currentQuestionIndex);
                                    if (nextQuestion.getQuesType().equals("TEXT")) {
                                        Intent intent = new Intent(ListeningChoiceActivity.this, ListeningQuestionActivity.class);
                                        intent.putExtra("currentQuestionIndex", currentQuestionIndex);
                                        Log.e("pick1", "currentQuestionIndex");
                                        intent.putExtra("questions", (Serializable) questions);
                                        intent.putExtra("courseID", courseID);
                                        intent.putExtra("lessonID", lessonID);
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
                                    Log.e("ListeningChoiceActivity", "Answer ID từ API: " + answer.getId());
                                    if (answerIds != 0) {
                                        QuestionManager.gradeAnswer(answerIds, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                Log.e("ListeningChoiceActivity", "Lỗi khi chấm điểm: " + e.getMessage());
                                            }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    Log.e("ListeningChoiceActivity", "Chấm điểm thành công cho Answer ID: " + answerIds + "Diem: " + answer.getPointAchieved());
                                                } else {
                                                    Log.e("ListeningChoiceActivity", "Lỗi từ server: " + response.code());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("ListeningChoiceActivity", "Bài học không có câu trl.");
                                    }
                                } else {
                                    Log.e("ListeningChoiceActivity", "Không nhận được câu trả lời từ API.");
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
                        Log.e("ListeningChoiceActivity", errorMessage);
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
                        audioUrl = mediaFile.getMaterLink().replace("0.0.0.0", "14.225.198.3");
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
            audioUrl = lessonAudioUrl;
            isUsingLessonAudio = true;
            if (mediaPlayer == null || !mediaPlayer.isPlaying() && !isPaused) {
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
                        Toast.makeText(ListeningChoiceActivity.this, "Không tìm thấy audio", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(ListeningChoiceActivity.this, "Lỗi khi lấy audio: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void playAudio(String audioUrl) {
        if (audioUrl == null || audioUrl.isEmpty()) {
            Toast.makeText(this, "Không có audio để phát", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPreparing) {
            Log.d("MediaPlayer", "Đang chuẩn bị, bỏ qua yêu cầu");
            return;
        }

        try {
            if (mediaPlayer == null) {
                initializeMediaPlayer(audioUrl);
            } else if (isPaused) {
                if (isSeekable) {
                    try {
                        mediaPlayer.seekTo(currentPosition);
                        mediaPlayer.start();
                        startWaves();
                        isPlayingAnimation = true;
                        isPaused = false;
                    } catch (IllegalStateException e) {
                        Log.e("MediaPlayerError", "Lỗi tiếp tục phát: " + e.getMessage());
                        resetMediaPlayer(audioUrl);
                    }
                } else {
                    // Nếu không hỗ trợ seek, phát lại từ đầu
                    resetMediaPlayer(audioUrl);
                }
            } else if (!mediaPlayer.isPlaying()) {
                resetMediaPlayer(audioUrl);
            }
        } catch (IllegalStateException e) {
            Log.e("MediaPlayerError", "Lỗi trạng thái: " + e.getMessage());
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
                        // Lấy nội dung câu hỏi
                        String questionContent = question.getQuesContent();

                        Log.d("ListeningChoiceActivity", "Câu hỏi: " + questionContent);

                        List<QuestionChoice> choices = question.getQuestionChoices();
                        if (choices != null && !choices.isEmpty()) {
                            runOnUiThread(() -> {
                                tvQuestion.setText(questionContent);
                                userAnswers.clear();
                                ChoiceAdapter choiceAdapter = new ChoiceAdapter(ListeningChoiceActivity.this, choices, userAnswers);
                                recyclerViewChoices.setAdapter(choiceAdapter);
                                for (QuestionChoice choice : choices) {
                                    if (choice.isChoiceKey()) {
                                        correctAnswers=(choice.getChoiceContent());
                                    }
                                }
                            });
                        } else {
                                Log.e("ListeningChoiceActivity", "Câu hỏi không có lựa chọn.");
                        }
                        // Lấy audio cho câu hỏi mới
                        fetchAudio(question.getId(), lessonID);
                    } else {
                        Log.e("ListeningChoiceActivity", "Câu hỏi trả về là null.");
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("ListeningChoiceActivity", errorMessage);
                }

                @Override
                public void onSuccess() {}
            });
        } else {
            finishLesson();
        }
    }

    private void finishLesson() {
        Intent intent = new Intent(ListeningChoiceActivity.this, PointResultLessonActivity.class);
        intent.putExtra("lessonId",lessonID);
        intent.putExtra("courseId",courseID);
        intent.putExtra("enrollmentId", enrollmentId);
        startActivity(intent);
        finish();
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
        if (isPreparing) {
            Log.d("MediaPlayer", "Đang chuẩn bị, bỏ qua yêu cầu");
            return;
        }

        if (!isPlayingAnimation) {
            if (isPaused && mediaPlayer != null) {
                try {
                    if (isSeekable && currentPosition >= 0) {
                        mediaPlayer.seekTo(currentPosition);
                        mediaPlayer.start();
                        startWaves();
                        isPlayingAnimation = true;
                        isPaused = false;
                    } else {
                        resetMediaPlayer(audioUrl);
                    }
                } catch (IllegalStateException e) {
                    Log.e("MediaPlayerError", "Lỗi tiếp tục phát: " + e.getMessage());
                    resetMediaPlayer(audioUrl);
                }
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
        if (audioUrl == null) {
            Toast.makeText(this, "Không có audio để phát lại", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                if (isSeekable) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                    startWaves();
                    isPlayingAnimation = true;
                    currentPosition = 0;
                    isPaused = false;
                } else {
                    // Nếu không hỗ trợ seek, reset và phát lại
                    resetMediaPlayer(audioUrl);
                }
            } catch (IllegalStateException e) {
                Log.e("MediaPlayerError", "Lỗi phát lại: " + e.getMessage());
                resetMediaPlayer(audioUrl);
            }
        } else {
            resetMediaPlayer(audioUrl);
        }

        resetWaveViews();
    }

    private void pauseAudio() {
        if (mediaPlayer == null) return;

        try {
            if (mediaPlayer.isPlaying()) {
                try {
                    currentPosition = mediaPlayer.getCurrentPosition();
                } catch (IllegalStateException e) {
                    currentPosition = 0;
                }
                mediaPlayer.pause();
                isPaused = true;
            }
        } catch (IllegalStateException e) {
            Log.e("MediaPlayerError", "Lỗi trạng thái MediaPlayer: " + e.getMessage());
        }
    }

    private void initializeMediaPlayer(String audioUrl) {
        if (isPreparing) return;
        isPreparing = true;
        btnListen.setEnabled(false);

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(audioUrl);

            mediaPlayer.setOnPreparedListener(mp -> {
                isPreparing = false;
                btnListen.setEnabled(true);
                try {
                    int duration = mediaPlayer.getDuration();
                    if (duration == -1) {
                        Log.d("MediaPlayerInfo", "Audio duration is unknown");
                        isSeekable = false;
                    }
                    mediaPlayer.start();
                    btnCheckResult.setEnabled(true);
                    startWaves();
                    isPlayingAnimation = true;
                    isPaused = false;
                    currentPosition = 0;
                } catch (IllegalStateException e) {
                    Log.e("MediaPlayerError", "Lỗi khi phát: " + e.getMessage());
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                isPreparing = false;
                btnListen.setEnabled(true);
                stopWaves();
                isPlayingAnimation = false;
                Log.e("MediaPlayerError", "Lỗi phát audio: " + what + ", " + extra);
                runOnUiThread(() -> Toast.makeText(this, "Không thể phát audio", Toast.LENGTH_SHORT).show());
                return true;
            });

            mediaPlayer.setOnInfoListener((mp, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
                    Log.d("MediaPlayer", "Stream không hỗ trợ seek");
                    isSeekable = false;
                }
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
            isPreparing = false;
            btnListen.setEnabled(true);
            Log.e("MediaPlayerError", "Lỗi khởi tạo: " + e.getMessage());
            runOnUiThread(() -> Toast.makeText(this, "Lỗi khi tải audio", Toast.LENGTH_SHORT).show());
        }
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
        isPreparing = false;
        initializeMediaPlayer(audioUrl);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                Log.e("MediaPlayerError", "Error in onDestroy: " + e.getMessage());
            }
            mediaPlayer = null;
        }
        isPreparing = false;
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
                bar.setBackgroundColor(Color.parseColor("#C4865E")); // Màu đã hoàn thành
            } else {
                bar.setBackgroundColor(Color.parseColor("#E0E0E0")); // Màu chưa hoàn thành
            }
            progressContainer.addView(bar);
        }
    }
}