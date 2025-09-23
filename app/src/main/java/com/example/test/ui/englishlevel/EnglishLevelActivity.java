package com.example.test.ui.englishlevel;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.R;
import com.example.test.adapter.EnglishLevelAdapter;
import com.example.test.api.ApiCallback;
import com.example.test.api.EnglishLevelManager;
import com.example.test.model.EnglishLevel;

import java.util.ArrayList;
import java.util.List;

public class EnglishLevelActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EnglishLevelAdapter adapter;
    EnglishLevelManager englishLevelManager = new EnglishLevelManager(this);
    private List<String> levelList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_english_level);
        ImageView back = findViewById(R.id.btnBack);

        back.setOnClickListener(view -> {
            finish();
        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EnglishLevelAdapter(this,levelList);
        recyclerView.setAdapter(adapter);

        fetchLevels();
    }

    private void fetchLevels() {
        englishLevelManager.fetchEnglishLevels(new ApiCallback<List<EnglishLevel>>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(List<EnglishLevel> result) {
                runOnUiThread(() -> {
                    levelList.clear();
                    for (EnglishLevel level : result) {
                        levelList.add(level.getName()); // Lấy tên của Level
                    }
                    adapter.notifyDataSetChanged();
                });
            }


            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(EnglishLevelActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    Log.e("EnglishLevel", error);
                });
            }
        });
    }
}
