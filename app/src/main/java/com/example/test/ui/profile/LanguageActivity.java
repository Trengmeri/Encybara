package com.example.test.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;
import com.example.test.ui.home.HomeActivity;

import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {

    LinearLayout btnEng, btnViet;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load ngôn ngữ trước khi khởi tạo giao diện
        loadLocale();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_language);

        initializeViews();

        btnEng.setOnClickListener(v -> {
            setLocale("en");
            finish();
        });
        btnViet.setOnClickListener(v -> {
            setLocale("vi");
            finish();
        });
    }

    private void initializeViews() {
        btnEng = findViewById(R.id.btnEnglish);
        btnViet = findViewById(R.id.btnVietnamese);
    }

    public void setLocale(String lang) {
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("Language", lang);
        editor.apply();

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Restart toàn bộ ứng dụng để thay đổi ngôn ngữ
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
