package com.example.test.game;

import android.annotation.SuppressLint;

import com.example.test.api.ApiCallback;
import com.example.test.api.GameManager;
import android.content.Intent;
import android.media.MediaPlayer; // Import MediaPlayer
import android.media.SoundPool;   // Import SoundPool
import android.media.AudioAttributes; // Import AudioAttributes cho SoundPool (API 21+)
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.test.R;
import com.example.test.ui.question_data.PointResultCourseActivity;
// Các import khác không liên quan đến âm thanh có thể bị xóa nếu không dùng
// import com.example.test.ui.ForgotPassWordActivity;
// import com.example.test.ui.SignInActivity;

import java.util.HashMap; // Import HashMap để quản lý SoundPool IDs

public class IntroGameActivity extends AppCompatActivity {

    ImageButton btnStart, btnExit;

    private MediaPlayer introMediaPlayer; // Đối tượng MediaPlayer cho nhạc nền intro
    private SoundPool soundPool;          // Đối tượng SoundPool cho hiệu ứng âm thanh
    private HashMap<Integer, Integer> soundMap; // Lưu trữ ID của các âm thanh đã tải vào SoundPool
    private int courseID,sessionId;
    private GameManager gameManager = new GameManager(this);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnStart = findViewById(R.id.btnStart);
        btnExit = findViewById(R.id.btnExit);

        // --- Khởi tạo MediaPlayer cho nhạc nền Intro ---
        try {
            introMediaPlayer = MediaPlayer.create(this, R.raw.intro_game_sound);
            if (introMediaPlayer == null) {
                Log.e("IntroGameActivity", "MediaPlayer.create() failed, check intro_music.mp3");
            } else {
                Log.e("IntroGameActivity", "MediaPlayer.create() success");
                introMediaPlayer.setLooping(true);
                introMediaPlayer.setVolume(0.6f, 0.6f);
                introMediaPlayer.start();

            }
        } catch (Exception e) {
            Log.e("IntroGameActivity", "Error creating MediaPlayer: " + e.getMessage());
            e.printStackTrace();
        }

