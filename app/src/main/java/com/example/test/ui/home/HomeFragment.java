package com.example.test.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.test.R;

import com.example.test.SharedPreferencesManager;
import com.example.test.api.ApiCallback;
import com.example.test.api.LessonManager;
import com.example.test.api.ResultManager;
import com.example.test.api.LearningProgressManager;
import com.example.test.api.UserManager;
import com.example.test.model.Enrollment;
import com.example.test.model.Lesson;
import com.example.test.model.Result;
import com.example.test.ui.NotificationActivity;
import com.example.test.ui.profile.ProfileFragment;
import com.example.test.ui.study.MyCourseFragment;
import com.example.test.ui.study.StudyFragment;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import android.view.MenuItem;
import android.widget.PopupMenu;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {
    private Button continueButton,selectCourseButton;
    private TextView courseTitle, courseNumber;
    private TextView totalPoints, readingPoints, listeningPoints, speakingPoints, writingpoint;
    private ImageView btnNoti, btnProfile;
    private UserManager userManager;
    private LearningProgressManager learningProgressManager;
    private List<CourseInfo> activeCourses;
    private int selectedCourseId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        initializeManagers();
        setupViews();
        loadData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = requireActivity().getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        btnNoti.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);
        });
    }

    private void initializeViews(View view) {
        btnProfile = view.findViewById(R.id.imgAvatar);
        continueButton = view.findViewById(R.id.btn_continue);
        courseTitle = view.findViewById(R.id.courseTitle);
//        courseNumber = view.findViewById(R.id.courseNumber);
        btnNoti = view.findViewById(R.id.img_notification);
        totalPoints = view.findViewById(R.id.totalPoints);
        readingPoints = view.findViewById(R.id.readingpoint);
        listeningPoints = view.findViewById(R.id.listeningpoint);
        speakingPoints = view.findViewById(R.id.speakingpoint);
        writingpoint = view.findViewById(R.id.writingpoint);
    }

    private void initializeManagers() {
        learningProgressManager = new LearningProgressManager(requireContext());
        userManager = new UserManager(requireContext());
        activeCourses = new ArrayList<>();
    }

    private void setupViews() {
        setupClickListeners();
    }
    private void setupClickListeners() {
        continueButton.setOnClickListener(v -> {
            if (!isAdded() || getActivity() == null) {
                return;
            }

            if (selectedCourseId != -1) {
                Log.d("HomeFragment", "Continue button clicked for course ID: " + selectedCourseId);

                // Switch to Study tab
                ViewPager2 viewPager = requireActivity().findViewById(R.id.vpg_main);
                viewPager.setCurrentItem(1, true);

                // Pass course ID to StudyFragment
                StudyFragment studyFragment = (StudyFragment) requireActivity()
                        .getSupportFragmentManager()
                        .findFragmentByTag("f1");

                if (studyFragment != null) {
                    Log.d("HomeFragment", "Found StudyFragment, selecting course");
                    studyFragment.selectCourse(selectedCourseId);
                } else {
                    Log.e("HomeFragment", "StudyFragment not found");
                }
            }
        });
    }
    private void loadData() {

        loadLearningResults();
        loadLatestLessonAndCourse();
        loadUserProfile();
    }
