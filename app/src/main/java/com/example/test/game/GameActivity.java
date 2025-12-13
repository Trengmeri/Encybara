package com.example.test.game;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.LinearLayout; // Đảm bảo đã import
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;
import com.example.test.response.QuestionDetailRespone;
import com.example.test.api.QuestionService;

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

    private QuestionService questionService; // ✅ Thêm QuestionService
    //private final int COURSE_ID_FOR_REVIEW = 11; // ✅ ID khóa học cố định để lấy câu hỏi. Thay đổi nếu cần.
    private static final String TAG = "GameActivity";

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
//        courseID = getIntent().getIntExtra("CourseID", 1);
//        Log.d("CourseID","CourseID tu intent : "+ courseID);
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

        // ✅ Gọi QuestionService để lấy một câu hỏi ngẫu nhiên
        // Chúng ta sẽ chỉ lấy 1 câu hỏi mỗi lần mở dialog
        questionService.getRandomReviewQuestionsForCourse(11, 1, new QuestionService.QuestionFetchCallback() {
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
            Collections.shuffle(choices); // ✅ Xáo trộn thứ tự lựa chọn
            for (int i = 0; i < choices.size(); i++) {
                QuestionDetailRespone.QuestionChoice choice = choices.get(i);
                RadioButton rb = new RadioButton(this);
                rb.setText(choice.getChoiceContent());
                rb.setId(choice.getId()); // ✅ Gán ID của choice làm ID của RadioButton
                radioGroupAnswers.addView(rb);

                if (choice.isChoiceKey()) {
                    // correctAnswerId = i; // Nếu bạn muốn lưu trữ index
                    // Hoặc lưu trữ ID của lựa chọn đúng
                    final int finalCorrectAnswerId = choice.getId();
                    radioGroupAnswers.setOnCheckedChangeListener((group, checkedId) -> {
                        selectedAnswerId[0] = checkedId;
                    });
                }
            }
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TransparentDialog);

        AlertDialog dialog = builder
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (d, w) -> {
                    if (selectedAnswerId[0] != -1) { // Đảm bảo người dùng đã chọn
                        boolean isCorrect = false;
                        for (QuestionDetailRespone.QuestionChoice choice : question.getQuestionChoices()) {
                            if (choice.getId() == selectedAnswerId[0] && choice.isChoiceKey()) {
                                isCorrect = true;
                                break;
                            }
                        }

                        if (isCorrect) {
                            gameView.clearQuestionAt(row, col);
                            Toast.makeText(GameActivity.this, "Đúng! Ô đã được dọn trống.", Toast.LENGTH_SHORT).show();
                        } else {
                            gameView.handleWrongAnswer(row, col);
                            Toast.makeText(GameActivity.this, "Sai rồi! Ô này biến thành đá và bạn bị đẩy lùi!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(GameActivity.this, "Vui lòng chọn một đáp án.", Toast.LENGTH_SHORT).show();
                        // Nếu không chọn, có thể cho phép dialog đóng hoặc buộc chọn
                        // Hiện tại, dialog sẽ đóng và không xử lý câu trả lời.
                        // Bạn có thể cân nhắc gọi lại showQuestionDialog để buộc chọn.
                    }
                    if (gameView.isGameRunning()) startTimer();
                })
                .setNegativeButton("Hủy", (d, w) -> {
                    Toast.makeText(GameActivity.this, "Bạn đã hủy trả lời.", Toast.LENGTH_SHORT).show();
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


    // 🍯 Khi đến hũ mật
    private void showWinDialog() {
        stopTimer();
        new AlertDialog.Builder(this)
                .setTitle("🎉 Chúc mừng!")
                .setMessage("Bạn đã tìm được hũ mật 🍯!")
                .setPositiveButton("Chơi lại", (d, w) -> startGame())
                .setNegativeButton("Thoát", (d,w) -> finish())
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
                .setNegativeButton("Thoát", (d,w) -> finish())
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
                .setNegativeButton("Thoát", (d,w) -> finish())
                .setCancelable(false)
                .show();
    }
}