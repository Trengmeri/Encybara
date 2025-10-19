package com.example.test.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.BaseActivity;
import com.example.test.NetworkChangeReceiver;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.AuthenticationManager;
import com.example.test.model.Answer;
import com.example.test.model.Course;
import com.example.test.model.Enrollment;
import com.example.test.model.Discussion;
import com.example.test.model.Lesson;
import com.example.test.model.MediaFile;
import com.example.test.model.Question;
import com.example.test.model.Result;

import java.util.List;

public class ForgotPassWordActivity extends BaseActivity {

    EditText edtEmail;
    Button btnContinue;
    ImageView imgBack;
    NetworkChangeReceiver networkReceiver;
    AuthenticationManager apiManager;
    boolean valid= true;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_pass_word);
        setUpView();

        networkReceiver = new NetworkChangeReceiver();
        apiManager = new AuthenticationManager(this);

        // Khởi tạo Dialog loading
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false); // Không cho phép đóng khi chạm ngoài màn hình

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                valid = true;
                if (email.isEmpty()) {
                    showCustomDialog("Please enter email.");
                    valid = false;
                    //Toast.makeText(ForgotPassWordActivity.this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidEmail(email)) {
                    showCustomDialog("Email format incorrect");
                    valid = false;
                    //Toast.makeText(ForgotPassWordActivity.this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!valid) return; // Nếu không hợp lệ, dừng lại

                // Kiểm tra kết nối Internet trước khi gọi API
                if (!apiManager.isInternetAvailable(ForgotPassWordActivity.this)) {
                    showCustomDialog("No Internet Connection!");
                    return;
                }
                btnContinue.setEnabled(false);
                btnContinue.setAlpha(0.5f);
                if (valid) {
                    showLoading();
                    if (!apiManager.isInternetAvailable(ForgotPassWordActivity.this)) {
                        Toast.makeText(ForgotPassWordActivity.this, "Không có kết nối Internet!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    apiManager.sendForgotPasswordRequest(email, new ApiCallback<String>() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onSuccess(String otpID) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideLoading();
                                    //showCustomDialog("OTP was sent to your email.Please check!");
                                    //Toast.makeText(ForgotPassWordActivity.this, "Vui lòng kiểm tra email của bạn.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            saveOtpId(otpID); // Lưu otpID vào SharedPreferences
                            Intent intent = new Intent(ForgotPassWordActivity.this, ConfirmCode2Activity.class);
                            intent.putExtra("source", "forgot");
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideLoading();
                                    Toast.makeText(ForgotPassWordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    btnContinue.setEnabled(true);
                                    btnContinue.setAlpha(1f);
                                }
                            });
                        }
                    });
                }
            }
        });

        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPassWordActivity.this, SignInActivity.class);
            startActivity(intent);
        });
    }
    private void saveOtpId(String otpID) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("otpID", otpID);
        editor.apply();
    }
    private void setUpView() {
        edtEmail = (EditText) findViewById(R.id.edt_email);
        btnContinue = findViewById(R.id.btn_continue);
        imgBack = (ImageView) findViewById(R.id.imgBack);
    }
    private void showCustomDialog(String message) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_alert);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Ánh xạ View
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        ImageView imgIcon = dialog.findViewById(R.id.imgIcon);

        tvMessage.setText(message);
        //imgIcon.setImageResource(iconResId);

        // Thiết lập vị trí hiển thị trên cùng màn hình
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.TOP);
            window.setWindowAnimations(R.style.DialogAnimation); // Gán animation
        }

        dialog.show();

        // Ẩn Dialog sau 2 giây
        new Handler().postDelayed(() -> {
            dialog.dismiss();
        }, 2000);
    }
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
}
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Kiểm tra nếu chưa chọn trong SelectActivity thì mới lưu
//        boolean hasSelectedOption = sharedPreferences.getBoolean("hasSelectedOption", false);
//        if (!hasSelectedOption) {
//            editor.putString("lastActivity", this.getClass().getName());
//            editor.apply();
//        }
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
