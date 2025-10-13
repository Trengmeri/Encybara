package game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.gameView);

        // 🎯 Lắng nghe khi gặp vật cản cần trả lời
        gameView.setOnQuestionListener((row, col) -> showQuestionDialog(row, col));

        // 🏁 Lắng nghe khi thắng
        gameView.setOnWinListener(() -> showWinDialog());

        // ❌ Lắng nghe khi thua (bị chặn đường)
        gameView.setOnGameOverListener(() -> showGameOverDialog()); // ✅ Thêm listener

        // ⚙️ Nút điều khiển
        ImageButton up = findViewById(R.id.buttonUp);
        ImageButton down = findViewById(R.id.buttonDown);
        ImageButton left = findViewById(R.id.buttonLeft);
        ImageButton right = findViewById(R.id.buttonRight);

        up.setOnClickListener(v -> gameView.moveBear(-1, 0));
        down.setOnClickListener(v -> gameView.moveBear(1, 0));
        left.setOnClickListener(v -> gameView.moveBear(0, -1));
        right.setOnClickListener(v -> gameView.moveBear(0, 1));
    }

    // 🧩 Câu hỏi pop-up
    private void showQuestionDialog(int row, int col) {
        new AlertDialog.Builder(this)
                .setTitle("Câu hỏi tiếng Anh 🧠")
                .setMessage("Từ 'bear' có nghĩa là gì?")
                .setPositiveButton("Con gấu", (d, w) -> {
                    gameView.clearQuestionAt(row, col);
                    Toast.makeText(GameActivity.this, "Đúng! Ô đã được dọn trống.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Con ong", (d, w) -> {
                    gameView.handleWrongAnswer(row, col);
                    Toast.makeText(GameActivity.this, "Sai rồi! Ô này biến thành đá và bạn bị đẩy lùi!", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    // 🍯 Khi đến hũ mật
    private void showWinDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Chúc mừng!")
                .setMessage("Bạn đã tìm được hũ mật 🍯!")
                .setPositiveButton("Chơi lại", (d, w) -> gameView.resetGame()) // ✅ Thêm tùy chọn chơi lại
                .setNegativeButton("Thoát", (d,w) -> finish()) // ✅ Thêm tùy chọn thoát
                .setCancelable(false)
                .show();
    }

    // ✅ Dialog thông báo Game Over
    private void showGameOverDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Game Over 😭")
                .setMessage("Bạn đã bị chặn hết đường đi! Thử lại nhé.")
                .setPositiveButton("Chơi lại", (d, w) -> gameView.resetGame()) // ✅ Chơi lại
                .setNegativeButton("Thoát", (d,w) -> finish()) // ✅ Thoát game
                .setCancelable(false)
                .show();
    }
}