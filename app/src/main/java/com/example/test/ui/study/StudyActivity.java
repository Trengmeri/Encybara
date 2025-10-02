package com.example.test.ui.study;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.test.R;
import com.example.test.ui.explore.ExploreActivity;
import com.example.test.ui.home.HomeActivity;
import com.example.test.ui.profile.ProfileActivity;

public class StudyActivity extends AppCompatActivity {
    private Button btnMyCourse, btnAllCourse;
    private ViewPager2 viewPager;
    private StudyPagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_study);

        setupBottomBar();

        btnMyCourse = findViewById(R.id.btnMyCourse);
        btnAllCourse = findViewById(R.id.btnAllCourse);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setSaveEnabled(false);

        adapter = new StudyPagerAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(new MyCourseFragment());
        adapter.addFragment(new AllCourseFragment());
        viewPager.setAdapter(adapter);

        btnMyCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
                btnMyCourse.setBackground(ContextCompat.getDrawable(StudyActivity.this, R.drawable.bg_mycourse));
                btnAllCourse.setBackground(ContextCompat.getDrawable(StudyActivity.this, R.drawable.bg_allcourse));
            }
        });

        btnAllCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
                btnAllCourse.setBackground(ContextCompat.getDrawable(StudyActivity.this, R.drawable.bg_mycourse));
                btnMyCourse.setBackground(ContextCompat.getDrawable(StudyActivity.this, R.drawable.bg_allcourse));
            }
        });

        // Nhận courseId từ Intent (truyền từ HomeActivity hoặc nơi khác)
        int selectedCourseId = getIntent().getIntExtra("selected_course_id", -1);
        if (selectedCourseId != -1) {
            selectCourse(selectedCourseId);
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                refreshCurrentFragment(position);
            }
        });
    }

    private void refreshCurrentFragment(int position) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + position);
        if (fragment instanceof MyCourseFragment && position == 0) {
            ((MyCourseFragment) fragment).onResume();
        } else if (fragment instanceof AllCourseFragment && position == 1) {
            ((AllCourseFragment) fragment).onResume();
        }
    }

    public void selectCourse(int courseId) {
        Log.d("StudyActivity", "Selecting course with ID: " + courseId);
        viewPager.setCurrentItem(0, true);

        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f0");
        if (currentFragment instanceof MyCourseFragment) {
            Log.d("StudyActivity", "Found existing MyCourseFragment");
            ((MyCourseFragment) currentFragment).openFirstLesson(courseId);
        } else {
            Log.e("StudyActivity", "MyCourseFragment not found");
        }
    }

    /** Bottom bar navigation **/
    private void setupBottomBar() {
        LinearLayout icHome = findViewById(R.id.ic_home);
        LinearLayout icStudy = findViewById(R.id.ic_study);
        LinearLayout icExplore = findViewById(R.id.ic_explore);
        LinearLayout icProfile = findViewById(R.id.ic_profile);

        icHome.setSelected(false);
        icProfile.setSelected(false);
        icStudy.setSelected(true);
        icExplore.setSelected(false);

        icStudy.setOnClickListener(v -> {
            if (!(StudyActivity.this instanceof StudyActivity)) {
                startActivity(new Intent(StudyActivity.this, StudyActivity.class));
                overridePendingTransition(0,0);
                finish();
            }
        });

        icHome.setOnClickListener(v -> {
            startActivity(new Intent(StudyActivity.this, HomeActivity.class));
            overridePendingTransition(0,0);
            finish();
        });

        icExplore.setOnClickListener(v -> {
            startActivity(new Intent(StudyActivity.this, ExploreActivity.class));
            overridePendingTransition(0,0);
            finish();
        });

        icProfile.setOnClickListener(v -> {
            startActivity(new Intent(StudyActivity.this, ProfileActivity.class));
            overridePendingTransition(0,0);
            finish();
        });
    }
}
