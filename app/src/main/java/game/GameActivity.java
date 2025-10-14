package game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageButton;
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
    private void showQuestionDialog(int row, int col) {
        // Tạm dừng timer khi dialog câu hỏi hiện ra
        stopTimer(); // ✅ Tạm dừng timer
        new AlertDialog.Builder(this)
                .setTitle("Câu hỏi tiếng Anh 🧠")
                .setMessage("Từ 'bear' có nghĩa là gì?")
                .setPositiveButton("Con gấu", (d, w) -> {
                    gameView.clearQuestionAt(row, col);
                    Toast.makeText(GameActivity.this, "Đúng! Ô đã được dọn trống.", Toast.LENGTH_SHORT).show();
                    if (gameView.isGameRunning()) { // ✅ Chỉ khởi động lại timer nếu game vẫn đang chạy
                        startTimer();
                    }
                })
                .setNegativeButton("Con ong", (d, w) -> {
                    gameView.handleWrongAnswer(row, col);
                    Toast.makeText(GameActivity.this, "Sai rồi! Ô này biến thành đá và bạn bị đẩy lùi!", Toast.LENGTH_LONG).show();
                    if (gameView.isGameRunning()) { // ✅ Chỉ khởi động lại timer nếu game vẫn đang chạy
                        startTimer();
                    } else {
                        // Nếu handleWrongAnswer dẫn đến game over, timer sẽ không được khởi động lại
                        stopTimer();
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