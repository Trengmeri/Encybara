package com.example.test.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.NevigateQuestion;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.LessonManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Course;
import com.example.test.model.Enrollment;
import com.example.test.model.Lesson;
import com.example.test.model.Result;
import com.example.test.ui.CourseInformationActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private Context context;
    private List<Course> courseList;
    private String proStatus;
    private LessonManager lessonManager = new LessonManager();
    private ResultManager resultManager;

    public CourseAdapter(String proStatus, Context context, List<Course> courseList) {
        this.proStatus = proStatus;
        this.context = context;
        this.courseList = courseList != null
                ? courseList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(Course::getId))
                .collect(Collectors.toList())
                : new ArrayList<>();
        this.resultManager = new ResultManager(context);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {

        Course course = courseList.get(position);
        if (course == null) {
            return; // Tránh lỗi khi đối tượng bị null
        }
        holder.tvCourseTitle.setText(course.getName());
        holder.tvCourseDescription.setText(course.getIntro());
        holder.tvCourseDescription.setMaxLines(3);
        holder.tvCourseDescription.setEllipsize(TextUtils.TruncateAt.END);
//        if()
//
//        // Xóa tất cả lesson trước khi thêm mới
//        holder.lessonContainer.removeAllViews(); // Sắp xếp lesson theo thứ tự tăng dần

        if(proStatus.equals("True")){
            List<Integer> sortedLessonIds = new ArrayList<>(course.getLessonIds());
            Collections.sort(sortedLessonIds);

            // Xóa các bài học cũ trước khi thêm mới
            holder.lessonContainer.removeAllViews();

            for (Integer lessonId : sortedLessonIds) {
                TextView textView = new TextView(context);
                textView.setText(String.valueOf(lessonId));
                textView.setTextSize(16);
                textView.setTypeface(null, Typeface.BOLD); // Chữ in đậm
                textView.setGravity(Gravity.CENTER);

                int size = 65;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(10, 0, 10, 0); // Điều chỉnh khoảng cách giữa các lesson
                textView.setLayoutParams(params);

                // Lấy ID bài học trước đó (nếu có)
                int previousLessonId = lessonId - 1; // Giả sử bài học có ID liên tiếp

                if (lessonId == sortedLessonIds.get(0)) {
                    // Kiểm tra nếu bài học đầu tiên đã hoàn thành hay chưa
                    resultManager.fetchResultByLesson(lessonId, new ApiCallback<Result>() {
                        @Override
                        public void onSuccess() { }

                        @Override
                        public void onSuccess(Result result) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                // Nếu có điểm → nền vàng (hoàn thành)
                                textView.setBackgroundResource(R.drawable.bg_lesson_cricle);
                                textView.setBackgroundTintList(
                                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_yellow))
                                );
                                textView.setEnabled(true);
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                // Nếu chưa có điểm, nhưng là bài học đầu tiên → mở khóa với màu trắng
                                textView.setBackgroundResource(R.drawable.bg_lesson_cricle);
                                textView.setBackgroundTintList(
                                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                                );
                                textView.setEnabled(true);
                            });
                        }
                    });
                }

                else{
                    resultManager.fetchResultByLesson(lessonId, new ApiCallback<Result>() {
                        @Override
                        public void onSuccess() { }

                        @Override
                        public void onSuccess(Result result) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                // Nếu có điểm → nền vàng (hoàn thành)
                                textView.setBackgroundResource(R.drawable.bg_lesson_cricle); // Màu vàng
                                textView.setBackgroundTintList(
                                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_yellow))
                                );
                                textView.setEnabled(true);
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            // Nếu chưa có điểm → Kiểm tra bài học trước
                            resultManager.fetchResultByLesson(previousLessonId, new ApiCallback<Result>() {
                                @Override
                                public void onSuccess() { }

                                @Override
                                public void onSuccess(Result result) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        // Nếu bài học trước có điểm → Bật bài học này (chưa hoàn thành nhưng có thể làm)
                                        textView.setBackgroundResource(R.drawable.bg_lesson_cricle); // Màu mặc định
                                        textView.setBackgroundTintList(
                                                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                                        );
                                        textView.setEnabled(true);
                                    });
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        // Nếu bài học trước chưa có điểm → Khóa bài học hiện tại (xám)
                                        textView.setBackgroundResource(R.drawable.bg_lesson_cricle); // Màu xám
                                        textView.setBackgroundTintList(
                                                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_gray))
                                        );
                                        textView.setEnabled(false);
                                    });
                                }
                            });
                        }
                    });
                }

                // Xử lý sự kiện click để fetch dữ liệu và điều hướng
                textView.setOnClickListener(v -> {
                    resultManager.getEnrollment(course.getId(), new ApiCallback<Enrollment>() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onSuccess(Enrollment enrollment) {
                            int enrollmentId = enrollment.getId();
                            lessonManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onSuccess(Lesson lesson) {
                                    Intent intent = new Intent(context, NevigateQuestion.class);
                                    intent.putExtra("courseId", course.getId());
                                    intent.putExtra("enrollmentId", enrollmentId);
                                    intent.putExtra("lessonId", lessonId);
                                    intent.putExtra("questionIds", new ArrayList<>(lesson.getQuestionIds()));
                                    context.startActivity(intent);
                                }


                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(context, "Lỗi tải dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {

                        }
                    });
                });


                holder.lessonContainer.addView(textView);
            }
        }
        else if (proStatus.equals("False")){
            holder.itemView.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, CourseInformationActivity.class);
                Log.d("CourseID:", course.getId() +"");
                intent.putExtra("courseId", course.getId());
                context.startActivity(intent);
            });
        } else if (proStatus.equals("Done")) {
            holder.itemView.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_yellow))
            );
        } else if (proStatus.equals("None")) {
            resultManager.getEnrollment(course.getId(), new ApiCallback<Enrollment>() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onSuccess(Enrollment enrollment) {
                    if(enrollment.getTotalPoints() > 0){
                        new Handler(Looper.getMainLooper()).post(() -> {
                            holder.itemView.setBackgroundTintList(
                                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_yellow))
                            );
                            holder.itemView.setOnClickListener(v -> {
                                Toast.makeText(context, context.getString(R.string.compcourse), Toast.LENGTH_SHORT).show();
                            });
                        });
                    } else if (enrollment.getProStatus().equals("false")){
                        new Handler(Looper.getMainLooper()).post(() -> {
                            holder.itemView.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                            holder.itemView.setOnClickListener(v -> {
                                Intent intent = new Intent(context, CourseInformationActivity.class);
                                intent.putExtra("courseId", course.getId());
                                context.startActivity(intent);
                            });
                        });
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            holder.itemView.setBackgroundTintList(
                                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorDefault))
                            );
                            holder.itemView.setOnClickListener(v -> {
                                Toast.makeText(context, context.getString(R.string.joincourse), Toast.LENGTH_SHORT).show();
                            });
                        });
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        holder.itemView.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                        holder.itemView.setOnClickListener(v -> {
                            Intent intent = new Intent(context, CourseInformationActivity.class);
                            intent.putExtra("courseId", course.getId());
                            context.startActivity(intent);
                        });
                    });
                }
            });
        }
    }



    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void setCourseList(List<Course> newList) {
        if (newList != null) {
            // Tạo một danh sách mới đã sắp xếp
            List<Course> newSortedList = newList.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(Course::getId))
                    .collect(Collectors.toList());

            // Lưu lại vị trí thay đổi trong courseList
            int oldSize = this.courseList != null ? this.courseList.size() : 0;
            this.courseList = newSortedList;

            // Kiểm tra xem có thay đổi danh sách không và thông báo phạm vi đã thay đổi
            if (oldSize != newSortedList.size()) {
                notifyItemRangeChanged(0, newSortedList.size());
            }
        } else {
            this.courseList = new ArrayList<>();
            notifyItemRangeChanged(0, 0);  // Nếu danh sách trống, thông báo là đã thay đổi
        }
    }



    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseTitle, tvCourseDescription;
        LinearLayout lessonContainer;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvCourseDescription = itemView.findViewById(R.id.tvCourseDescription);
            lessonContainer = itemView.findViewById(R.id.lessonContainer);
        }
    }
    public void openLesson(int courseId, int lessonId) {
        Log.d("CourseAdapter", "Opening lesson " + lessonId + " for course " + courseId);

        resultManager.getEnrollment(courseId, new ApiCallback<Enrollment>() {
            @Override
            public void onSuccess(Enrollment enrollment) {
                int enrollmentId = enrollment.getId();
                lessonManager.fetchLessonById(lessonId, new ApiCallback<Lesson>() {
                    @Override
                    public void onSuccess(Lesson lesson) {
                        Intent intent = new Intent(context, NevigateQuestion.class);
                        intent.putExtra("courseId", courseId);
                        intent.putExtra("enrollmentId", enrollmentId);
                        intent.putExtra("lessonId", lessonId);
                        intent.putExtra("questionIds", new ArrayList<>(lesson.getQuestionIds()));
                        context.startActivity(intent);
                    }

                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onFailure(String errorMessage) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, "Lỗi tải dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String errorMessage) {
                Log.e("CourseAdapter", "Error getting enrollment: " + errorMessage);
            }
        });
    }
}

