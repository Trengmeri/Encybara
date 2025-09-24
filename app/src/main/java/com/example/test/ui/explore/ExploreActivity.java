package com.example.test.ui.explore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;
import com.example.test.ui.DictionaryActivity;
import com.example.test.ui.GroupFlashcardActivity;
import com.example.test.ui.home.HomeActivity;
import com.example.test.ui.profile.ProfileActivity;
import com.example.test.ui.schedule.ScheduleActivity;
import com.example.test.ui.study.StudyActivity;

public class ExploreActivity extends AppCompatActivity {

    private ImageView btnstudy,btnhome,btnprofile;
    LinearLayout btnChat, btnDic, btnVoice, btnFlash, btnSche;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore);

        btnFlash= findViewById(R.id.btnFlash);
        btnSche= findViewById(R.id.btnSche);
        btnDic= findViewById(R.id.btnDic);

        setupBottomBar();

        btnDic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExploreActivity.this, DictionaryActivity.class);
                startActivity(intent);
            }
        });

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExploreActivity.this, GroupFlashcardActivity.class);
                startActivity(intent);
            }
        });
        btnSche.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExploreActivity.this, ScheduleActivity.class);
                startActivity(intent);
            }
        });
    }

    /** Bottom bar navigation **/
    private void setupBottomBar() {
        LinearLayout icHome = findViewById(R.id.ic_home);
        LinearLayout icStudy = findViewById(R.id.ic_study);
        LinearLayout icExplore = findViewById(R.id.ic_explore);
        LinearLayout icProfile = findViewById(R.id.ic_profile);

        icExplore.setOnClickListener(v -> {
            if (!(ExploreActivity.this instanceof ExploreActivity)) {
                startActivity(new Intent(ExploreActivity.this, ExploreActivity.class));
                overridePendingTransition(0,0);
                finish();
            }
        });

        icStudy.setOnClickListener(v -> {
            startActivity(new Intent(ExploreActivity.this, StudyActivity.class));
            overridePendingTransition(0,0);
            finish();
        });

        icHome.setOnClickListener(v -> {
            startActivity(new Intent(ExploreActivity.this, HomeActivity.class));
            overridePendingTransition(0,0);
            finish();
        });

        icProfile.setOnClickListener(v -> {
            startActivity(new Intent(ExploreActivity.this, ProfileActivity.class));
            overridePendingTransition(0,0);
            finish();
        });
    }
}