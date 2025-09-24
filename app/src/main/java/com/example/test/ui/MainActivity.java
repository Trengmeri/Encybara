package com.example.test.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.test.R;
import com.example.test.ui.schedule.AlarmScheduler;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_IS_FIRST_TIME = "isFirstTime";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load lại ngôn ngữ trước khi hiển thị giao diện
        loadLocale();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainIntro), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AlarmScheduler.logAllAlarms(this);

        final ImageView bookTextImageView = findViewById(R.id.bookTextImageView);
        final ImageView capybaraImageView = findViewById(R.id.capybaraImageView);

        // Ban đầu ẩn bookText và capybara
        bookTextImageView.setAlpha(0f);
        capybaraImageView.setAlpha(0f);

// Lấy vị trí Y của bookText
        float bookY = bookTextImageView.getY();
        float startY = 400f; // điểm bắt đầu từ dưới
        float endY = bookY - capybaraImageView.getHeight(); // lên sát chữ

// Thời gian fade-in book
        long fadeDuration = 1000;

// Hiện bookText bằng fade-in
        bookTextImageView.animate()
                .alpha(1f)
                .setDuration(fadeDuration)
                .start();

// Hiện capybara alpha = 1 ngay từ đầu để nhìn thấy nhảy
        capybaraImageView.setAlpha(1f);

// Tạo ValueAnimator để capybara nhảy cùng lúc với book fade-in
        ValueAnimator jumpAnimator = ValueAnimator.ofFloat(startY, endY);
        jumpAnimator.setDuration(fadeDuration); // cùng thời gian với book fade-in
        jumpAnimator.setInterpolator(new DecelerateInterpolator());
        jumpAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            capybaraImageView.setTranslationY(value);
        });
        jumpAnimator.start();

        // Chuyển sang màn hình kế tiếp sau 3 giây
        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isFirstTime = prefs.getBoolean(KEY_IS_FIRST_TIME, true);
            Intent intent;
            if (isFirstTime) {
                intent = new Intent(MainActivity.this, Intro2Activity.class);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(KEY_IS_FIRST_TIME, false);
                editor.apply();
            } else {
                intent = new Intent(MainActivity.this, SignInActivity.class);
            }
            startActivity(intent);
            finish();
        }, 5000);
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("Language", "en"); // Mặc định là tiếng Anh
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
