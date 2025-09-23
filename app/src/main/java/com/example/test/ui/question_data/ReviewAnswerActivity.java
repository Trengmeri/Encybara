package com.example.test.ui.question_data;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.ApiService;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Answer;
import com.example.test.model.Course;
import com.example.test.model.EvaluationResult;
import com.example.test.model.Lesson;
import com.example.test.model.Question;
import com.example.test.model.QuestionChoice;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewAnswerActivity extends AppCompatActivity {
    LinearLayout lessonsContainer;
    TextView courseTitle,lessonTitle;
    ImageView btnBackto;
    LessonManager lesManager = new LessonManager();
    QuestionManager quesManager = new QuestionManager(this);
    ResultManager resultManager = new ResultManager(this);
    int lessonId, courseID;
    TableLayout tableTestResult;
    ApiService apiService;
    private Dialog loadingDialog;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_answer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        courseTitle = findViewById(R.id.courseTitle);
        btnBackto = findViewById(R.id.btnBackto);
//        lessonsContainer = findViewById(R.id.lessonsContainer);
        tableTestResult= findViewById(R.id.tableTestResult);
        Log.d("ReviewAnswerActivity", "Đã vào màn này ") ;
        btnBackto.setOnClickListener(v -> {
            finish();
        });
        courseID = getIntent().getIntExtra("courseId",1);
        fetchCourseData(courseID);
        apiService= new ApiService(this);
        // Khởi tạo Dialog loading
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false); // Không cho phép đóng khi chạm ngoài màn hình
    }

//    private LinearLayout addLessonLayout(String lessonName) {
//        LinearLayout lessonLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.item_lesson, null);
//        TextView lessonTitleTextView = lessonLayout.findViewById(R.id.lessonTitle);
//        lessonTitleTextView.setText(lessonName);
//        lessonsContainer.addView(lessonLayout);
//        return lessonLayout; // Trả về lessonLayout
//    }

    private void fetchCourseData(int courseId) {
        lesManager.fetchCourseById(courseId, new ApiCallback<Course>() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onSuccess(Course course) {
                runOnUiThread(() -> {
                    if (course!= null) {
                        courseTitle.setText(course.getName());

                        List<Integer> lessonIds = course.getLessonIds();

                        for (int i =0 ; i< lessonIds.size(); i++) {
                            lessonId = lessonIds.get(i);
                            fetchLessonData(lessonId);
                            Log.e("LessonId: ", lessonIds.get(i).toString());
                        }
                    } else {
                        Log.e("ReviewAnswerActivity", "Không có khóa học nào.");
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }

//    private void fetchLessonData(int lessonId) {
//        lesManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onSuccess(Lesson lesson) {
//                runOnUiThread(() -> {
//                    if (lesson!= null && lesson.getQuestionIds()!= null) {
//                        String lessonContext = lesson.getName() + ": " + lesson.getSkillType();
//                        //LinearLayout lessonLayout = addLessonLayout(lessonContext);
//                        LinearLayout questionsContainer = lessonLayout.findViewById(R.id.questionsContainer);
//                        for (Integer questionId: lesson.getQuestionIds()) {
//                            fetchQuestionAndAnswer(questionId, questionsContainer);
//                        }
//                    } else {
//                        Log.e("ReviewAnswerActivity", "Không có câu hỏi nào.");
//                    }
//                });
//            }
//
//
//            @Override
//            public void onFailure(String errorMessage) {
//
//            }
//
//        });
//    }
private void fetchLessonData(int lessonId) {
    lesManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onSuccess(Lesson lesson) {
            runOnUiThread(() -> {
                if (lesson!= null && lesson.getQuestionIds()!= null) {
//                    LinearLayout questionsContainer = findViewById(R.id.questionsContainer);
                    for (Integer questionId: lesson.getQuestionIds()) {
                        fetchQuestionAndAnswer(questionId, tableTestResult);
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
                            TableRow row = new TableRow(tableTestResult.getContext());

                            // TextView cho câu hỏi + đáp án đúng
                            TextView questionTextView = new TextView(tableTestResult.getContext());
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
                                                    new AlertDialog.Builder(tableTestResult.getContext())
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
                                                    new AlertDialog.Builder(tableTestResult.getContext())
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
                            TextView userAnswerTextView = new TextView(tableTestResult.getContext());
                            String userAnswer = answer.getAnswerContent().trim();

                            // Chuyển câu trả lời của người dùng thành danh sách và sắp xếp
//                            List<String> userAnswers = Arrays.stream(userAnswer.split(","))
//                                    .map(String::trim) // Loại bỏ khoảng trắng
//                                    .sorted() // Sắp xếp theo bảng chữ cái
//                                    .collect(Collectors.toList());
//                            String userAnswerFormatted = String.join(", ", userAnswers);
                            userAnswerTextView.setText(userAnswer);
                            userAnswerTextView.setPadding(10, 10, 10, 10);
                            userAnswerTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                            userAnswerTextView.setTypeface(null, Typeface.BOLD);

                            // TextView cho điểm số
                            TextView pointTextView = new TextView(tableTestResult.getContext());
                            pointTextView.setText(String.valueOf(answer.getPointAchieved()));
                            pointTextView.setPadding(10, 10, 10, 10);
                            pointTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                            pointTextView.setGravity(Gravity.CENTER);
                            pointTextView.setTypeface(null, Typeface.BOLD);

                            // Đổi màu chữ tùy theo đúng/sai
                            if (!userAnswer.isEmpty() && !correctAnswers.isEmpty() &&
                                    userAnswer.trim().equalsIgnoreCase(correctAnswers.get(0).trim())) {
                                userAnswerTextView.setTextColor(ContextCompat.getColor(tableTestResult.getContext(), android.R.color.holo_green_dark));
                                pointTextView.setTextColor(ContextCompat.getColor(tableTestResult.getContext(), android.R.color.holo_green_dark));
                            } else {
                                userAnswerTextView.setTextColor(ContextCompat.getColor(tableTestResult.getContext(), android.R.color.holo_red_dark));
                                pointTextView.setTextColor(ContextCompat.getColor(tableTestResult.getContext(), android.R.color.holo_red_dark));
                            }
                            // Thêm các TextView vào hàng
                            row.addView(questionTextView);
                            row.addView(userAnswerTextView);
                            row.addView(pointTextView);

                            // Thêm hàng vào bảng
                            tableTestResult.addView(row);
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