        // --- Khởi tạo SoundPool cho hiệu ứng âm thanh ---
        // Sử dụng AudioAttributes.Builder cho API 21+
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5) // Số lượng âm thanh có thể phát cùng lúc
                .setAudioAttributes(audioAttributes)
                .build();

        // Khởi tạo HashMap và tải âm thanh nút Start
        soundMap = new HashMap<>();
        // Tải âm thanh button_start_sound vào SoundPool và lưu ID của nó
        soundMap.put(R.raw.pop_sound, soundPool.load(this, R.raw.pop_sound, 1)); // 1 là ưu tiên


        // Lấy courseID từ Intent
        courseID = getIntent().getIntExtra("CourseID", 1);
        Log.d("CourseID","CourseID tu intent : "+ courseID);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Phát âm thanh nút Start
                if (soundPool != null && soundMap.containsKey(R.raw.pop_sound)) {
                    soundPool.play(soundMap.get(R.raw.pop_sound), 1.0f, 1.0f, 1, 0, 1.0f);
                }

                // Tạm dừng nhạc nền intro trước khi chuyển Activity
                if (introMediaPlayer != null && introMediaPlayer.isPlaying()) {
                    introMediaPlayer.pause();
                }

                String gameName = "Game Course " + courseID;
                String gameDescription = "Testing game for course " + courseID;
                String gameType = "REVIEW"; // REVIEW, PRACTICE, ...
                int maxQuestions = 10;
                int timeLimit = 120; //

                gameManager.sendCreateGameRequest(
                        courseID,
                        gameName,
                        gameDescription,
                        gameType,
                        maxQuestions,
                        timeLimit,
                        new ApiCallback<Integer>() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    Toast.makeText(IntroGameActivity.this, "Tạo game thành công!", Toast.LENGTH_SHORT).show();
                                    Log.d("IntroGameActivity", "API Create Game thành công. Bắt đầu GameActivity.");
                                    Intent intent = new Intent(IntroGameActivity.this, GameActivity.class);
                                    intent.putExtra("Courseid", courseID);
                                    intent.putExtra("SESSION_ID", sessionId); // Truyền sessionId
                                    startActivity(intent);
                                });
                            }

                            @Override
                            public void onSuccess(Integer gameId) {
                                Log.d("GameFlow", "Game created with ID: " + gameId);

                                // Sau khi tạo thành công => gọi API start
                                gameManager.sendStartGameRequest(gameId, new ApiCallback() {
                                    @Override
                                    public void onSuccess() {
//                                        Log.d("GameFlow", "Game started successfully!");
//                                        Intent intent = new Intent(IntroGameActivity.this, GameActivity.class);
//                                        intent.putExtra("Courseid", courseID);
//                                        //intent.putExtra("SESSION_ID", sessionId); // Truyền sessionId
//                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onSuccess(Object result) {
                                        if (result instanceof Integer) {
                                            // Lấy Session ID từ phản hồi (Ví dụ: 21)
                                            int receivedSessionId = (int) result;

                                            // ❌ LỖI GỐC: Bạn khai báo biến global là 'sessionId', nhưng lại KHÔNG dùng nó ở đây.
                                            // HÃY GÁN CHO BIẾN GLOBAL NẾU CẦN, NHƯNG TỐT NHẤT DÙNG BIẾN CỤC BỘ receivedSessionId.

                                            Log.d("GameFlow", "Game started successfully. Session ID: " + receivedSessionId);

                                            runOnUiThread(() -> {
                                                Intent intent = new Intent(IntroGameActivity.this, GameActivity.class);
                                                intent.putExtra("Courseid", courseID);

                                                // ✅ Đảm bảo khóa là "SESSION_ID" và giá trị là receivedSessionId
                                                intent.putExtra("SESSION_ID", receivedSessionId);

                                                startActivity(intent);
                                            });
                                        }}

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Log.e("GameFlow", "Start game failed: " + errorMessage);
                                    }
                                });

                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                runOnUiThread(() -> {
                                    Toast.makeText(IntroGameActivity.this, "Lỗi tạo game: " + errorMessage, Toast.LENGTH_LONG).show();
                                    Log.e("IntroGameActivity", "API Create Game thất bại: " + errorMessage);
                                    // Khôi phục nhạc nền nếu thất bại và người dùng ở lại activity
                                    if (introMediaPlayer != null && !introMediaPlayer.isPlaying()) {
                                        introMediaPlayer.start();
                                    }
                                });
                            }
                        }
                );
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Phát âm thanh nút Start
                if (soundPool != null && soundMap.containsKey(R.raw.pop_sound)) {
                    soundPool.play(soundMap.get(R.raw.pop_sound), 1.0f, 1.0f, 1, 0, 1.0f);
                }

                // Tạm dừng nhạc nền intro trước khi chuyển Activity
                if (introMediaPlayer != null && introMediaPlayer.isPlaying()) {
                    introMediaPlayer.pause();
                }
                Intent intent = new Intent(IntroGameActivity.this, PointResultCourseActivity.class);
                startActivity(intent);

            }
        });

    }

    // --- Quản lý vòng đời của MediaPlayer và SoundPool ---

    @Override
    protected void onResume() {
        super.onResume();
        // Phát nhạc nền intro khi Activity trở lại trạng thái hoạt động
        if (introMediaPlayer != null && !introMediaPlayer.isPlaying()) {
            introMediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Tạm dừng nhạc nền intro khi Activity bị tạm dừng
        if (introMediaPlayer != null && introMediaPlayer.isPlaying()) {
            introMediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng tài nguyên của MediaPlayer khi Activity bị hủy
        if (introMediaPlayer != null) {
            introMediaPlayer.stop();
            introMediaPlayer.release();
            introMediaPlayer = null;
        }
        // Giải phóng tài nguyên của SoundPool khi Activity bị hủy
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}