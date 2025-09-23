package com.example.test.ui.explore;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;

public class ExploreActivity extends AppCompatActivity {

    private ImageView btnstudy,btnhome,btnprofile;
    LinearLayout btnChat, btnDic, btnVoice, btnFlash, btnSche;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore);

//        btnstudy = findViewById(R.id.ic_study);
//        btnhome = findViewById(R.id.ic_home);
//        btnprofile = findViewById(R.id.ic_profile);
//        btnFlash= findViewById(R.id.btnFlash);
//        btnSche= findViewById(R.id.btnSche);

//        btnhome.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ExploreActivity.this, HomeActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        btnFlash.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ExploreActivity.this, GroupFlashcardActivity.class);
//                startActivity(intent);
//            }
//        });
//        btnSche.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ExploreActivity.this, ScheduleActivity.class);
//                startActivity(intent);
//            }
//        });


    }
}