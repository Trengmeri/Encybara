package com.example.test.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.R;
import com.example.test.model.QuestionChoice;

import java.util.List;

public class ChoiceAdapter extends RecyclerView.Adapter<ChoiceAdapter.ChoiceViewHolder> {

    private List<QuestionChoice> choices;
    private Context context;
    private List<String> userAnswers;
    private int selectedPosition = -1; // Lưu vị trí của lựa chọn đã chọn


    public ChoiceAdapter(Context context, List<QuestionChoice> choices, List<String> userAnswers) {
        this.context = context;
        this.choices = choices;
        this.userAnswers = userAnswers;
    }

    @NonNull
    @Override
    public ChoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.choice_btn, parent, false);
        return new ChoiceViewHolder(view);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull ChoiceViewHolder holder, @SuppressLint("RecyclerView") int position) {
        QuestionChoice choice = choices.get(position);
        String answer = choice.getChoiceContent();
        holder.choiceButton.setText(answer);

        // Kiểm tra nếu lựa chọn đã được chọn trước đó
        if (position == selectedPosition) {
            holder.choiceButton.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_answer_pressed)); // Màu khi được chọn

        } else {
            holder.choiceButton.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_answer));

        }

        holder.choiceButton.setOnClickListener(v -> {
            boolean isSelected = holder.choiceButton.isSelected();
            holder.choiceButton.setSelected(!isSelected);
            // Nếu lựa chọn này đã được chọn, thì bỏ chọn nó
            if (position == selectedPosition) {
                selectedPosition = -1; // Không có nút nào được chọn
                userAnswers.clear();
            } else {
                // Chỉ giữ lại một lựa chọn duy nhất
                userAnswers.clear();
                userAnswers.add(answer);
                selectedPosition = position; // Cập nhật vị trí đã chọn
            }
            notifyDataSetChanged(); // Cập nhật lại giao diện RecyclerView
        });
    }


    @Override
    public int getItemCount() {
        return choices.size();
    }

    static class ChoiceViewHolder extends RecyclerView.ViewHolder {
        AppCompatButton choiceButton;

        public ChoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            choiceButton = itemView.findViewById(R.id.btnOption);
        }
    }
}