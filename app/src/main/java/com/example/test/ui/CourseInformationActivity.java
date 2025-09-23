package com.example.test.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.test.NevigateQuestion;
import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.adapter.LessonAdapter;
import com.example.test.adapter.ReviewAdapter;
import com.example.test.api.ApiCallback;
import com.example.test.api.CourseManager;
import com.example.test.api.LessonManager;
import com.example.test.api.ResultManager;
import com.example.test.api.ReviewManager;
import com.example.test.model.Course;
import com.example.test.model.Enrollment;
import com.example.test.model.Lesson;
import com.example.test.model.Review;
import com.example.test.model.User;
import com.example.test.ui.home.HomeActivity;
import com.example.test.ui.study.StudyFragment;

import java.util.ArrayList;
import java.util.List;

    public class CourseInformationActivity extends AppCompatActivity {

    AppCompatButton btnAbout, btnLesson;
    ImageView btnSendReview, btnBackto, btnJoin;
    LinearLayout contentAbout, contentLes;
    TextView txtContentAbout, courseName, numLessons;
    Course curCourse;
    private int courseID;
    private int currentPage = 1; // Bắt đầu từ trang 1
    private boolean isLoading = false; // Để tránh tải dữ liệu nhiều lần
    private boolean hasMoreData = true; // Để biết còn dữ liệu để tải không

    private RecyclerView recyclerView, recyclerViewLesson;
    private ReviewAdapter reviewAdapter;
    private LessonAdapter lessonAdapter;
    private ReviewManager reviewManager = new ReviewManager(this);
    private LessonManager lessonManager = new LessonManager();
    private CourseManager courseManager = new CourseManager(CourseInformationActivity.this);
    private ResultManager resultManager = new ResultManager(CourseInformationActivity.this);
    private List<Review> reviews= new ArrayList<>();

    private User curUser = SharedPreferencesManager.getInstance(this).getUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_infor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        khaiBao();

        // Kiểm tra null cho các view quan trọng
        if (courseName == null || txtContentAbout == null || recyclerView == null) {
            Log.e("CourseActivity", "One or more views are null. Check activity_course.xml");
            return;
        }
        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        reviewAdapter = new ReviewAdapter(this, new ArrayList<>());
//        recyclerView.setAdapter(reviewAdapter);

        lessonAdapter = new LessonAdapter(this);
        recyclerViewLesson.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLesson.setAdapter(lessonAdapter);

        // Lấy thông tin khóa học
        getCourseInfo(courseID, new ApiCallback<Course>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(Course course) {
                Runnable thread = new Runnable() {
                    @Override
                    public void run() {

                    }
                };


                runOnUiThread(() -> {
                    curCourse = course;
                    courseName.setText(course.getName());
                    txtContentAbout.setText(course.getIntro());
                    numLessons.setText(course.getLessonIds().size() + " lessons ");
                    Log.d("CourseInfo", "Name: " + course.getName() + ", Intro: " + course.getIntro());

                    // Gọi hàm lấy danh sách bài học
                    loadLessons(course.getLessonIds());

//                    loadReviews(); // Tải reviews sau khi có curCourse
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Log.e("CourseInfo", "Lỗi: " + errorMessage));
            }
        });
        loadReviews();





        // Sự kiện nút About và Lesson
        btnAbout.setOnClickListener(v -> {
            btnAbout.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_about));
            btnLesson.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_lesson));
            contentAbout.setVisibility(View.VISIBLE);
            recyclerViewLesson.setVisibility(View.GONE);
        });

        btnLesson.setOnClickListener(v -> {
            btnLesson.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_about));
            btnAbout.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_lesson));
            contentAbout.setVisibility(View.GONE);
            recyclerViewLesson.setVisibility(View.VISIBLE);
        });
        btnJoin.setOnClickListener(v -> {
            joinCourse();
        });

        btnBackto.setOnClickListener(v -> {
            finish();
        });
    }

        private void joinCourse() {
            resultManager.getEnrollment(courseID, new ApiCallback<Enrollment>() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onSuccess(Enrollment result) {
                    int id = result.getId();
                    courseManager.joinCourse(id, new ApiCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("CourseManager", "Course joined successfully");
                        }

                        @Override
                        public void onSuccess(Object result) {
                            Log.d("CourseManager", "Course joined");
                            runOnUiThread(() -> {
                                btnJoin.setVisibility(View.GONE); // Ẩn nút Join
                                // Hiển thị nền tối
                                View darkOverlay = findViewById(R.id.darkOverlay);
                                darkOverlay.setVisibility(View.VISIBLE);

                                // Hiển thị GIF và thông báo
                                ImageView imgSuccessGif = findViewById(R.id.imgSuccessGif);
                                TextView tvSuccessMessage = findViewById(R.id.tvSuccessMessage);

                                imgSuccessGif.setVisibility(View.VISIBLE);
                                tvSuccessMessage.setVisibility(View.VISIBLE);

                                // Load GIF bằng Glide
                                Glide.with(CourseInformationActivity.this)
                                        .asGif()
                                        .load(R.raw.like)
                                        .into(imgSuccessGif);

                                // Tự động chuyển đến Study sau vài giây
                                new Handler().postDelayed(() -> {
                                    Intent intent = new Intent(CourseInformationActivity.this, HomeActivity.class);
                                    intent.putExtra("targetPage", 1);
                                    startActivity(intent);
                                }, 3000);
                            });

                        }
                        @Override
                        public void onFailure(String errorMessage) {
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    resultManager.createEnrollment(courseID, new ApiCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                btnJoin.setVisibility(View.GONE); // Ẩn nút Join
                                // Hiển thị nền tối
                                View darkOverlay = findViewById(R.id.darkOverlay);
                                darkOverlay.setVisibility(View.VISIBLE);

                                // Hiển thị GIF và thông báo
                                ImageView imgSuccessGif = findViewById(R.id.imgSuccessGif);
                                TextView tvSuccessMessage = findViewById(R.id.tvSuccessMessage);

                                imgSuccessGif.setVisibility(View.VISIBLE);
                                tvSuccessMessage.setVisibility(View.VISIBLE);

                                // Load GIF bằng Glide
                                Glide.with(CourseInformationActivity.this)
                                        .asGif()
                                        .load(R.raw.like)
                                        .into(imgSuccessGif);

                                // Tự động chuyển đến Study sau vài giây
                                new Handler().postDelayed(() -> {
                                    Intent intent = new Intent(CourseInformationActivity.this, HomeActivity.class);
                                    intent.putExtra("targetPage", 1);
                                    startActivity(intent);
                                }, 3000);
                            });
                        }

                        @Override
                        public void onSuccess(Object result) {

                        }

                        @Override
                        public void onFailure(String errorMessage) {
                        }
                    });
                }
            });


        }

        private void khaiBao() {
        courseID = getIntent().getIntExtra("courseId",1);
        // Ánh xạ views
        btnAbout = findViewById(R.id.btnAbout);
        btnBackto= findViewById(R.id.btnBackto);
        numLessons = findViewById(R.id.numLessons);
        btnLesson = findViewById(R.id.btnLesson);
        contentAbout = findViewById(R.id.contentAbout);
        recyclerViewLesson = findViewById(R.id.recyclerViewLessons);
        courseName = findViewById(R.id.courseName);
        txtContentAbout = findViewById(R.id.txtContentAbout);
        recyclerView = findViewById(R.id.recyclerViewDiscussion);
        btnJoin = findViewById(R.id.btnJoin);
        reviewAdapter = new ReviewAdapter(CourseInformationActivity.this, reviews);
        recyclerView.setAdapter(reviewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contentAbout.setVisibility(View.VISIBLE);
        recyclerViewLesson.setVisibility(View.GONE);

    }

    public void getCourseInfo(int courseId, ApiCallback<Course> callback) {
        courseManager.fetchCourseById(courseId, new ApiCallback<Course>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(Course course) {
                runOnUiThread(() -> callback.onSuccess(course));

            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> callback.onFailure(errorMessage));
            }
        });
    }

    private void loadLessons(List<Integer> lessonIds) {
        List<Lesson> lessons = new ArrayList<>();

        if (lessonIds == null || lessonIds.isEmpty()) {
            Log.w("CourseActivity", "Không có bài học nào trong khóa học.");
            return;
        }

        for (int lessonId : lessonIds) {
            lessonManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
                @Override
                public void onSuccess() {}

                @Override
                public void onSuccess(Lesson lesson) {
                    runOnUiThread(() -> {
                        lessons.add(lesson);

                        // Khi đã tải xong tất cả bài học, cập nhật adapter
                        if (lessons.size() == lessonIds.size()) {
                            lessonAdapter.setLessons(lessons);
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("LessonLoad", "Lỗi tải bài học: " + errorMessage);
                }
            });
        }
    }

        private void loadReviews() {
            reviewManager.fetchReviewsByCourse(courseID, currentPage, new ApiCallback<List<Review>>() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onSuccess(List<Review> reviews) {
                    runOnUiThread(() -> {
                        if (reviews == null || reviews.isEmpty()) {
                            hasMoreData = false;
                            Log.d("CourseInformationActivity", "List review null 1");
                            return;
                        }
                        else {
                            Log.d("CourseInformationActivity", "reviewSize: " );
                            reviewAdapter.addMoreReviews(reviews);

                        }

                        currentPage++;
                        isLoading = false;
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() ->
                            Toast.makeText(CourseInformationActivity.this, "Lỗi tải đánh giá: " + errorMessage, Toast.LENGTH_SHORT).show());
                }
            });
        }
}