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

import com.example.test.PopupHelper;
import com.example.test.R;
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

public class ListeningActivity extends AppCompatActivity {
    String correctAnswer;
    private MediaPlayer mediaPlayer;
    private EditText etAnswer;
    private List<Integer> questionIds;
    private  String questype, audioUrl;
    private String userAnswer;
    private int currentStep = 0; // Bước hiện tại (bắt đầu từ 0)
    private int totalSteps; // Tổng số bước trong thanh tiến trình
    private int answerIds;
    //ImageView btnListen;
    TextView tvQuestion;
    LinearLayout progressBar;
    Button btnCheckResult;
    ImageView imgLessonMaterial,btnReplay;
    private LinearLayout btnListen;

    private int currentPosition = 0; // Lưu vị trí hiện tại của âm thanh
    private boolean isPaused = false; // Trạng thái tạm dừng của âm thanh

    QuestionManager quesManager = new QuestionManager(this);
    LessonManager lesManager = new LessonManager();
    ResultManager resultManager = new ResultManager(this);
    MediaManager mediaManager = new MediaManager(this);
    LearningMaterialsManager materialsManager = new LearningMaterialsManager(this);
    // Thêm các View cho vòng tròn sóng âm
    private View wave1, wave2, wave3;
    private ObjectAnimator animator1ScaleX, animator1ScaleY, animator1Alpha;
    private ObjectAnimator animator2ScaleX, animator2ScaleY, animator2Alpha;
    private ObjectAnimator animator3ScaleX, animator3ScaleY, animator3Alpha;
    private boolean isPlayingAnimation = false;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listening_question);

        btnListen = findViewById(R.id.btnListen);
        btnCheckResult = findViewById(R.id.btnCheckResult);
        tvQuestion = findViewById(R.id.tvQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        imgLessonMaterial = findViewById(R.id.imgLessonMaterial);
        btnReplay=findViewById(R.id.btnReplay);
        // Ánh xạ các vòng tròn sóng âm
        wave1 = findViewById(R.id.wave_1);
        wave2 = findViewById(R.id.wave_2);
        wave3 = findViewById(R.id.wave_3);

        // Khởi tạo các animator nhưng không chạy ngay
        setupWaveAnimators();
        int lessonId = 4;
        int enrollmentId = getIntent().getIntExtra("enrollmentId", 1);
        fetchLessonAndQuestions(lessonId);

        progressBar = findViewById(R.id.progressBar); // Ánh xạ ProgressBar
        createProgressBars(totalSteps, currentStep); // Tạo progress bar dựa trên số câu hỏi thực tế
        btnReplay.setOnClickListener(v -> {
            replayAudio();
        });

        btnCheckResult.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();  // Dừng âm thanh nếu đang phát
                currentPosition = mediaPlayer.getCurrentPosition();
                isPaused = true;
               // mediaPlayer.release();
                //mediaPlayer = null;
                stopWaves(); // Dừng hoạt hình khi dừng âm thanh
                isPlayingAnimation = false;
            }
            String userAnswer = etAnswer.getText().toString().trim();

            Log.d("ListeningActivity", "User Answers: " + userAnswer);
            if (userAnswer.isEmpty()) {
                Toast.makeText(ListeningActivity.this, "Vui lòng trả lời câu hỏi!", Toast.LENGTH_SHORT).show();
            } else {
                String answerContent = userAnswer;
                // Lưu câu trả lời của người dùng
                quesManager.saveUserAnswer(questionIds.get(currentStep), answerContent, 0,null,enrollmentId, new ApiCallback() {

                    @Override
                    public void onSuccess() {
                        Log.e("ListeningActivity", "Câu trả lời đã được lưu: " + answerContent);
                        // Hiển thị popup
                        runOnUiThread(() -> {
                            PopupHelper.showResultPopup(ListeningActivity.this, questype, userAnswer, correctAnswer, null, null, null, () -> {
                                currentStep++; // Tăng currentStep
                                etAnswer.setText("");
                                // Kiểm tra nếu hoàn thành
                                if (currentStep < totalSteps) {
                                    fetchQuestion(questionIds.get(currentStep)); // Lấy câu hỏi tiếp theo
                                    createProgressBars(totalSteps, currentStep); // Tạo progress bar dựa trên số câu hỏi thực tế
                                } else {
                                    Intent intent = new Intent(ListeningActivity.this, ListeningPick1Activity.class);
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
                                    Log.e("ListeningActivity", "Answer ID từ API: " + answer.getId());
                                    if (answerIds != 0) {
                                        QuestionManager.gradeAnswer(answerIds, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                Log.e("ListeningActivity", "Lỗi khi chấm điểm: " + e.getMessage());
                                            }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    Log.e("ListeningActivity", "Chấm điểm thành công cho Answer ID: " + answerIds);
                                                } else {
                                                    Log.e("ListeningActivity", "Lỗi từ server: " + response.code());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("ListeningActivity", "Bài học không có câu trl.");
                                    }
                                } else {
                                    Log.e("ListeningActivity", "Không nhận được câu trả lời từ API.");
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
                        fetchQuestion(questionIds.get(currentStep));
                        materialsManager.fetchAudioByLesId(lessonId, new ApiCallback<String>() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onSuccess(String result) {
                                runOnUiThread(() -> { // Sử dụng runOnUiThread ở đây
                                    if (result!= null) {
                                        audioUrl = result;
                                        btnListen.setOnClickListener(v -> {
                                            Log.d("AudioTest", "Đã click vào nút nghe");
                                            toggleAudioAndAnimation();
                                        });

                                    }
                                });
                            }

                            @Override
                            public void onFailure(String errorMessage) {

                            }
                        });
                        materialsManager.fetchAndLoadImageByLesId(lessonId, imgLessonMaterial);
//                        fetchAudioUrl(questionIds.get(currentStep));
                    } else {
                        Log.e("Pick1Activity", "Bài học không có câu hỏi.");
                    }
                } else {
                    Log.e("Pick1Activity", "Bài học trả về là null.");
                }
            }



            @Override
            public void onFailure(String errorMessage) {
                Log.e("Pick1Activity", errorMessage);
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
                    runOnUiThread(() -> {
                        tvQuestion.setText(question.getQuesContent());
                        materialsManager.fetchAndLoadImage(questionId, imgLessonMaterial);
                        List<QuestionChoice> choices = question.getQuestionChoices();
                        for (QuestionChoice choice : choices) {
                            if (choice.isChoiceKey()) {
                                correctAnswer = choice.getChoiceContent();
                            }
                        }
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
            // Bắt đầu phát âm thanh
            playAudio(audioUrl);
            // Bắt đầu hoạt hình
            startWaves();
            isPlayingAnimation = true;
        } else {
            // Dừng âm thanh
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                currentPosition = mediaPlayer.getCurrentPosition(); // Lưu vị trí hiện tại
                isPaused = true;
                //mediaPlayer.release();
                //mediaPlayer = null;
            }
            // Dừng hoạt hình
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
                bar.setBackgroundColor(Color.parseColor("#C4865E")); // Màu đã hoàn thành
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
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(audioUrl);
                mediaPlayer.setOnPreparedListener(mp -> {
                    if (isPaused) {
                        mediaPlayer.seekTo(currentPosition); // Tiếp tục từ vị trí đã dừng
                    }
                    mediaPlayer.start();
                    btnCheckResult.setEnabled(true);  // Kích hoạt lại nút CheckResult
                });

                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e("MediaPlayerError", "Error occurred: what=" + what + ", extra=" + extra);
                    return true;
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    stopWaves();
                    isPlayingAnimation = false;
                    currentPosition = 0; // Đặt lại vị trí khi âm thanh kết thúc
                    isPaused = false;
                });
                mediaPlayer.prepareAsync();
            } else if (isPaused) {
                mediaPlayer.seekTo(currentPosition); // Tiếp tục từ vị trí đã dừng
                mediaPlayer.start();
                isPaused = false;
            }
        } catch (IOException | IllegalArgumentException e) {
            Log.e("MediaPlayerError", e.getMessage());
        }
    }

    // Phát lại âm thanh từ đầu
    private void replayAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0); // Đặt vị trí về đầu
            mediaPlayer.start(); // Phát lại từ đầu
            currentPosition = 0; // Đặt lại vị trí hiện tại
            isPaused = false; // Đặt lại trạng thái tạm dừng

            // Dừng hoạt hình hiện tại
            stopWaves();

            // Đặt lại trạng thái của các vòng tròn sóng âm về giá trị ban đầu
            wave1.setScaleX(1f);
            wave1.setScaleY(1f);
            wave1.setAlpha(0.5f);

            wave2.setScaleX(1f);
            wave2.setScaleY(1f);
            wave2.setAlpha(0.3f);

            wave3.setScaleX(1f);
            wave3.setScaleY(1f);
            wave3.setAlpha(0.1f);

            // Chạy lại hoạt hình từ đầu
            startWaves();
            isPlayingAnimation = true;
        } else {
            // Nếu mediaPlayer chưa được khởi tạo, gọi playAudio để khởi tạo và phát từ đầu
            playAudio(audioUrl);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        // Hủy các animator để tránh rò rỉ bộ nhớ
        if (animator1ScaleX != null) animator1ScaleX.cancel();
        if (animator1ScaleY != null) animator1ScaleY.cancel();
        if (animator1Alpha != null) animator1Alpha.cancel();
        if (animator2ScaleX != null) animator2ScaleX.cancel();
        if (animator2ScaleY != null) animator2ScaleY.cancel();
        if (animator2Alpha != null) animator2Alpha.cancel();
        if (animator3ScaleX != null) animator3ScaleX.cancel();
        if (animator3ScaleY != null) animator3ScaleY.cancel();
        if (animator3Alpha != null) animator3Alpha.cancel();
    }
}
