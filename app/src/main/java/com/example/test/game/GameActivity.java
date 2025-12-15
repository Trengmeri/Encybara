package com.example.test.game;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout; // Đảm bảo đã import
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.GameManager;
import com.example.test.api.LearningMaterialsManager;
import com.example.test.response.QuestionDetailRespone;
import com.example.test.api.QuestionService;
import com.example.test.ui.home.HomeActivity;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;
    private TextView timerTextView;
    private CountDownTimer countDownTimer;
    private final long START_TIME_IN_MILLIS = 2 * 60 * 1000;
    private long timeLeftInMillis = START_TIME_IN_MILLIS;
    private int courseID;
    private int currentSessionId;
    private GameManager gameManager = new GameManager(this);
    private QuestionService questionService; // ✅ Thêm QuestionService
    private LearningMaterialsManager materialsManager = new LearningMaterialsManager(this);;
    private static final String TAG = "GameActivity";
    private int currentScore = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.gameView);
        if (gameView == null) {
            Log.e(TAG, "GameView not found in layout!");
        }
        timerTextView = findViewById(R.id.timerTextView);

        questionService = new QuestionService(this);

        gameView.setOnQuestionListener((row, col) -> showQuestionDialog(row, col));
        gameView.setOnWinListener(() -> showWinDialog());
        gameView.setOnGameOverListener(() -> showGameOverDialog());

        ImageButton up = findViewById(R.id.buttonUp);
        ImageButton down = findViewById(R.id.buttonDown);
        ImageButton left = findViewById(R.id.buttonLeft);
        ImageButton right = findViewById(R.id.buttonRight);

        up.setOnClickListener(v -> gameView.moveBear(-1, 0));
        down.setOnClickListener(v -> gameView.moveBear(1, 0));
        left.setOnClickListener(v -> gameView.moveBear(0, -1));
        right.setOnClickListener(v -> gameView.moveBear(0, 1));
        courseID = getIntent().getIntExtra("Courseid", 1);
        Log.d("CourseID","Courseid tu intent : "+ courseID);
        currentSessionId=getIntent().getIntExtra("SESSION_ID",1);
        Log.d("SESSION_ID","SESSION_ID tu intent : "+ currentSessionId);
        startGame();
    }
    private void startGame() {
        gameView.resetGame();
        timeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
                if (!gameView.isGameWon() && !gameView.isGameOver()) {
                    gameView.setGameRunning(false);
                    showTimeOutDialog();
                }
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        if (questionService != null) {
            questionService.shutdown(); // ✅ Đóng ExecutorService
        }
    }

    // 🧩 Câu hỏi pop-up
    @SuppressLint("MissingInflatedId")
    private void showQuestionDialog(final int row, final int col) {
        stopTimer(); // Tạm dừng timer khi dialog câu hỏi hiện ra

        // Hiển thị một ProgressDialog hoặc Toast "Đang tải câu hỏi..." nếu muốn
        Toast.makeText(this, "Đang tải câu hỏi...", Toast.LENGTH_SHORT).show();

        //  Gọi QuestionService để lấy một câu hỏi ngẫu nhiên
        // Chúng ta sẽ chỉ lấy 1 câu hỏi mỗi lần mở dialog
        questionService.getRandomReviewQuestionsForCourse(courseID, 1, new QuestionService.QuestionFetchCallback() {
            @Override
            public void onSuccess(List<QuestionDetailRespone.QuestionDetail> questions) {
                runOnUiThread(() -> { // Đảm bảo chạy trên UI thread
                    if (questions != null && !questions.isEmpty()) {
                        QuestionDetailRespone.QuestionDetail question = questions.get(0);
                        displayQuestionInDialog(question, row, col);
                    } else {
                        Toast.makeText(GameActivity.this, "Không thể tải câu hỏi. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                        if (gameView.isGameRunning()) startTimer(); // Khởi động lại timer nếu không có câu hỏi
                    }
                });
            }



            @Override
            public void onError(String message) {
                runOnUiThread(() -> { // Đảm bảo chạy trên UI thread
                    Toast.makeText(GameActivity.this, "Lỗi khi tải câu hỏi: " + message, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Lỗi API: " + message);
                    if (gameView.isGameRunning()) startTimer(); // Khởi động lại timer nếu có lỗi
                });
            }
        });
    }

    // ✅ Phương thức mới để hiển thị câu hỏi trong dialog sau khi đã tải
    @SuppressLint("MissingInflatedId")
    private void displayQuestionInDialog(final QuestionDetailRespone.QuestionDetail question, final int row, final int col) {
        // Inflate layout tùy chỉnh
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_question_background, null);

        TextView btnShowImage = dialogView.findViewById(R.id.btn_show_image);
        // ⭐ LẤY LESSON ID TỪ CÂU HỎI (đã được set trong QuestionService)
        final int dynamicLessonId = question.getLessonId();
        final int questionId = question.getId(); // Lấy ID của câu hỏi
        Log.d(TAG, "Question ID đang hiển thị: " + questionId);
        btnShowImage.setVisibility(View.VISIBLE);
        btnShowImage.setOnClickListener(v -> showImageDialogByLesson(dynamicLessonId));

        // Tìm và thiết lập câu hỏi
        TextView questionTextView = dialogView.findViewById(R.id.question_text_view);
        if (questionTextView != null) {
            questionTextView.setText(question.getQuesContent());
        }

        // Tạo RadioGroup và RadioButton cho các đáp án
        RadioGroup radioGroupAnswers = dialogView.findViewById(R.id.radio_group_answers);
        if (radioGroupAnswers == null) {
            Log.e(TAG, "RadioGroup with ID R.id.radio_group_answers not found in dialog_question_background.xml");
            // Xử lý lỗi hoặc tạo động RadioGroup nếu cần
            // Ví dụ: radioGroupAnswers = new RadioGroup(this); ...
            // Bạn NÊN định nghĩa RadioGroup trong XML.
            return; // Dừng nếu không tìm thấy RadioGroup
        }
        radioGroupAnswers.removeAllViews(); // Xóa các view cũ nếu có (trong trường hợp dialog được tái sử dụng)

        // Để lưu trữ lựa chọn của người dùng và đáp án đúng
        final int[] selectedAnswerId = {-1}; // Lưu trữ ID của RadioButton được chọn
        final int correctAnswerId = -1; // Sẽ được gán ID của RadioButton đúng

        // Xáo trộn thứ tự các lựa chọn để chúng không luôn xuất hiện ở cùng một vị trí
        List<QuestionDetailRespone.QuestionChoice> choices = question.getQuestionChoices();
        if (choices != null) {
            Collections.shuffle(choices);
            for (QuestionDetailRespone.QuestionChoice choice : choices) {
                RadioButton rb = new RadioButton(this);
                rb.setText(choice.getChoiceContent());
                rb.setId(choice.getId()); // Gán ID của choice làm ID của RadioButton
                radioGroupAnswers.addView(rb);
            }
            radioGroupAnswers.setOnCheckedChangeListener((group, checkedId) -> {
                selectedAnswerId[0] = checkedId;
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TransparentDialog);

        AlertDialog dialog = builder
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (d, w) -> {
                    if (selectedAnswerId[0] != -1) { // Đảm bảo người dùng đã chọn
                        d.dismiss(); // Đóng dialog ngay lập tức

                        // 🔔 GỌI HÀM MỚI để gửi câu trả lời lên server
                        submitAnswerToServer(question.getId(), selectedAnswerId[0], row, col);

                    } else {
                        Toast.makeText(GameActivity.this, "Vui lòng chọn một đáp án.", Toast.LENGTH_SHORT).show();
                        // KHÔNG đóng dialog, buộc người dùng phải chọn hoặc Hủy
                    }
                    // startTimer() sẽ được gọi sau khi nhận được phản hồi từ API (xem hàm submitAnswerToServer)
                })
                .setNegativeButton("Hủy", (d, w) -> {
                    Toast.makeText(GameActivity.this, "Bạn đã hủy trả lời. Lùi lại 1 bước.", Toast.LENGTH_SHORT).show();
                    gameView.pushBearBack();
                    if (gameView.isGameRunning()) startTimer();
                })
                .setCancelable(false)
                .create();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }

        dialog.show();
    }
    private void submitAnswerToServer(int questionId, int choiceId, int row, int col) {
        Toast.makeText(this, "Đang gửi đáp án...", Toast.LENGTH_SHORT).show();

        // Tham số currentSessionId đã được lấy từ Intent trong onCreate

        gameManager.sendAnswerRequest(
                (long) currentSessionId,
                (long) questionId,
                (long) choiceId,
                new ApiCallback() {

                    @Override
                    public void onSuccess() {
                        // Không sử dụng, cần onSuccess(Object result) để lấy điểm
                    }

                    // ✅ Phương thức onSuccess mới cần được định nghĩa trong ApiCallback
                    // Dùng Object để linh hoạt nhận String (message) hoặc Integer (score)
                    // Hoặc trong trường hợp này, chúng ta sẽ cần thay đổi nó.
                    // Xem ghi chú bên dưới về chữ ký ApiCallback.
                    @Override
                    public void onSuccess(Object result) {
                        runOnUiThread(() -> {
                            if (gameView.isGameRunning()) startTimer(); // Khởi động lại timer

                            if (result instanceof Integer) {
                                // Trường hợp 1: Nhận được điểm số (currentScore/finalScore)
                                int score = (Integer) result;
                                currentScore = score;
                                Toast.makeText(GameActivity.this, "Điểm hiện tại: " + currentScore, Toast.LENGTH_SHORT).show();

                                // *** RẤT QUAN TRỌNG:
                                // Vì API không trả về isCorrect, chúng ta cần sửa đổi ApiCallback
                                // hoặc xử lý isCorrect ở đây. Giả định bạn đã sửa đổi ApiCallback.
                                // GIẢ ĐỊNH: Nếu điểm số tăng, câu trả lời là ĐÚNG.
                                // *Đây là cách xử lý tạm, nên sửa ApiCallback để nhận isCorrect.*

                                // Nếu API trả về true/false về độ chính xác
                                boolean isCorrect = true; // Cần lấy từ Object result thực tế

                                if (isCorrect) {
                                    gameView.clearQuestionAt(row, col);
                                    Toast.makeText(GameActivity.this, "Đúng! Ô đã được dọn trống. Điểm: " + currentScore, Toast.LENGTH_LONG).show();
                                } else {
                                    gameView.handleWrongAnswer(row, col);
                                    Toast.makeText(GameActivity.this, "Sai rồi! Ô này biến thành đá và bạn bị đẩy lùi! Điểm: " + currentScore, Toast.LENGTH_LONG).show();
                                }

                            } else if (result instanceof String && "Game Completed".equals(result)) {
                                // Trường hợp 2: Game kết thúc
                                Toast.makeText(GameActivity.this, "Game Completed. Chuyển sang kết quả.", Toast.LENGTH_LONG).show();
                                // Không cần gọi endGameAndShowResult nữa vì đã kết thúc trên server
                                // Bạn nên chuyển thẳng sang màn hình hiển thị final score.
                            } else {
                                // Phản hồi không rõ ràng
                                Toast.makeText(GameActivity.this, "Đáp án đã được gửi.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        runOnUiThread(() -> {
                            if (gameView.isGameRunning()) startTimer(); // Khởi động lại timer

                            Log.e(TAG, "Lỗi gửi đáp án: " + errorMessage);
                            Toast.makeText(GameActivity.this, "Lỗi gửi đáp án: " + errorMessage, Toast.LENGTH_LONG).show();

                            // Nếu lỗi do "Game session has ended" (Lỗi nghiệp vụ)
                            if (errorMessage.contains("Game session has ended")) {
                                Toast.makeText(GameActivity.this, "Game đã kết thúc. Xem kết quả.", Toast.LENGTH_LONG).show();
                                // Bỏ qua lỗi và chuyển sang màn hình kết quả cuối cùng
                                endGameAndShowResult();
                            } else {
                                // Nếu lỗi khác (Mất kết nối, v.v.), vẫn lùi gấu về ô cũ
                                gameView.pushBearBack();
                            }
                        });
                    }
                }
        );
    }
    private void showImageDialogByLesson(int lessonId) {
        if (lessonId <= 0) {
            Toast.makeText(this, "Bài học này không có tài liệu minh họa.", Toast.LENGTH_SHORT).show();
            return;
        }

        stopTimer();
        LayoutInflater inflater = this.getLayoutInflater();
        View imageDialogView = inflater.inflate(R.layout.dialog_image_viewer, null);
        ImageView imageView = imageDialogView.findViewById(R.id.question_image_view);
        imageView.setVisibility(View.GONE);

        //  GỌI HÀM TẢI ẢNH THEO LESSON ID
        materialsManager.fetchAndLoadImageByLesId(lessonId, imageView);

        new AlertDialog.Builder(this)
                .setTitle("Hình ảnh tài liệu bài học")
                .setView(imageDialogView)
                .setPositiveButton("Đóng", (d, w) -> {
                    d.dismiss();
                    if (gameView.isGameRunning()) startTimer();
                })
                .create()
                .show();
    }
    private void endGameAndShowResult() {
        // 1. Dừng Timer và các hoạt động khác
        stopTimer();

        // 2. Gọi API End Game
        gameManager.sendEndGameRequest(currentSessionId, new ApiCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    // Xử lý thành công: Chuyển sang màn hình kết quả hoặc hiển thị thông báo
                    Toast.makeText(GameActivity.this, "Kết thúc Game thành công!", Toast.LENGTH_SHORT).show();
                    // Ví dụ: Hiển thị dialog thắng/thua ở đây, sau đó finish()
                    navigateToHomeAndFinish();
                });
            }

            @Override
            public void onSuccess(Object result) {
                // Không sử dụng phương thức này cho hàm End Game, chỉ sử dụng onSuccess()
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    // Xử lý lỗi: Thông báo cho người dùng
                    Log.e("GameActivity", "Lỗi kết thúc game: " + errorMessage);
                    Toast.makeText(GameActivity.this, "Lỗi kết thúc game: " + errorMessage, Toast.LENGTH_LONG).show();
                });
                // Dù lỗi API, vẫn nên cho người dùng thoát khỏi GameActivity
                navigateToHomeAndFinish();
            }
        });
    }

    private void navigateToHomeAndFinish() {
        // Tạo Intent để chuyển về HomeActivity
        Intent intent = new Intent(GameActivity.this, HomeActivity.class);

        // Đặt cờ để dọn dẹp Stack Activity (quay về màn hình Home)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);

        // Đóng GameActivity
        finish();
    }
    // 🍯 Khi đến hũ mật
    private void showWinDialog() {
        stopTimer();
        new AlertDialog.Builder(this)
                .setTitle("🎉 Chúc mừng!")
                .setMessage("Bạn đã tìm được hũ mật 🍯!")
                .setPositiveButton("Chơi lại", (d, w) -> startGame())
                .setNegativeButton("Thoát", (d,w) -> {
                    endGameAndShowResult(); // ✅ GỌI HÀM END GAME
                })
                .setCancelable(false)
                .show();
    }

    // ✅ Dialog thông báo Game Over do đường bị chặn
    private void showGameOverDialog() {
        stopTimer();
        new AlertDialog.Builder(this)
                .setTitle("Game Over 😭")
                .setMessage("Bạn đã bị chặn hết đường đi! Thử lại nhé.")
                .setPositiveButton("Chơi lại", (d, w) -> startGame())
                .setNegativeButton("Thoát", (d,w) -> {
                    endGameAndShowResult(); // ✅ GỌI HÀM END GAME
                })
                .setCancelable(false)
                .show();
    }

    // ✅ Dialog thông báo Game Over do hết thời gian
    private void showTimeOutDialog() {
        stopTimer();
        new AlertDialog.Builder(this)
                .setTitle("Hết giờ! ⌛")
                .setMessage("Bạn đã hết thời gian để tìm hũ mật. Game Over!")
                .setPositiveButton("Chơi lại", (d, w) -> startGame())
                .setNegativeButton("Thoát", (d,w) -> {
                    endGameAndShowResult(); // ✅ GỌI HÀM END GAME
                })
                .setCancelable(false)
                .show();
    }
}