//    private void loadEnrollments() {
//        learningProgressManager.fetchLatestEnrollment(new ApiCallback<JsonObject>() {
//            @Override
//            public void onSuccess(JsonObject enrollment) {
//                try {
//                    JsonArray content = enrollment.getAsJsonObject("data")
//                            .getAsJsonArray("content");
//                    processEnrollments(content);
//                } catch (Exception e) {
//                    handleError("Error parsing enrollment", e);
//                }
//            }
//
//            @Override
//            public void onSuccess() {}
//
//            @Override
//            public void onFailure(String error) {
//                handleError("Failed to load enrollments", error);
//            }
//        });
//    }
//
//    private void processEnrollments(JsonArray enrollments) {
//        activeCourses.clear();
//        selectedCourseId = -1; // Reset selected course ID
//
//        // Log the entire enrollment data
//        Log.d("HomeFragment", "Enrollment data: " + enrollments.toString());
//
//        // Iterate from the end of the array to find the nearest course matching the conditions
//        for (int i = enrollments.size() - 1; i >= 0; i--) {
//            JsonObject course = enrollments.get(i).getAsJsonObject();
//            Log.d("HomeFragment", "Processing course: " + course.toString());
//
//            if (isMatchingCourse(course)) {
//                Log.d("HomeFragment", "Matching course found: " + course.toString());
//                // Safely retrieve courseId
//                if (course.has("courseId") && !course.get("courseId").isJsonNull()) {
//                    selectedCourseId = course.get("courseId").getAsInt();
//
//                    // Fetch course name using courseId
//                    learningProgressManager.fetchCourseDetails(selectedCourseId, new ApiCallback<String>() {
//                        @Override
//                        public void onSuccess() {
//
//                        }
//
//                        @Override
//                        public void onSuccess(String courseName) {
//                            // Update UI with the selected course
//                            requireActivity().runOnUiThread(() -> {
//                                courseTitle.setText(courseName);
//                                continueButton.setVisibility(View.VISIBLE);
//                            });
//                        }
//
//                        @Override
//                        public void onFailure(String error) {
//                            Log.e("HomeFragment", "Failed to fetch course details: " + error);
//                            requireActivity().runOnUiThread(() -> showNoCourseMessage());
//                        }
//                    });
//                    return; // Stop after finding the first match
//                } else {
//                    Log.e("HomeFragment", "Missing or null courseId in JSON object");
//                }
//            } else {
//                Log.d("HomeFragment", "Course does not match conditions: " + course.toString());
//            }
//        }
//
//        // If no matching course is found, show a message
//        requireActivity().runOnUiThread(this::showNoCourseMessage);
//    }
//    private boolean isMatchingCourse(JsonObject course) {
//        boolean hasProStatus = course.has("proStatus") && !course.get("proStatus").isJsonNull();
//        boolean hasTotalPoints = course.has("totalPoints") && !course.get("totalPoints").isJsonNull();
//        boolean hasComLevel = course.has("comLevel") && !course.get("comLevel").isJsonNull();
//
//        if (hasProStatus && hasTotalPoints && hasComLevel) {
//            boolean proStatus = course.get("proStatus").getAsBoolean();
//            double totalPoints = course.get("totalPoints").getAsDouble();
//            double comLevel = course.get("comLevel").getAsDouble();
//
//            return proStatus && totalPoints == 0 && comLevel == 0.0;
//        }
//
//        return false;
//    }

    private void showNoCourseMessage() {
        courseTitle.setText(getString(R.string.nocourse));
        //courseNumber.setText("");
        continueButton.setVisibility(View.GONE);
    }

    private void loadLearningResults() {
        learningProgressManager.fetchLearningResults(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                if (!isAdded()) return;
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

            requireActivity().runOnUiThread(() -> {
                totalPoints.setText(String.format("%.1f", overallScore));
                listeningPoints.setText(String.format("%.1f", listeningScore));
                speakingPoints.setText(String.format("%.1f", speakingScore));
                readingPoints.setText(String.format("%.1f", readingScore));
                writingpoint.setText(String.format("%.1f", writingScore));
            });
        } catch (Exception e) {
            handleError("Error parsing scores", e);
        }
    }

    private void handleError(String message, String error) {
        Log.e("HomeFragment", message + ": " + error);
        if (isAdded()) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void handleError(String message, Exception e) {
        handleError(message, e.getMessage());
    }

    private void loadLatestLessonAndCourse() {
        learningProgressManager.fetchLatestLesson(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(JsonObject latestLesson) {
                try {
                    Log.d("HomeFragment", "Latest lesson response: " + latestLesson.toString());
                    // Ensure "data" exists and is not null
                        // Ensure "content" exists and is a JsonArray
                        if (latestLesson.has("content") && latestLesson.get("content").isJsonArray()) {
                            JsonArray lessons = latestLesson.getAsJsonArray("content");

                            if (lessons.size() > 0) {
                                JsonObject lastLesson = lessons.get(lessons.size() - 1).getAsJsonObject();
                                int lessonId = lastLesson.get("lessonId").getAsInt();
                                Log.d("HomeFragment", "Latest lessonId: " + lessonId);

                                // Fetch courses containing the lesson
                                fetchCoursesContainingLesson(lessonId);
                            } else {
                                Log.e("HomeFragment", "No lessons found in response");
                                requireActivity().runOnUiThread(() -> showNoCourseMessage());
                            }
                        } else {
                            Log.e("HomeFragment", "Missing or invalid 'content' field in 'data'");
                            requireActivity().runOnUiThread(() -> showNoCourseMessage());
                        }

                } catch (Exception e) {
                    handleError("Error parsing latest lesson", e);
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
            public void onSuccess() {

            }

            @Override
            public void onSuccess(JsonArray courses) {

                try {
                    Log.d("HomeFregêmnt","Course details" + courses.toString());
                    for (int i = 0; i < courses.size(); i++) {
                        JsonObject course = courses.get(i).getAsJsonObject();

                        JsonArray lessonIds = course.getAsJsonArray("lessonIds");

                        // Kiểm tra nếu lessonId nằm trong lessonIds
                        for (int j = 0; j < lessonIds.size(); j++) {
                            if (lessonIds.get(j).getAsInt() == lessonId) {
                                Log.d("HomeFragment", "Course found: " + course.toString());
                                int courseId = course.get("id").getAsInt();
                                String courseName = course.get("name").getAsString();

                                // Hiển thị thông tin khóa học
                                requireActivity().runOnUiThread(() -> {
                                    courseTitle.setText(courseName);
                                    selectedCourseId = courseId;
                                    continueButton.setVisibility(View.VISIBLE);
                                });
                                return;
                            }
                        }
                    }

                    // Nếu không tìm thấy khóa học nào chứa lessonId
                    Log.e("HomeFragment", "No course contains the lessonId: " + lessonId);
                    requireActivity().runOnUiThread(() -> showNoCourseMessage());
                } catch (Exception e) {
                    handleError("Error parsing courses", e);
                }
            }

            @Override
            public void onFailure(String error) {
                handleError("Failed to fetch courses", error);
            }
        });
    }
    private void loadUserProfile() {
        String userId = SharedPreferencesManager.getInstance(requireContext()).getID();
        if (userId == null) return;

        userManager.fetchUserProfile(Integer.parseInt(userId), new ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                if (getActivity() == null || !isAdded()) return;

                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    String avatarUrl = result.optString("avatar");
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        avatarUrl = avatarUrl.replace("0.0.0.0", "14.225.198.3");

                        if (isAdded()) { // Check before using Glide
                            Glide.with(requireActivity())
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.img_avt_profile)
                                    .error(R.drawable.img_avt_profile)
                                    .circleCrop()
                                    .into(btnProfile);
                        }
                    }
                });
            }

            @Override
            public void onSuccess() {
                // Not used
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() == null || !isAdded()) return;

                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            "Failed to load profile: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();

        loadLearningResults();
        loadUserProfile();
    }
}
// Helper class để lưu thông tin khóa học
class CourseInfo {
    private int courseId;
    private String courseName;

    public CourseInfo(int courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
    }

    public int getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }

    @Override
    public String toString() {
        return courseName;
    }
}
