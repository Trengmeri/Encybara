package com.example.test.ui;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatDelegate;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.test.R;
import com.example.test.ui.schedule.AlarmScheduler;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_IS_FIRST_TIME = "isFirstTime";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load lại ngôn ngữ trước khi hiển thị giao diện
        loadLocale();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AlarmScheduler.logAllAlarms(this);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(KEY_IS_FIRST_TIME, true);

        // Chuyển sang Intro2 sau 3 giây
        // Hiển thị MainActivity trong 3 giây, sau đó điều hướng
        new Handler().postDelayed(() -> {
            Intent intent;
            if (isFirstTime) {
                // Lần đầu mở app, đi qua Intro2
                intent = new Intent(MainActivity.this, Intro2Activity.class);
                // Đánh dấu đã qua lần đầu
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(KEY_IS_FIRST_TIME, false);
                editor.apply();
            } else {
                // Không phải lần đầu, chuyển thẳng đến SignInActivity
                intent = new Intent(MainActivity.this, SignInActivity.class);
            }
            startActivity(intent);
            finish(); // Đóng MainActivity sau khi chuyển màn
        }, 3000);
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
