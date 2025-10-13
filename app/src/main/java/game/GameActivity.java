package game;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import com.example.test.R;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        FrameLayout container = findViewById(R.id.gameContainer);
        gameView = new GameView(this);
        container.addView(gameView);

        findViewById(R.id.buttonUp).setOnClickListener(v -> gameView.moveBear(-1, 0));
        findViewById(R.id.buttonDown).setOnClickListener(v -> gameView.moveBear(1, 0));
        findViewById(R.id.buttonLeft).setOnClickListener(v -> gameView.moveBear(0, -1));
        findViewById(R.id.buttonRight).setOnClickListener(v -> gameView.moveBear(0, 1));
    }
}
