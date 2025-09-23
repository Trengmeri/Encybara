package com.example.test;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PopupHelper {

    public static void showResultPopup(Activity activity, String questType, String userAnswers, String correctAnswers, Double score, String improvements, String evaluation, Runnable onNextQuestion) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View popupView = LayoutInflater.from(activity).inflate(R.layout.popup_result, null);
        dialog.setContentView(popupView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.dimAmount = 0.5f;
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setAttributes(layoutParams);
        }

        TextView tvMessage = popupView.findViewById(R.id.tvResultMessage);
        TextView tvDetail = popupView.findViewById(R.id.tvResultDetail);
        Button btnNext = popupView.findViewById(R.id.btnNextQuestion);
        Button btnview = popupView.findViewById(R.id.btnview);


        if ("MULTIPLE".equals(questType) || "CHOICE".equals(questType) || "TEXT".equals(questType)) {

            if (TextUtils.equals(userAnswers.trim().toLowerCase(), correctAnswers.trim().toLowerCase())) {
                String correctText = activity.getString(R.string.correct);
                String ansText = activity.getString(R.string.ANS);
                SpannableString spannable = new SpannableString(correctText + "  \n" + ansText);

                Drawable tick = ContextCompat.getDrawable(activity, R.drawable.ic_tick);
                if (tick != null) {
                    int size = (int) (tvMessage.getLineHeight() * 1.5);
                    tick.setBounds(0, 0, size, size);
                    ImageSpan imageSpan = new ImageSpan(tick, ImageSpan.ALIGN_BOTTOM);
                    int start = correctText.length()+1;
                    spannable.setSpan(imageSpan, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                tvMessage.setText(spannable);
                tvMessage.setTextColor(activity.getResources().getColor(android.R.color.holo_green_dark));
                tvDetail.setText(correctAnswers);  // hoặc String.join(...) nếu là List
                popupView.setBackgroundResource(R.drawable.popup_background_correct);
                btnNext.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_green_dark));

            } else {
                String oopsText = activity.getString(R.string.oops);
                String coAnsText = activity.getString(R.string.COANS);
                SpannableString spannable = new SpannableString(oopsText + "  \n" + coAnsText);

                Drawable cross = ContextCompat.getDrawable(activity, R.drawable.ic_cross);  // icon dấu X
                if (cross != null) {
                    int size = (int) (tvMessage.getLineHeight() * 1.5);
                    cross.setBounds(0, 0, size, size);
                    ImageSpan imageSpan = new ImageSpan(cross, ImageSpan.ALIGN_BOTTOM);
                    int start = oopsText.length()+1;
                    spannable.setSpan(imageSpan, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                tvMessage.setText(spannable);
                tvMessage.setTextColor(activity.getResources().getColor(android.R.color.holo_red_dark));
                tvDetail.setText(correctAnswers);  // hoặc String.join(...) nếu là List
                popupView.setBackgroundResource(R.drawable.popup_background_incorrect);
                btnNext.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_red_dark));
            }

        } else {
            try {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) btnNext.getLayoutParams();
                params.setMargins(16, 0, 0, 0);  // 16 pixels hoặc dùng dp chuyển sang pixel
                btnNext.setLayoutParams(params);

                btnview.setVisibility(View.VISIBLE);
                tvMessage.setText(String.format("%s %.1f",activity.getString(R.string.point), score));
                tvDetail.setText(evaluation);
                btnview.setOnClickListener(view -> {
                    tvMessage.setText(activity.getString(R.string.improvements));
                    tvDetail.setText(improvements);
                    btnview.setVisibility(View.GONE);
                });

                // Đặt màu nền theo điểm
                if (score < 3) {
                    popupView.setBackgroundResource(R.drawable.popup_background_incorrect);
                    tvMessage.setTextColor(activity.getResources().getColor(android.R.color.holo_red_dark));
                    btnNext.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_red_dark));
                    btnview.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_red_light));
                } else if (score < 6) {
                    popupView.setBackgroundResource(R.drawable.popup_yellow);
                    tvMessage.setTextColor(activity.getResources().getColor(android.R.color.holo_orange_dark));
                    btnNext.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_orange_dark));
                    btnview.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_orange_light));
                } else {
                    popupView.setBackgroundResource(R.drawable.popup_background_correct);
                    tvMessage.setTextColor(activity.getResources().getColor(android.R.color.holo_green_dark));
                    btnNext.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_green_dark));
                    btnview.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_green_light));
                }
            } catch (Exception e) {
                tvMessage.setText("Error displaying result!");
                tvDetail.setText(e.getMessage());
            }
        }
        btnNext.setOnClickListener(v -> {
            dialog.dismiss();
            onNextQuestion.run();
        });

        dialog.show();
    }
}

