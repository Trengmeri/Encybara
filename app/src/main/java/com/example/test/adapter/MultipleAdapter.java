package com.example.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.R;
import com.example.test.model.QuestionChoice;

import java.util.List;

public class MultipleAdapter extends RecyclerView.Adapter<MultipleAdapter.ChoiceViewHolder> {

    private List<QuestionChoice> choices;
    private Context context;
    private List<String> userAnswers;

    public MultipleAdapter(Context context, List<QuestionChoice> choices, List<String> userAnswers) {
        this.context = context;
        this.choices = choices;
        this.userAnswers = userAnswers;
    }

    @NonNull
    @Override
    public ChoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_choice, parent, false);
        return new ChoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoiceViewHolder holder, int position) {
        QuestionChoice choice = choices.get(position);
        String answer = choice.getChoiceContent();
        holder.choiceButton.setText(answer);

        // Kiểm tra nếu lựa chọn đã được chọn trước đó
        if (userAnswers.contains(answer)) {
            holder.choiceButton.setBackgroundResource(R.color.colorPressed);
        } else {
            holder.choiceButton.setBackgroundResource(R.color.colorDefault);
        }

        holder.choiceButton.setOnClickListener(v -> {
            if (userAnswers.contains(answer)) {
                userAnswers.remove(answer);
                holder.choiceButton.setBackgroundResource(R.color.colorDefault);
            } else {
                userAnswers.add(answer);
                holder.choiceButton.setBackgroundResource(R.color.colorPressed);
            }
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
            choiceButton = itemView.findViewById(R.id.choiceButton);
        }
    }
}