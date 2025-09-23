package com.example.test.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.EnglishLevelManager;
import com.example.test.ui.home.HomeActivity;

import java.util.List;

public class EnglishLevelAdapter extends RecyclerView.Adapter<EnglishLevelAdapter.ViewHolder> {
    private Context context;
    private List<String> levelList;
    private EnglishLevelManager englishLevelManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public EnglishLevelAdapter(Context context, List<String> levelList) {
        this.context = context;
        this.levelList = levelList;
        this.englishLevelManager = new EnglishLevelManager(context); // Truyền context vào EnglishLevelManager

        // Đảm bảo context không null trước khi gọi getSharedPreferences()
        if (context != null) {
            this.sharedPreferences = context.getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            this.editor = sharedPreferences.edit();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_english_level, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String level = levelList.get(position);
        holder.textViewLevel.setText(level);


        // Xử lý khi bấm vào TextView
        holder.textViewLevel.setOnClickListener(v -> {
            // Gọi API cập nhật English Level
            englishLevelManager.updateUserEnglishLevel(level, new ApiCallback() {
                @Override
                public void onSuccess() {
                    mainHandler.post(() -> {
                        // Chuyển sang trang Home
                        Intent intent = new Intent(context, HomeActivity.class);
                        context.startActivity(intent);
                        editor.putBoolean("hasSelectedOption", true);
                        editor.putString("lastActivity", HomeActivity.class.getName()); // Chuyển đến HomeActivity
                        editor.apply();
                    });
                }

                @Override
                public void onSuccess(Object result) {

                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("EnglishLevelAdapter", errorMessage);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return levelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLevel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLevel = itemView.findViewById(R.id.textViewLevel);
        }
    }
}
