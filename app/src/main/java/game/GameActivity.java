package game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

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
                .setPositiveButton("Con gấu", (d, w) -> gameView.clearQuestionAt(row, col))
                .setNegativeButton("Con ong", null)
                .show();
    }

    // 🍯 Khi đến hũ mật
    private void showWinDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Chúc mừng!")
                .setMessage("Bạn đã tìm được hũ mật 🍯!")
                .setPositiveButton("OK", null)
                .show();
    }
}
