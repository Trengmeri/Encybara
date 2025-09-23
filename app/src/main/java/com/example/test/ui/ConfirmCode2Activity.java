package com.example.test.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.test.AsteriskPasswordTransformationMethod;
import com.example.test.NetworkChangeReceiver;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.AuthenticationManager;
import com.example.test.model.Answer;
import com.example.test.model.Course;
import com.example.test.model.Discussion;
import com.example.test.model.Enrollment;
import com.example.test.model.Lesson;
import com.example.test.model.MediaFile;
import com.example.test.model.Question;
import com.example.test.model.Result;

import java.util.Objects;

public class ConfirmCode2Activity extends AppCompatActivity {

    private EditText[] codeInputs; // Mảng chứa các ô nhập mã
    private int currentInputIndex = 0; // Vị trí hiện tại của con trỏ nhập liệu
    private ImageView icback;
    private Button btnResendIn, btnResend;
    private TextView tvCountdown; // TextView hiển thị thời gian đếm ngược
    private static final long COUNTDOWN_TIME = 60000; // 60 giây
    private CountDownTimer countDownTimer;
    NetworkChangeReceiver networkReceiver;
    AuthenticationManager apiManager;
    private String typeScreen;
    private boolean isRequesting = false;
    private Dialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_code2);

        // Áp dụng padding cho giao diện
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo Dialog loading
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false); // Không cho phép đóng khi chạm ngoài màn hình

        // Ánh xạ các ô nhập mã
        codeInputs = new EditText[]{
                findViewById(R.id.editText1),
                findViewById(R.id.editText2),
                findViewById(R.id.editText3),
                findViewById(R.id.editText4),
                findViewById(R.id.editText5),
                findViewById(R.id.editText6)
        };
        typeScreen = getIntent().getStringExtra("source");
        icback = findViewById(R.id.iconback);
        btnResendIn = findViewById(R.id.btnResendIn);
        btnResend = findViewById(R.id.btnReSend);
        tvCountdown = findViewById(R.id.tv_countdown); // Ánh xạ TextView đếm ngược

        // Thiết lập sự kiện cho các nút trên bàn phím
        setupKeyboardListeners();

        // Nút quay lại
        icback.setOnClickListener(view -> {
            Intent intent = new Intent(ConfirmCode2Activity.this, SignUpActivity.class);
            startActivity(intent);
        });
        networkReceiver = new NetworkChangeReceiver();
        apiManager = new AuthenticationManager(this);
        // Bắt đầu đếm ngược thời gian
        startCountdown();

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnResend.setEnabled(false); // Ngăn người dùng nhấn liên tục
                btnResend.setAlpha(0.3f);
                String otpID = getOtpIdFromPreferences(); // Lấy OTP ID đã lưu
                apiManager.resendConfirmCodeRequest(otpID, new ApiCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showCustomDialog("Mã OTP đã được gửi lại. Vui lòng kiểm tra email.");
                                setupKeyboardListeners();
                                resetCountdown();// Gọi phương thức reset lại bộ đếm
                                isRequesting = false;

                                //Toast.makeText(ConfirmCode2Activity.this, "Mã OTP đã được gửi lại. Vui lòng kiểm tra email.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(Object result) {

                    }


                    @Override
                    public void onFailure(String errorMessage) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showCustomDialog("Lỗi: " + errorMessage);
                                //Toast.makeText(ConfirmCode2Activity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                                btnResend.setEnabled(true);
                                btnResend.setAlpha(1.0f); // Cho phép bấm lại nếu lỗi
                            }
                        });
                    }
                });
            }
        });
    }

    private void resetCountdown() {
        // Hủy bộ đếm hiện tại (nếu có)
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // Bắt đầu lại bộ đếm từ đầu
        startCountdown();
    }


    private void setupKeyboardListeners() {
        // Danh sách các nút số từ 0 đến 9
        int[] buttonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6, R.id.button7,
                R.id.button8, R.id.button9
        };

        // Gắn sự kiện cho từng nút
        for (int i = 0; i < buttonIds.length; i++) {
            int finalI = i;
            findViewById(buttonIds[i]).setOnClickListener(v -> {
                if (currentInputIndex < codeInputs.length) {
                    codeInputs[currentInputIndex].setTransformationMethod(null);
                    // Hiển thị số vừa nhập
                    codeInputs[currentInputIndex].setText(String.valueOf(finalI));
                    // Lưu giá trị để kiểm tra sau này
                    String enteredValue = String.valueOf(finalI);
                    int finalCurrentIndex = currentInputIndex;
                    //codeInputs[currentInputIndex].setText(String.valueOf(finalI)); // Hiển thị số vào ô nhập
                    // Nếu chưa đến ô cuối cùng thì sau 500ms ẩn đi
                    if (currentInputIndex < codeInputs.length - 1) {
                        new Handler().postDelayed(() -> {
                            if (codeInputs[finalCurrentIndex].getText().toString().equals(enteredValue)) {
                                codeInputs[finalCurrentIndex].setTransformationMethod(new AsteriskPasswordTransformationMethod());
                                codeInputs[finalCurrentIndex].setSelection(codeInputs[finalCurrentIndex].getText().length()); // Cập nhật lại con trỏ
                            }
                        }, 500);
                        // **Đặt con trỏ vào ô tiếp theo**
                        codeInputs[currentInputIndex + 1].requestFocus();
                    }
                    currentInputIndex++;
                }
            });
        }

        // Nút xóa
        findViewById(R.id.btnDel).setOnClickListener(v -> {
            if (currentInputIndex > 0) {
                currentInputIndex--; // Quay lại ô trước
                codeInputs[currentInputIndex].setText(""); // Xóa nội dung
                codeInputs[currentInputIndex].setTransformationMethod(null);
                codeInputs[currentInputIndex].requestFocus();
            }
        });

        // Lắng nghe sự kiện nhập mã vào ô cuối cùng

        codeInputs[5].addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() == 1) {
                    if (isRequesting) return; // Nếu đang request thì không gọi lại
                    isRequesting = true; // Đánh dấu là đang gọi API
                    new Handler().postDelayed(() -> {
                        showLoading();
                        for (EditText input : codeInputs) {
                            input.setTransformationMethod(new AsteriskPasswordTransformationMethod());
                        }
                    String otpID = getOtpIdFromPreferences();// Khi người dùng nhập vào ô cuối cùng
                    String code = getCode(); // Lấy mã đã nhập
                    // Gọi API xác nhận mã OTP
                        if (Objects.equals(typeScreen, "register")) {
                            // di luong DK
                            apiManager.sendConfirmCodeRequest(otpID,code, new ApiCallback() {
                                @Override
                                public void onSuccess() {
                                    // Chuyển đến Activity tiếp theo nếu mã đúng
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideLoading();
                                            Log.d("OTP", "OTP verification success");
                                            clearOtpId();
                                            Intent intent = new Intent(ConfirmCode2Activity.this, SetUpAccountActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });

                                }

                                @Override
                                public void onSuccess(Object object) {}


                                @Override
                                public void onFailure(String errorMessage) {
                                    // Hiển thị thông báo lỗi nếu mã sai
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideLoading();
                                            showCustomDialog("Lỗi: " + errorMessage);
                                            isRequesting= false;
                                            //Toast.makeText(ConfirmCode2Activity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                        else if (Objects.equals(typeScreen, "forgot")){
                            // di luong quen mk
                            apiManager.sendConfirmForgotPasswordRequest(otpID,code, new ApiCallback<String>() {
                                @Override
                                public void onSuccess(String token) {
                                    // Chuyển đến Activity tiếp theo nếu mã đúng
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideLoading();
                                            Log.d("OTP", "OTP verification success for forgot password");
                                            clearOtpId();
                                            Intent intent = new Intent(ConfirmCode2Activity.this, NewPassActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }

                                @Override
                                public void onSuccess() {
                                }


                                @Override
                                public void onFailure(String errorMessage) {
                                    // Hiển thị thông báo lỗi nếu mã sai
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideLoading();
                                            showCustomDialog("Lỗi: " + errorMessage);
                                            isRequesting= false;
                                            //Toast.makeText(ConfirmCode2Activity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    }, 1000);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
            });
    }
    // Lấy otpID từ SharedPreferences
    private String getOtpIdFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("otpID", null); // Trả về giá trị otpID hoặc null nếu không tồn tại
    }

    // Xóa otpID khỏi SharedPreferences
    private void clearOtpId() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("otpID");
        editor.apply();
    }

    // Phương thức để lấy mã đã nhập
    private String getCode() {
        StringBuilder code = new StringBuilder();
        for (EditText input : codeInputs) {
            code.append(input.getText().toString());
        }
        return code.toString();
    }

    private void startCountdown() {
        btnResendIn.setVisibility(View.VISIBLE);
        btnResend.setVisibility(View.GONE);
        tvCountdown.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) { // Cập nhật mỗi giây
            @Override
            public void onTick(long millisUntilFinished) {
                // Cập nhật thời gian còn lại
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                tvCountdown.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                // Khi đếm ngược kết thúc
                tvCountdown.setText("00:00");
                btnResend.setVisibility(View.VISIBLE);
                btnResendIn.setVisibility(View.GONE);
                tvCountdown.setVisibility(View.GONE);
                onCountdownFinished();
            }
        }.start();
    }

    private void onCountdownFinished() {
        // Hành động khi đếm ngược kết thúc
        showCustomDialog("Vui lòng nhấn gửi lại mã!");
        //Toast.makeText(ConfirmCode2Activity.this, "Vui lòng nhấn gửi lại mã!", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo hoặc xử lý logic khác
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy bộ đếm khi Activity bị hủy
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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
