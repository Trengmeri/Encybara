package com.example.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.R;
import com.example.test.model.Lesson;

import java.util.ArrayList;
import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {
    private Context context;
    private List<Lesson> lessons;

    public LessonAdapter(Context context) {
        this.context = context;
        this.lessons = new ArrayList<>();
    }

    public void setLessons(List<Lesson> newLessons) {
        this.lessons.clear();
        this.lessons.addAll(newLessons);
        notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    @Override
    public LessonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lesson_course, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.lessonTitle.setText(lesson.getName());
        holder.lessonNumber.setText(String.valueOf((position +1)));
        // Bạn có thể thêm các trường khác nếu cần
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView lessonTitle, lessonNumber;

        public LessonViewHolder(View itemView) {
            super(itemView);
            lessonTitle = itemView.findViewById(R.id.lessonTitle);
            lessonNumber = itemView.findViewById(R.id.lessonNumber);

        }
    }
}
