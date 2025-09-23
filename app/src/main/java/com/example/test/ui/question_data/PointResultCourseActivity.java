package com.example.test.ui.question_data;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.test.R;
import com.example.test.api.ApiService;
import com.example.test.model.Enrollment;
import com.example.test.ui.CourseInformationActivity;
import com.example.test.ui.ReviewActivity;
import com.example.test.ui.home.HomeActivity;
import com.example.test.api.ApiCallback;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Answer;
import com.example.test.model.Course;
import com.example.test.model.Lesson;
import com.example.test.model.Result;

import java.util.HashSet;
import java.util.Set;

public class PointResultCourseActivity extends AppCompatActivity {
    private LinearLayout readingSkillLayout, listeningSkillLayout, speakingSkillLayout, writingSkillLayout;
    private TextView pointTextView, totalComp;
    private Button btnReview, btnNext;
    private TextView correctRead, compRead;
    private TextView correctLis, compLis;
    private TextView correctSpeak, compSpeak, tvSuccessMessage;
    private TextView correctWrite, compWrite;
    private int totalPointR = 0,totalPointL = 0,totalPointS = 0,totalPointW = 0;
    private int r =0,l=0,s=0,w=0;
    private double compCourse;
    private int coursePoint;
    private double comR, comL, comS, comW;
    ImageView star1,star2,star3, imgSuccessGif;
    QuestionManager quesManager = new QuestionManager(this);
    LessonManager lesManager = new LessonManager();
    ApiService apiService = new ApiService(this);
    ResultManager resultManager = new ResultManager(this);
    private Set<Integer> addedResultIds = new HashSet<>();
    private int enrollmentId, courseID;
    private String status;
    private Handler handler = new Handler();
    private Runnable callApiRunnable;
    private static final int DELAY_MILLIS = 1500; // 1,5 giây
    View darkOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_result);

        initializeViews();
        courseID = getIntent().getIntExtra("courseId",1);
        status = getIntent().getStringExtra("status");
        fetchCourseData(courseID);

        String mode = getIntent().getStringExtra("EXTRA_MODE");

        btnReview.setOnClickListener(v -> {
//            Intent intent = new Intent(PointResultCourseActivity.this, ReviewActivity.class);
//            intent.putExtra("courseId", courseID);
//            startActivity(intent);
            if ("MODE_TEST".equals(mode)) {
                // sang ReviewAnswer
                Intent intent = new Intent(PointResultCourseActivity.this, ReviewAnswerActivity.class);
                startActivity(intent);
            } else if ("MODE_COURSE".equals(mode)) {
                // sang ReviewCourse
                Intent intent = new Intent(PointResultCourseActivity.this, ReviewActivity.class);
                intent.putExtra("CourseID",courseID);
                startActivity(intent);
            }
        });

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(PointResultCourseActivity.this, HomeActivity.class);
            intent.putExtra("targetPage", 0);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });


    }
    private void initializeViews() {
        pointTextView = findViewById(R.id.point);
        btnReview = findViewById(R.id.btnReview);
        btnNext = findViewById(R.id.btnNext);
        correctRead = findViewById(R.id.correct_read);
        compRead = findViewById(R.id.comp_read);
        correctLis = findViewById(R.id.correct_lis);
        compLis = findViewById(R.id.comp_lis);
        correctSpeak = findViewById(R.id.correct_speak);
        compSpeak = findViewById(R.id.comp_speak);
        correctWrite = findViewById(R.id.correct_write);
        compWrite = findViewById(R.id.comp_write);
        readingSkillLayout = findViewById(R.id.readingSkillLayout);
        listeningSkillLayout = findViewById(R.id.listeningSkillLayout);
        speakingSkillLayout = findViewById(R.id.speakingSkillLayout);
        writingSkillLayout = findViewById(R.id.writingSkillLayout);
        readingSkillLayout.setVisibility(View.GONE);
        listeningSkillLayout.setVisibility(View.GONE);
        speakingSkillLayout.setVisibility(View.GONE);
        writingSkillLayout.setVisibility(View.GONE);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);

        // Hiển thị nền tối
        darkOverlay = findViewById(R.id.darkOverlay);
        darkOverlay.setVisibility(View.VISIBLE);

        // Hiển thị GIF và thông báo
        imgSuccessGif = findViewById(R.id.imgSuccessGif);
         tvSuccessMessage = findViewById(R.id.tvSuccessMessage);

        imgSuccessGif.setVisibility(View.VISIBLE);
        tvSuccessMessage.setVisibility(View.VISIBLE);

        // Load GIF bằng Glide
        Glide.with(PointResultCourseActivity.this)
                .asGif()
                .load(R.raw.butterfly)
                .into(imgSuccessGif);
    }

    private void fetchCourseData(int courseId) {
                lesManager.fetchCourseById(courseId,new ApiCallback<Course>() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onSuccess(Course course) {
                        runOnUiThread(() -> {
                            if (course!= null && course.getLessonIds()!= null) {
                                int courseId = course.getId();
                                for (Integer lessonId: course.getLessonIds()) {
                                    fetchLessonAndCreateResult(lessonId,courseId);
                                }
                            } else {
                                Log.e("PointResultActivity","Không có khóa học nào.");
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e("PointResultActivity",errorMessage);
                    }
                });
    }

    private void fetchLessonAndCreateResult(int lessonId, int courseId) {
        resultManager.getEnrollment(courseId, new ApiCallback<Enrollment>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(Enrollment enrollment) {
                if(enrollment != null){
                    enrollmentId = enrollment.getId();
                    Log.e("ErollmentId: ", String.valueOf(enrollment.getId()));
                    lesManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onSuccess(Lesson lesson) {
                            if (lesson!= null && lesson.getSkillType()!= null) {
                                String skillType = lesson.getSkillType();
                                if (lesson!= null && lesson.getQuestionIds()!= null) {
                                    for (Integer questionId : lesson.getQuestionIds()) {
                                        resultManager.fetchAnswerPointsByQuesId(questionId, new ApiCallback<Answer>() {
                                            @Override
                                            public void onSuccess() {
                                            }

                                            @Override
                                            public void onSuccess(Answer answer) {
                                                if (answer != null) {
                                                    createResultForLesson(lessonId, answer.getSessionId(), enrollmentId, skillType);
                                                } else {
                                                    Log.e("PointResultActivity", "Không có câu trả lời nào.");
                                                }
                                            }


                                            @Override
                                            public void onFailure(String errorMessage) {
                                                Log.e("PointResultActivity",errorMessage);
                                            }

                                        });
                                    }
                                }
                            } else {
                                Log.e("PointResultActivity", "Bài học hoặc skillType không hợp lệ.");
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.e("PointResultActivity", errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {

            }

        });

    }

    private void createResultForLesson(int lessonId, int sessionId, int enrollmentId, String skillType) {
        if(status.equals("study")){
            resultManager.fetchResultByLesson(lessonId, new ApiCallback<Result>() {
                @Override
                public void onSuccess() {}

                @Override
                @SuppressLint("UseCompatLoadingForColorStateLists")
                public void onSuccess(Result result) {
                    if (result!= null) {
                        Log.d("PointResultActivity", "fetchResultByLesson: Lấy Result thành công");
                        runOnUiThread(() -> {
                            updateUI(skillType, result.getComLevel(), result.getTotalPoints(), result.getId(), enrollmentId);
                        });
                    } else {
                        Log.e("PointResultActivity", "fetchResultByLesson: Kết quả không hợp lệ.");
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("PointResultActivity", "fetchResultByLesson: " + errorMessage);
                }
            });
        } else {
            resultManager.createResult(lessonId, sessionId, enrollmentId, new ApiCallback() {
                @Override
                public void onSuccess() {
                    Log.d("PointResultActivity", "createResultForLesson: Gọi fetchResultByLesson"); // Log trước khi gọi fetchResultByLesson
                    resultManager.fetchResultByLesson(lessonId, new ApiCallback<Result>() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        @SuppressLint("UseCompatLoadingForColorStateLists")
                        public void onSuccess(Result result) {
                            if (result!= null) {
                                Log.d("PointResultActivity", "fetchResultByLesson: Lấy Result thành công");
                                runOnUiThread(() -> {
                                    updateUI(skillType, result.getComLevel(), result.getTotalPoints(), result.getId(), enrollmentId);
                                });
                            } else {
                                Log.e("PointResultActivity", "fetchResultByLesson: Kết quả không hợp lệ.");
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.e("PointResultActivity", "fetchResultByLesson: " + errorMessage);
                        }
                    });
                }

                @Override
                public void onSuccess(Object result){}


                @Override
                public void onFailure(String errorMessage) {
                    Log.e("PointResultActivity",errorMessage);
                }


            });
        }
    }

    private void updateUI(String skillType, double complete, int totalPoints, int resultId, int enrollmentId) {
        runOnUiThread(() -> {
            if (!addedResultIds.contains(resultId)) {
                addedResultIds.add(resultId);

                switch (skillType) {
                    case "READING":
                        totalPointR += totalPoints;
                        comR += complete;
                        r++;
                        readingSkillLayout.setVisibility(View.VISIBLE);
                        break;
                    case "LISTENING":
                        totalPointL += totalPoints;
                        comL += complete;
                        l++;
                        listeningSkillLayout.setVisibility(View.VISIBLE);
                        break;
                    case "SPEAKING":
                        totalPointS += totalPoints;
                        comS += complete;
                        s++;
                        speakingSkillLayout.setVisibility(View.VISIBLE);
                        break;
                    case "WRITING":
                        totalPointW += totalPoints;
                        comW += complete;
                        w++;
                        writingSkillLayout.setVisibility(View.VISIBLE);
                        break;
                    default:
                        Log.e("PointResultActivity", "Skill type không hợp lệ: " + skillType);
                }
            }

            correctRead.setText(getString(R.string.point)  + totalPointR);
            compRead.setText(getString(R.string.comp) + String.format("%.1f", comR / r));
            correctLis.setText(getString(R.string.point) + totalPointL);
            compLis.setText(getString(R.string.comp) + String.format("%.1f", comL / l));
            correctSpeak.setText(getString(R.string.point) + totalPointS);
            compSpeak.setText(getString(R.string.comp) + String.format("%.1f", comS / s));
            correctWrite.setText(getString(R.string.point)+ totalPointW);
            compWrite.setText(getString(R.string.comp) + String.format("%.1f", comW / w));

            // Hủy bất kỳ API call nào đang chờ trước đó
            if (callApiRunnable != null) {
                handler.removeCallbacks(callApiRunnable);
            }

            // Đặt lại thời gian chờ, nếu không có thêm dữ liệu trong 2 giây => gọi API
            callApiRunnable = () -> {

                    callCompleteTestApi(enrollmentId, totalPointR,totalPointL,totalPointS, totalPointW);

            };
            handler.postDelayed(callApiRunnable, DELAY_MILLIS);
        });
    }

    private void callCompleteTestApi(int enrollmentId, int totalPointR,int totalPointL,int totalPointS,int totalPointW) {
        apiService.saveEnrollment(enrollmentId, new ApiCallback<Enrollment>(){
            @Override
            public void onSuccess() {}

            @Override
            public void onSuccess(Enrollment enrollment) {
                Log.d("saveEnrollment", "save enrollment thanh cong");
                compCourse = enrollment.getComLevel();
                coursePoint = enrollment.getTotalPoints();
                runOnUiThread(() -> {
                    if (compCourse > 90) {
                        star3.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.yellow)));

                    }
                    if (compCourse > 60) {
                        star2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.yellow)));

                    }
                    if (compCourse > 30) {
                        star1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.yellow)));

                    }
                    pointTextView.setText(String.valueOf(coursePoint));

                    imgSuccessGif.setVisibility(View.GONE);
                    tvSuccessMessage.setVisibility(View.GONE);
                    darkOverlay.setVisibility(View.GONE);

                    if (status.equals("test")) {
                        apiService.completeTest(enrollmentId, compCourse, coursePoint,
                                totalPointR, totalPointL, totalPointS, totalPointW,
                                new ApiCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d("PointResultActivity", "Hoàn thành bài kiểm tra thành công");
                                    }

                                    @Override
                                    public void onSuccess(Object result) {
                                        Log.d("PointResultActivity", "Kết quả bài kiểm tra: " + result.toString());
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Log.e("PointResultActivity", "Lỗi khi hoàn thành bài kiểm tra: " + errorMessage);
                                    }
                                });
                    } else {
                        apiService.updateResult(enrollmentId, new ApiCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("update result", "update result thanh cong");
                                apiService.recomment(enrollmentId, new ApiCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d("recommnet", "recomment enrollment thanh cong");
                                    }

                                    @Override
                                    public void onSuccess(Object result) {}

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Log.d("recomment", errorMessage);
                                    }
                                });
                            }

                            @Override
                            public void onSuccess(Object result) {

                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Log.d("update result", errorMessage);
                            }
                        });
                    }


                });
            }

                @Override
                public void onFailure(String errorMessage) {
                    Log.d("saveEnrollment", errorMessage);
                }


        });
    }
}