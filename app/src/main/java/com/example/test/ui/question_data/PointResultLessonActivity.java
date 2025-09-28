package com.example.test.ui.question_data;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.test.NevigateQuestion;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.ApiService;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Answer;
import com.example.test.model.Course;
import com.example.test.model.Enrollment;
import com.example.test.model.EvaluationResult;
import com.example.test.model.Lesson;
import com.example.test.model.Question;
import com.example.test.model.QuestionChoice;
import com.example.test.model.Result;
import com.example.test.ui.DiscussionActivity;
import com.example.test.ui.home.HomeActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PointResultLessonActivity extends AppCompatActivity {

    Button btnDone,btnDiscuss, btnNext;
    private int lessonID,courseID, enrollmentId;
    QuestionManager quesManager = new QuestionManager(this);
    LessonManager lesManager = new LessonManager();
    ResultManager resultManager = new ResultManager(this);
    TextView point, tvSuccessMessage;
    ImageView star1,star2,star3, imgSuccessGif;
    View darkOverlay;
    TableLayout tableResult;
    // Set lưu các ID đã gọi để tránh gọi trùng
    private Set<Integer> calledAnswerIds = new HashSet<>();
    ApiService apiService;
    private Dialog loadingDialog;
//    public static final String EXTRA_MODE = "mode";
//    //public static final int MODE_TEST = 1; // Test đầu vào
//    public static final int MODE_COURSE = 2; // Khóa học bình thường

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_point_result_lesson);
        AnhXa();

        courseID = getIntent().getIntExtra("courseId",1);
        lessonID = getIntent().getIntExtra("lessonId",1);
        enrollmentId = getIntent().getIntExtra("enrollmentId", 1);

        apiService= new ApiService(this);
        // Khởi tạo Dialog loading
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false); // Không cho phép đóng khi chạm ngoài màn hình

        Log.e("point","Lesson ID: "+ lessonID + "courseID: "+ courseID);

        fetchCourseData(courseID,lessonID);
        fetchLessonData(lessonID);
        makepoint(courseID,lessonID);
        btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(PointResultLessonActivity.this, HomeActivity.class);
            intent.putExtra("targetPage", 0);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnDiscuss.setOnClickListener(v -> {
            Intent intent = new Intent(PointResultLessonActivity.this, DiscussionActivity.class);
            intent.putExtra("lessonId", lessonID);
            startActivity(intent);
        });
    }

    public void AnhXa(){
        btnDone = findViewById(R.id.btnDone);
        btnNext = findViewById(R.id.btnNext);
        point = findViewById(R.id.point);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        btnDiscuss=findViewById(R.id.btnDiscuss);
        tableResult= findViewById(R.id.tableResult);
        // Hiển thị nền tối
        darkOverlay = findViewById(R.id.darkOverlay);
        darkOverlay.setVisibility(View.VISIBLE);

        // Hiển thị GIF và thông báo
        imgSuccessGif = findViewById(R.id.imgSuccessGif);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);

        imgSuccessGif.setVisibility(View.VISIBLE);
        tvSuccessMessage.setVisibility(View.VISIBLE);

        // Load GIF bằng Glide
        Glide.with(PointResultLessonActivity.this)
                .asGif()
                .load(R.raw.dragon_like)
                .into(imgSuccessGif);
    }

    private void makepoint(int courseId, int lessonId) {
        lesManager.fetchCourseById(courseId, new ApiCallback<Course>() {
            @Override
            public void onSuccess() {}

            @Override
            public void onSuccess(Course course) {
                if (course != null) {
                    Integer maxLessonId = course.getLessonIds().stream().max(Integer::compareTo).orElse(lessonId);
                    if (maxLessonId > lessonId) {
                        runOnUiThread(() -> btnNext.setText(getString(R.string.nextques)));
                        int lesid = lessonId +1;
                        lesManager.fetchLessonById(lesid, new ApiCallback<Lesson>(){

                            @Override
                            public void onSuccess() {

                            }
                            @Override
                            public void onSuccess(Lesson lesson) {
                                btnNext.setOnClickListener(view -> {
                                    Intent intent = new Intent(PointResultLessonActivity.this, NevigateQuestion.class);
                                    intent.putExtra("courseId", course.getId());
                                    intent.putExtra("lessonId", lesid);
                                    intent.putExtra("enrollmentId", enrollmentId);
                                    intent.putExtra("questionIds", new ArrayList<>(lesson.getQuestionIds()));
                                    startActivity(intent);
                                    finish();
                                });
                            }
                            @Override
                            public void onFailure(String errorMessage) {

                            }
                        });

                    } else {
                        runOnUiThread(() -> {
                            btnNext.setText(getString(R.string.viewpointcourse));
                            btnDone.setVisibility(View.GONE);
                            btnNext.setOnClickListener(view -> {
                                Intent intent = new Intent(PointResultLessonActivity.this, PointResultCourseActivity.class);
                                intent.putExtra("courseId", course.getId());
                                intent.putExtra("status", "study");
                                intent.putExtra("EXTRA_MODE", "MODE_COURSE");
                                startActivity(intent);
                                finish();
                            });
                        });
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("makepoint", "Error: " + errorMessage);
            }
        });
    }


    private void fetchCourseData(int courseId, int lessonId) {

                resultManager.getEnrollment(courseId, new ApiCallback<Enrollment>() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onSuccess(Enrollment enrollment) {
                        if (enrollment != null) {
                            int enrollmentId = enrollment.getId();
                            Log.e("ErollmentId: ", String.valueOf(enrollment.getId()));
                            lesManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onSuccess(Lesson lesson) {
                                    if (lesson != null && lesson.getSkillType() != null) {
                                        if (lesson != null && lesson.getQuestionIds() != null) {
                                            for (Integer questionId : lesson.getQuestionIds()) {
                                                resultManager.fetchAnswerPointsByQuesId(questionId, new ApiCallback<Answer>() {
                                                    @Override
                                                    public void onSuccess() {
                                                    }

                                                    @Override
                                                    public void onSuccess(Answer answer) {
                                                        if (answer != null) {
                                                            createResultForLesson(lessonId, answer.getSessionId(), enrollmentId);
                                                        } else {
                                                            Log.e("PointResultActivity", "Không có câu trả lời nào.");
                                                        }
                                                    }


                                                    @Override
                                                    public void onFailure(String errorMessage) {
                                                        Log.e("PointResultActivity", errorMessage);
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


    private void createResultForLesson(int lessonId, int sessionId, int enrollmentId) {
        resultManager.createResult(lessonId, sessionId, enrollmentId, new ApiCallback() {
            @Override
            public void onSuccess() {
                Log.d("PointResultActivity", "createResultForLesson: Gọi fetchResultByLesson"); // Log trước khi gọi fetchResultByLesson
                resultManager.fetchResultByLesson(lessonId, new ApiCallback<Result>() {
                    @Override
                    public void onSuccess() {}

                    @SuppressLint("UseCompatLoadingForColorStateLists")
                    @Override
                    public void onSuccess(Result result) {
                        if (result!= null) {
                            Log.d("PointResultActivity", "fetchResultByLesson: Lấy Result thành công");
                            runOnUiThread(() -> {
                                point.setText(String.valueOf(result.getTotalPoints()));
                                imgSuccessGif.setVisibility(View.GONE);
                                tvSuccessMessage.setVisibility(View.GONE);
                                darkOverlay.setVisibility(View.GONE);
                                if (result.getComLevel() > 90) {
                                    star3.setBackgroundTintList(getResources().getColorStateList(R.color.yellow));
                                }
                                if (result.getComLevel() > 60) {
                                    star2.setBackgroundTintList(getResources().getColorStateList(R.color.yellow));
                                }
                                if (result.getComLevel() > 30) {
                                    star1.setBackgroundTintList(getResources().getColorStateList(R.color.yellow));
                                }
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

    private void fetchLessonData(int lessonId) {
        lesManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(Lesson lesson) {
                runOnUiThread(() -> {
                    if (lesson!= null && lesson.getQuestionIds()!= null) {
                        LinearLayout questionsContainer = findViewById(R.id.questionsContainer);
                        for (Integer questionId: lesson.getQuestionIds()) {
                            fetchQuestionAndAnswer(questionId, tableResult);
                        }
                    } else {
                        Log.e("ReviewAnswerActivity", "Không có câu hỏi nào.");
                    }
                });
            }


            @Override
            public void onFailure(String errorMessage) {

            }

        });
    }

    private void fetchQuestionAndAnswer(int questionId, LinearLayout questionsContainer) {
        quesManager.fetchQuestionContentFromApi(questionId, new ApiCallback<Question>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(Question question) {
                resultManager.fetchAnswerPointsByQuesId(questionId, new ApiCallback<Answer>() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onSuccess(Answer answer) {
                        runOnUiThread(() -> {

                            // Tạo một hàng mới cho bảng
                            TableRow row = new TableRow(tableResult.getContext());

                            // TextView cho câu hỏi + đáp án đúng
                            TextView questionTextView = new TextView(tableResult.getContext());
                            // Tạo SpannableStringBuilder để thay đổi màu chữ trong cùng một TextView
                            SpannableStringBuilder spannable = new SpannableStringBuilder();

                            // Thêm nội dung câu hỏi với định dạng in đậm
                            spannable.append("Q: ").append(question.getQuesContent()).append("\n");

                            // Định dạng "Correct:" với màu xanh
                            int correctStart = spannable.length();
                            spannable.append("Correct: ");
                            int correctEnd = spannable.length();
                            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")), correctStart, correctEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannable.setSpan(new RelativeSizeSpan(0.8f), correctStart, correctEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Cỡ chữ nhỏ hơn
                            // Lấy danh sách đáp án đúng và sắp xếp lại
                            List<String> correctAnswers = question.getQuestionChoices()
                                    .stream()
                                    .filter(QuestionChoice::isChoiceKey)
                                    .map(QuestionChoice::getChoiceContent)
                                    .sorted() // Sắp xếp theo bảng chữ cái
                                    .collect(Collectors.toList());
                            String correctAnswerString = correctAnswers.isEmpty() ? "Improvement suggestions" : String.join(", ", correctAnswers);
                            // Định dạng đáp án đúng với màu xanh
                            int answerStart = spannable.length();
                            spannable.append(correctAnswerString);
                            int answerEnd = spannable.length();

                            if (correctAnswers.isEmpty()) {
                                // Tạo ClickableSpan để bắt sự kiện nhấn vào
                                ClickableSpan clickableSpan = new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        // Gọi API sendAnswerToAPI để lấy improvements
                                        showLoading();
                                        apiService.sendAnswerToApi(question.getQuesContent(), answer.getAnswerContent(), new ApiCallback<EvaluationResult>() {
                                                    @Override
                                                    public void onSuccess(EvaluationResult result) {
                                                        runOnUiThread(() -> {
                                                            hideLoading();
                                                            // Hiển thị improvements trong AlertDialog
                                                            new AlertDialog.Builder(tableResult.getContext())
                                                                    .setTitle("Improvement Suggestions")
                                                                    .setMessage(result.getimprovements() != null && !result.getimprovements().isEmpty()
                                                                            ? result.getimprovements()
                                                                            : "No suggestion available.")
                                                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                                                    .show();
                                                        });
                                                    }

                                                    @Override
                                                    public void onSuccess() {}

                                                    @Override
                                                    public void onFailure(String errorMessage) {
                                                        runOnUiThread(() -> {
                                                            // Hiển thị lỗi nếu API thất bại
                                                            new AlertDialog.Builder(tableResult.getContext())
                                                                    .setTitle("Error")
                                                                    .setMessage("Failed to load suggestions: " + errorMessage)
                                                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                                                    .show();
                                                        });
                                                    }
                                                });
                                    }

                                    @Override
                                    public void updateDrawState(@NonNull TextPaint ds) {
                                        super.updateDrawState(ds);
                                        ds.setColor(Color.BLUE);
                                        ds.setUnderlineText(true);
                                    }
                                };
                                spannable.setSpan(clickableSpan, answerStart, answerEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")), answerStart, answerEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannable.setSpan(new RelativeSizeSpan(0.8f), answerStart, answerEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Cỡ chữ nhỏ hơn
                            questionTextView.setText(spannable);
                            questionTextView.setPadding(10, 10, 10, 10);
                            questionTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
                            questionTextView.setTypeface(null, Typeface.BOLD);
                            questionTextView.setMovementMethod(LinkMovementMethod.getInstance());
                            // TextView cho câu trả lời của người dùng
                            TextView userAnswerTextView = new TextView(tableResult.getContext());
                            String userAnswer = answer.getAnswerContent().trim();

                            // Chuyển câu trả lời của người dùng thành danh sách và sắp xếp
//                            List<String> userAnswers = Arrays.stream(userAnswer.split(","))
//                                    .map(String::trim) // Loại bỏ khoảng trắng
//                                    .sorted() // Sắp xếp theo bảng chữ cái
//                                    .collect(Collectors.toList());
                            //String userAnswerFormatted = String.join(", ", userAnswers);
                            //String userAnswerFormatted = answer.getAnswerContent().trim();

                            userAnswerTextView.setText(userAnswer);
                            userAnswerTextView.setPadding(10, 10, 10, 10);
                            userAnswerTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                            userAnswerTextView.setTypeface(null, Typeface.BOLD);

                            // TextView cho điểm số
                            TextView pointTextView = new TextView(tableResult.getContext());
                            pointTextView.setText(String.valueOf(answer.getPointAchieved()));
                            pointTextView.setPadding(10, 10, 10, 10);
                            pointTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                            pointTextView.setGravity(Gravity.CENTER);
                            pointTextView.setTypeface(null, Typeface.BOLD);

                            // Đổi màu chữ tùy theo đúng/sai
                            if (!userAnswer.isEmpty() && !correctAnswers.isEmpty() &&
                                    userAnswer.trim().equalsIgnoreCase(correctAnswers.get(0).trim())) {
                                userAnswerTextView.setTextColor(ContextCompat.getColor(tableResult.getContext(), android.R.color.holo_green_dark));
                                pointTextView.setTextColor(ContextCompat.getColor(tableResult.getContext(), android.R.color.holo_green_dark));
                            } else {
                                userAnswerTextView.setTextColor(ContextCompat.getColor(tableResult.getContext(), android.R.color.holo_red_dark));
                                pointTextView.setTextColor(ContextCompat.getColor(tableResult.getContext(), android.R.color.holo_red_dark));
                            }

                            // Thêm các TextView vào hàng
                            row.addView(questionTextView);
                            row.addView(userAnswerTextView);
                            row.addView(pointTextView);

                            // Thêm hàng vào bảng
                            tableResult.addView(row);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {

                    }

                });
            }

            @Override
            public void onFailure(String errorMessage) {

            }

        });
    }
    private void showLoading() {
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void hideLoading() {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}