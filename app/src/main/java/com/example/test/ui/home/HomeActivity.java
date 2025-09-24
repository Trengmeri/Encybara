package com.example.test.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.api.ApiCallback;
import com.example.test.api.LearningProgressManager;
import com.example.test.api.UserManager;
import com.example.test.ui.NotificationActivity;
import com.example.test.ui.explore.ExploreActivity;
import com.example.test.ui.profile.ProfileActivity;
import com.example.test.ui.study.StudyActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private Button continueButton;
    private TextView courseTitle;
    private TextView totalPoints, readingPoints, listeningPoints, speakingPoints, writingPoints;
    private ImageView btnNoti, btnProfile;
    private UserManager userManager;
    private LearningProgressManager learningProgressManager;
    private List<CourseInfo> activeCourses;
    private int selectedCourseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);

        initializeViews();
        initializeManagers();
        setupViews();
        loadData();

        // Transparent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        btnNoti.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        setupBottomBar();
    }

    private void initializeViews() {
        btnProfile = findViewById(R.id.imgAvatar);
        continueButton = findViewById(R.id.btn_continue);
        courseTitle = findViewById(R.id.courseTitle);
        btnNoti = findViewById(R.id.img_notification);
        totalPoints = findViewById(R.id.totalPoints);
        readingPoints = findViewById(R.id.readingpoint);
        listeningPoints = findViewById(R.id.listeningpoint);
        speakingPoints = findViewById(R.id.speakingpoint);
        writingPoints = findViewById(R.id.writingpoint);
    }

    private void initializeManagers() {
        learningProgressManager = new LearningProgressManager(this);
        userManager = new UserManager(this);
        activeCourses = new ArrayList<>();
    }

    private void setupViews() {
        continueButton.setOnClickListener(v -> {
            if (selectedCourseId != -1) {
                Log.d("HomeActivity", "Continue button clicked for course ID: " + selectedCourseId);
                // Chuyển sang StudyActivity thay vì fragment
                Intent intent = new Intent(HomeActivity.this, StudyActivity.class);
                intent.putExtra("courseId", selectedCourseId);
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        loadLearningResults();
        loadLatestLessonAndCourse();
        loadUserProfile();
    }

    private void showNoCourseMessage() {
        courseTitle.setText(getString(R.string.nocourse));
        continueButton.setVisibility(View.GONE);
    }

    private void loadLearningResults() {
        learningProgressManager.fetchLearningResults(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                updateScores(result);
            }

            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String error) {
                handleError("Failed to load learning results", error);
            }
        });
    }

    private void updateScores(JsonObject result) {
        try {
            double listeningScore = result.get("listeningScore").getAsDouble();
            double speakingScore = result.get("speakingScore").getAsDouble();
            double readingScore = result.get("readingScore").getAsDouble();
            double writingScore = result.get("writingScore").getAsDouble();
            double overallScore = result.get("overallScore").getAsDouble();

            runOnUiThread(() -> {
                totalPoints.setText(String.format("%.1f", overallScore));
                listeningPoints.setText(String.format("%.1f", listeningScore));
                speakingPoints.setText(String.format("%.1f", speakingScore));
                readingPoints.setText(String.format("%.1f", readingScore));
                writingPoints.setText(String.format("%.1f", writingScore));
            });
        } catch (Exception e) {
            handleError("Error parsing scores", e.getMessage());
        }
    }

    private void handleError(String message, String error) {
        Log.e("HomeActivity", message + ": " + error);
        runOnUiThread(() ->
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show()
        );
    }

    private void loadLatestLessonAndCourse() {
        learningProgressManager.fetchLatestLesson(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess() {}

            @Override
            public void onSuccess(JsonObject latestLesson) {
                try {
                    if (latestLesson.has("content") && latestLesson.get("content").isJsonArray()) {
                        JsonArray lessons = latestLesson.getAsJsonArray("content");
                        if (lessons.size() > 0) {
                            JsonObject lastLesson = lessons.get(lessons.size() - 1).getAsJsonObject();
                            int lessonId = lastLesson.get("lessonId").getAsInt();
                            fetchCoursesContainingLesson(lessonId);
                        } else {
                            runOnUiThread(HomeActivity.this::showNoCourseMessage);
                        }
                    } else {
                        runOnUiThread(HomeActivity.this::showNoCourseMessage);
                    }
                } catch (Exception e) {
                    handleError("Error parsing latest lesson", e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                handleError("Failed to fetch latest lesson", error);
            }
        });
    }

    private void fetchCoursesContainingLesson(int lessonId) {
        learningProgressManager.fetchCourses(new ApiCallback<JsonArray>() {
            @Override
            public void onSuccess() {}

            @Override
            public void onSuccess(JsonArray courses) {
                try {
                    for (int i = 0; i < courses.size(); i++) {
                        JsonObject course = courses.get(i).getAsJsonObject();
                        JsonArray lessonIds = course.getAsJsonArray("lessonIds");
                        for (int j = 0; j < lessonIds.size(); j++) {
                            if (lessonIds.get(j).getAsInt() == lessonId) {
                                int courseId = course.get("id").getAsInt();
                                String courseName = course.get("name").getAsString();
                                runOnUiThread(() -> {
                                    courseTitle.setText(courseName);
                                    selectedCourseId = courseId;
                                    continueButton.setVisibility(View.VISIBLE);
                                });
                                return;
                            }
                        }
                    }
                    runOnUiThread(HomeActivity.this::showNoCourseMessage);
                } catch (Exception e) {
                    handleError("Error parsing courses", e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                handleError("Failed to fetch courses", error);
            }
        });
    }

    private void loadUserProfile() {
        String userId = SharedPreferencesManager.getInstance(this).getID();
        if (userId == null) return;

        userManager.fetchUserProfile(Integer.parseInt(userId), new ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                runOnUiThread(() -> {
                    String avatarUrl = result.optString("avatar");
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        avatarUrl = avatarUrl.replace("0.0.0.0", "14.225.198.3");
                        Glide.with(HomeActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.img_avt_profile)
                                .error(R.drawable.img_avt_profile)
                                .circleCrop()
                                .into(btnProfile);
                    }
                });
            }

            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(HomeActivity.this,
                                "Failed to load profile: " + errorMessage,
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLearningResults();
        loadUserProfile();
    }

    /** Bottom bar navigation **/
    private void setupBottomBar() {
        LinearLayout icHome = findViewById(R.id.ic_home);
        LinearLayout icStudy = findViewById(R.id.ic_study);
        LinearLayout icExplore = findViewById(R.id.ic_explore);
        LinearLayout icProfile = findViewById(R.id.ic_profile);

        icHome.setOnClickListener(v -> {
            if (!(HomeActivity.this instanceof HomeActivity)) {
                startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                overridePendingTransition(0,0);
                finish();
            }
        });

        icStudy.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, StudyActivity.class));
            overridePendingTransition(0,0);
            finish();
        });

        icExplore.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ExploreActivity.class));
            overridePendingTransition(0,0);
            finish();
        });

        icProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            overridePendingTransition(0,0);
            finish();
        });
    }

    // Helper class để lưu thông tin khóa học
    static class CourseInfo {
        private int courseId;
        private String courseName;

        public CourseInfo(int courseId, String courseName) {
            this.courseId = courseId;
            this.courseName = courseName;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        @Override
        public String toString() {
            return courseName;
        }
    }
}

