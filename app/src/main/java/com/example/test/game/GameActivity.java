package com.example.test.game;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView; // ✅ Thêm TextView
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;

import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;
    private TextView timerTextView; // ✅ TextView để hiển thị thời gian
    private CountDownTimer countDownTimer; // ✅ Đối tượng CountDownTimer
    private final long START_TIME_IN_MILLIS = 2 * 60 * 1000; // 2 phút = 120,000 milliseconds
    private long timeLeftInMillis = START_TIME_IN_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.gameView);
        if (gameView == null) {
            Log.e("MainActivity", "GameView not found in layout!");
            // Đây là một lỗi nghiêm trọng, SurfaceView sẽ không bao giờ hiển thị.
        }
        timerTextView = findViewById(R.id.timerTextView); // ✅ Gán TextView từ layout

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

        startGame(); // ✅ Bắt đầu game và timer khi activity được tạo
    }

    private void startGame() {
        gameView.resetGame(); // Đặt lại GameView về trạng thái ban đầu
        timeLeftInMillis = START_TIME_IN_MILLIS; // Reset thời gian
        updateCountDownText(); // Cập nhật hiển thị thời gian
        startTimer(); // Bắt đầu đếm ngược
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Hủy timer cũ nếu có
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
                if (!gameView.isGameWon() && !gameView.isGameOver()) { // ✅ Nếu hết giờ mà chưa thắng/thua do đường chặn
                    gameView.setGameRunning(false); // Dừng game trong GameView
                    showTimeOutDialog(); // Hiển thị dialog thua do hết giờ
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
        stopTimer(); // ✅ Hủy timer khi Activity bị hủy để tránh memory leaks
    }

    // 🧩 Câu hỏi pop-up
    @SuppressLint("MissingInflatedId")
    private void showQuestionDialog(final int row, final int col) {
        stopTimer(); // Tạm dừng timer khi dialog câu hỏi hiện ra

        final String questionText = "Từ 'bear' có nghĩa là gì?";
        final String[] answers = {"Con gấu", "Con ong", "Mang/Chịu đựng", "Trần trụi"};
        final int correctAnswerIndex = 0;

        // Inflate layout tùy chỉnh
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_question_background, null);

        // Tìm và thiết lập câu hỏi
        TextView questionTextView = dialogView.findViewById(R.id.question_text_view); // Cần thêm ID này vào layout
        if (questionTextView != null) {
            questionTextView.setText(questionText);
        }

        // Tạo RadioGroup và RadioButton cho các đáp án
        RadioGroup radioGroupAnswers = dialogView.findViewById(R.id.radio_group_answers); // Cần thêm ID này vào layout
        if (radioGroupAnswers == null) {
            radioGroupAnswers = new RadioGroup(this);
            radioGroupAnswers.setId(R.id.radio_group_answers); // Gán ID nếu tạo động
            // Thêm radioGroupAnswers vào dialogView nếu nó chưa tồn tại (ví dụ, thêm vào LinearLayout chính)
            // Đây là phần phức tạp hơn nếu bạn muốn hoàn toàn động.
            // Tốt nhất là định nghĩa RadioGroup trong XML của dialog_question_background.xml
            // Ví dụ: <RadioGroup android:id="@+id/radio_group_answers" ... />
            ((LinearLayout) dialogView.findViewById(R.id.dialog_content_container)).addView(radioGroupAnswers); // Giả sử bạn có container
        }

        // Để lưu trữ lựa chọn của người dùng
        final int[] selectedAnswerIndex = {-1}; // Khởi tạo với -1

        // Thêm RadioButton vào RadioGroup
        for (int i = 0; i < answers.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(answers[i]);
            rb.setId(i); // Gán ID cho mỗi RadioButton bằng chỉ số của nó
            radioGroupAnswers.addView(rb);
        }

        // Lắng nghe sự kiện chọn đáp án
        radioGroupAnswers.setOnCheckedChangeListener((group, checkedId) -> {
            selectedAnswerIndex[0] = checkedId; // checkedId chính là ID bạn gán cho RadioButton
        });


        new AlertDialog.Builder(this)
                .setView(dialogView) // Đặt layout tùy chỉnh của bạn vào đây
                // .setTitle("Câu hỏi tiếng Anh 🧠") // Không dùng setTitle nữa vì layout đã có hình gấu
                .setPositiveButton("Xác nhận", (d, w) -> {
                    if (selectedAnswerIndex[0] == correctAnswerIndex) {
                        gameView.clearQuestionAt(row, col);
                        Toast.makeText(GameActivity.this, "Đúng! Ô đã được dọn trống.", Toast.LENGTH_SHORT).show();
                    } else {
                        gameView.handleWrongAnswer(row, col);
                        Toast.makeText(GameActivity.this, "Sai rồi! Ô này biến thành đá và bạn bị đẩy lùi!", Toast.LENGTH_LONG).show();
                    }

                    if (gameView.isGameRunning()) {
                        startTimer();
                    } else {
                        stopTimer();
                    }
                })
                .setNegativeButton("Hủy", (d, w) -> {
                    Toast.makeText(GameActivity.this, "Bạn đã hủy trả lời.", Toast.LENGTH_SHORT).show();
                    if (gameView.isGameRunning()) {
                        startTimer();
                    }
                })
                .setCancelable(false)
                .show();
    }

    // 🍯 Khi đến hũ mật
    private void showWinDialog() {
        stopTimer(); // ✅ Dừng timer khi thắng
        new AlertDialog.Builder(this)
                .setTitle("🎉 Chúc mừng!")
                .setMessage("Bạn đã tìm được hũ mật 🍯!")
                .setPositiveButton("Chơi lại", (d, w) -> startGame()) // ✅ Gọi startGame để reset và bắt đầu timer
                .setNegativeButton("Thoát", (d,w) -> finish())
                .setCancelable(false)
                .show();
    }

    // ✅ Dialog thông báo Game Over do đường bị chặn
    private void showGameOverDialog() {
        stopTimer(); // ✅ Dừng timer
        new AlertDialog.Builder(this)
                .setTitle("Game Over 😭")
                .setMessage("Bạn đã bị chặn hết đường đi! Thử lại nhé.")
                .setPositiveButton("Chơi lại", (d, w) -> startGame()) // ✅ Gọi startGame
                .setNegativeButton("Thoát", (d,w) -> finish())
                .setCancelable(false)
                .show();
    }

    // ✅ Dialog thông báo Game Over do hết thời gian
    private void showTimeOutDialog() {
        stopTimer(); // Đảm bảo timer đã dừng
        new AlertDialog.Builder(this)
                .setTitle("Hết giờ! ⌛")
                .setMessage("Bạn đã hết thời gian để tìm hũ mật. Game Over!")
                .setPositiveButton("Chơi lại", (d, w) -> startGame()) // ✅ Gọi startGame
                .setNegativeButton("Thoát", (d,w) -> finish())
                .setCancelable(false)
                .show();
    }
}