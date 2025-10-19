package com.example.test.ui.entrance_test;

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
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.BaseActivity;
import com.example.test.PopupHelper;
import com.example.test.R;
import com.example.test.adapter.ChoiceAdapter;
import com.example.test.api.ApiCallback;
import com.example.test.api.ApiService;
import com.example.test.api.LearningMaterialsManager;
import com.example.test.api.LessonManager;
import com.example.test.api.MediaManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Answer;
import com.example.test.model.Course;
import com.example.test.model.Discussion;
import com.example.test.model.EvaluationResult;
import com.example.test.model.Lesson;
import com.example.test.model.MediaFile;
import com.example.test.model.Question;
import com.example.test.model.QuestionChoice;
import com.example.test.model.Result;
import com.example.test.ui.question_data.PointResultCourseActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ListeningPick1Activity extends BaseActivity {
    String correctAnswers;
    private MediaPlayer mediaPlayer;
    private EditText etAnswer;
    private List<Integer> questionIds;
    private  String questype, audioUrl;
    private List<String> userAnswers = new ArrayList<>();
    private int currentStep = 0; // Bước hiện tại (bắt đầu từ 0)
    private int totalSteps; // Tổng số bước trong thanh tiến trình
    private int answerIds;
    ImageView imgLessonMaterial,btnReplay;
    TextView tvQuestion;
    LinearLayout progressBar;
    Button btnCheckResult;
    private LinearLayout btnListen;
    LearningMaterialsManager materialsManager = new LearningMaterialsManager(this);
    // Thêm các View cho vòng tròn sóng âm
    private View wave1, wave2, wave3;
    private ObjectAnimator animator1ScaleX, animator1ScaleY, animator1Alpha;
    private ObjectAnimator animator2ScaleX, animator2ScaleY, animator2Alpha;
    private ObjectAnimator animator3ScaleX, animator3ScaleY, animator3Alpha;
    private boolean isPlayingAnimation = false;
    private int currentPosition = 0; // Lưu vị trí hiện tại của âm thanh
    private boolean isPaused = false; // Trạng thái tạm dừng của âm thanh
    private boolean isMediaPrepared = false;

    QuestionManager quesManager = new QuestionManager(this);
    LessonManager lesManager = new LessonManager();
    ResultManager resultManager = new ResultManager(this);
    MediaManager mediaManager = new MediaManager(this);
    private RecyclerView recyclerViewChoices;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listening_choice);

        btnListen = findViewById(R.id.btnListen1);
        btnCheckResult = findViewById(R.id.btnCheckResult);
        tvQuestion = findViewById(R.id.tvQuestion);
        recyclerViewChoices = findViewById(R.id.recyclerViewChoices);
        imgLessonMaterial = findViewById(R.id.imgLessonMaterial);
        btnReplay=findViewById(R.id.btnReplay1);
        // Ánh xạ các vòng tròn sóng âm
        wave1 = findViewById(R.id.wave_1);
        wave2 = findViewById(R.id.wave_2);
        wave3 = findViewById(R.id.wave_3);

        // Khởi tạo các animator nhưng không chạy ngay
        setupWaveAnimators();
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
        int lessonId = 3;
        int enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        fetchLessonAndQuestions(lessonId);

        progressBar = findViewById(R.id.progressBar); // Ánh xạ ProgressBar
        createProgressBars(totalSteps, currentStep); // Cập nhật thanh tiến trình mỗi lần chuyển câu

        btnReplay.setOnClickListener(v -> replayAudio());
        btnListen.setOnClickListener(v -> {
                                            Log.d("AudioTest", "Đã click vào nút nghe");
                                            toggleAudioAndAnimation();
                                        });

            btnCheckResult.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pauseAudio();
                stopWaves();
                isPlayingAnimation = false;
            }
            Log.d("ListeningPick1Activity", "User Answers: " + userAnswers);
            if (userAnswers.isEmpty()) {
                Toast.makeText(ListeningPick1Activity.this, "Vui lòng trả lời câu hỏi!", Toast.LENGTH_SHORT).show();
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
                        Log.e("ListeningPick1Activity", "Câu trả lời đã được lưu: " + answerContent);
                        // Hiển thị popup
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(ListeningPick1Activity.this, questype, answerContent, correctAnswers, null, null, null, () -> {
                                currentStep++; // Tăng currentStep

                                // Kiểm tra nếu hoàn thành
                                if (currentStep < totalSteps) {
                                    fetchQuestion(questionIds.get(currentStep)); // Lấy câu hỏi tiếp theo
                                    createProgressBars(totalSteps, currentStep); // Cập nhật thanh tiến trình mỗi lần chuyển câu

                                } else {
                                    Intent intent = new Intent(ListeningPick1Activity.this, SpeakingActivity.class);
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
                                    Log.e("ListeningPick1Activity", "Answer ID từ API: " + answer.getId());
                                    if (answerIds != 0) {
                                        QuestionManager.gradeAnswer(answerIds, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                Log.e("ListeningPick1Activity", "Lỗi khi chấm điểm: " + e.getMessage());
                                            }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    Log.e("ListeningPick1Activity", "Chấm điểm thành công cho Answer ID: " + answerIds);
                                                } else {
                                                    Log.e("ListeningPick1Activity", "Lỗi từ server: " + response.code());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("ListeningPick1Activity", "Bài học không có câu trl.");
                                    }
                                } else {
                                    Log.e("ListeningPick1Activity", "Không nhận được câu trả lời từ API.");
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
                        materialsManager.fetchAudioByLesId(lessonId,  new ApiCallback<String>() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onSuccess(String result) {
                                runOnUiThread(() -> { // Sử dụng runOnUiThread ở đây
                                    if (result!= null) {
                                        audioUrl = result;
//                                        btnListen.setOnClickListener(v -> {
//                                            Log.d("AudioTest", "Đã click vào nút nghe");
//                                            toggleAudioAndAnimation();
//                                        });

                                    }
                                });
                            }

                            @Override
                            public void onFailure(String errorMessage) {

                            }
                        });

                        materialsManager.fetchAndLoadImageByLesId(lessonId, imgLessonMaterial);
                    } else {
                        Log.e("ListeningPick1Activity", "Bài học không có câu hỏi.");
                    }
                } else {
                    Log.e("ListeningPick1Activity", "Bài học trả về là null.");
                }
            }



            @Override
            public void onFailure(String errorMessage) {
                Log.e("ListeningPick1Activity", errorMessage);
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
                    questype = question.getQuesType();
                    // Lấy nội dung câu hỏi
                    String questionContent = question.getQuesContent();
                    Log.d("ListeningPick1Activity", "Câu hỏi: " + questionContent);

                    List<QuestionChoice> choices = question.getQuestionChoices();
                    if (choices != null && !choices.isEmpty()) {
                        runOnUiThread(() -> {
                            tvQuestion.setText(questionContent);
                            userAnswers.clear();
                            ChoiceAdapter choiceAdapter = new ChoiceAdapter(ListeningPick1Activity.this, choices, userAnswers);
                            recyclerViewChoices.setAdapter(choiceAdapter);
                            for (QuestionChoice choice : choices) {
                                if (choice.isChoiceKey()) {
                                    correctAnswers=choice.getChoiceContent();
                                }
                            }
                        });
                    } else {
                        Log.e("ListeningPick1Activity", "Câu hỏi không có lựa chọn.");
                    }
                } else {
                    Log.e("ListeningPick1Activity", "Câu hỏi trả về là null.");
                }
            }



            @Override
            public void onFailure(String errorMessage) {
                Log.e("ListeningPick1Activity", errorMessage);
            }



            @Override
            public void onSuccess() {}
        });
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
                // Tiếp tục từ vị trí tạm dừng
                mediaPlayer.start();
                startWaves();
                isPlayingAnimation = true;
                isPaused = false;
            } else {
                // Phát từ đầu nếu chưa có mediaPlayer hoặc đã hoàn thành
                playAudio(audioUrl);
                startWaves();
                isPlayingAnimation = true;
            }
        } else {
            pauseAudio();
            stopWaves();
            isPlayingAnimation = false;
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

    private void fetchAudioUrl(int questionId) {

        // Gọi phương thức fetchAudioUrl từ ApiManager
        mediaManager.fetchMediaByQuesId(questionId, new ApiCallback<MediaFile>() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(MediaFile mediaFile) {
                runOnUiThread(() -> { // Sử dụng runOnUiThread ở đây
                    if (mediaFile!= null) {
                        btnListen.setOnClickListener(v -> {
                            String modifiedLink = mediaFile.getMaterLink().replace("0.0.0.0", "14.225.198.3");
                            playAudio(modifiedLink);
                        });

                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                // Hiển thị thông báo lỗi nếu có
                Log.e("media",errorMessage);
            }
        });
    }
    private void playAudio(String audioUrl) {
        if (audioUrl == null || audioUrl.isEmpty()) {
            Log.e("MediaPlayerError", "Audio URL is null or empty");
            return;
        }

        try {
            if (mediaPlayer == null) {
                initializeMediaPlayer(audioUrl);
            } else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(currentPosition);
                mediaPlayer.start();
            }
        } catch (IllegalStateException e) {
            Log.e("MediaPlayerError", "IllegalStateException: " + e.getMessage());
            resetMediaPlayer(audioUrl);
        }
    }
    // Phát lại âm thanh từ đầu
    private void replayAudio() {
        if (audioUrl == null) return;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        stopWaves();
        resetWaveViews();
        currentPosition = 0;
        initializeMediaPlayer(audioUrl);
        startWaves();
        isPlayingAnimation = true;
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
                mediaPlayer.start();
                btnCheckResult.setEnabled(true);
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("MediaPlayerError", "Error occurred: what=" + what + ", extra=" + extra);
                resetMediaPlayer(audioUrl);
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
        }
    }

    private void resetMediaPlayer(String audioUrl) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                Log.e("MediaPlayerError", "Error in onDestroy: " + e.getMessage());
            }
            mediaPlayer = null;
        }
        cancelAnimators();
    }
    private void cancelAnimators() {
        ObjectAnimator[] animators = {animator1ScaleX, animator1ScaleY, animator1Alpha,
                animator2ScaleX, animator2ScaleY, animator2Alpha,
                animator3ScaleX, animator3ScaleY, animator3Alpha};
        for (ObjectAnimator animator : animators) {
            if (animator != null) animator.cancel();
        }
    }
}
