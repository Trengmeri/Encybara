package game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast; // ✅ Thêm Toast để hiển thị thông báo

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
                    Toast.makeText(GameActivity.this, "Đúng! Ô đã được dọn trống.", Toast.LENGTH_SHORT).show(); // ✅ Thông báo đúng
                })
                .setNegativeButton("Con ong", (d, w) -> {
                    gameView.handleWrongAnswer(row, col); // ✅ Gọi phương thức xử lý trả lời sai
                    Toast.makeText(GameActivity.this, "Sai rồi! Ô này biến thành đá và bạn bị đẩy lùi!", Toast.LENGTH_LONG).show(); // ✅ Thông báo sai
                })
                .setCancelable(false) // ✅ Ngăn không cho người dùng đóng dialog mà không trả lời
                .show();
    }

    // 🍯 Khi đến hũ mật
    private void showWinDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Chúc mừng!")
                .setMessage("Bạn đã tìm được hũ mật 🍯!")
                .setPositiveButton("OK", (d, w) -> {
                    // Có thể thêm logic khởi động lại game hoặc thoát ứng dụng tại đây
                    finish(); // Ví dụ: đóng activity
                })
                .setCancelable(false) // ✅ Ngăn không cho người dùng đóng dialog mà không nhấn OK
                .show();
    }
}