package com.example.test.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.test.BaseActivity;
import com.example.test.NetworkChangeReceiver;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.AuthenticationManager;
import com.example.test.model.Answer;
import com.example.test.model.Course;
import com.example.test.model.Enrollment;
import com.example.test.model.Lesson;
import com.example.test.model.MediaFile;
import com.example.test.model.Question;
import com.example.test.model.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends BaseActivity {

    EditText edtName, edtEmail, edtMKhau1;
    TextView txtEmailerror, txtPasserror,btnIn;
    CheckBox cbCheck;
    Button btnUp;
    NetworkChangeReceiver networkReceiver;
    AuthenticationManager apiManager;
    boolean isvalid =true;
    //private boolean isPasswordVisible = false;
    private Dialog loadingDialog;
    private boolean isPasswordVisible = false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        AnhXa();
        setupPasswordField();
        // Khởi tạo Dialog loading
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false); // Không cho phép đóng khi chạm ngoài màn hình

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        edtName.addTextChangedListener(textWatcher);
        edtEmail.addTextChangedListener(textWatcher);
        edtMKhau1.addTextChangedListener(textWatcher);

        cbCheck.setOnCheckedChangeListener((buttonView, isChecked) -> checkInputFields());


        // Tạo đối tượng NetworkChangeReceiver
        networkReceiver = new NetworkChangeReceiver();
        apiManager = new AuthenticationManager(this);

        btnUp.setOnClickListener(view -> {
            showError();
            btnUp.setEnabled(false);
            btnUp.setAlpha(0.5f);

            String hoten = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String pass = edtMKhau1.getText().toString().trim();
            if(isvalid) {
                showLoading();
                if (!apiManager.isInternetAvailable(SignUpActivity.this)) {
                    Toast.makeText(SignUpActivity.this, "Vui lòng kiểm tra kết nối Internet của bạn.", Toast.LENGTH_LONG).show();
                } else {
                    apiManager.sendSignUpRequest(this, hoten, email, pass, new ApiCallback<String>() {
                        @Override
                        public void onSuccess() {
                        }

                    @Override
                    public void onFailure(String errorMessage) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoading();
                                showCustomDialog("Sign up failed. Email was used.");
                                btnUp.setEnabled(true); // Bật lại nút nếu thất bại
                                btnUp.setAlpha(1.0f);
                            }
                        });
                    }

                    @Override
                    public void onSuccess(String otpID) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoading();
                            }
                        });
                        saveOtpId(otpID); // Lưu otpID vào SharedPreferences
                        Log.d("ConfirmCode", "otpID được lưu: " + otpID);
                            Intent intent = new Intent(SignUpActivity.this, ConfirmCode2Activity.class);
                            intent.putExtra("source", "register");
                            startActivity(intent);
                        }
                    });
                }
            }
        });

        btnIn.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Ẩn lỗi khi người dùng nhấn vào EditText để sửa
        edtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    txtEmailerror.setVisibility(View.GONE);
                }
            }
        });
        edtMKhau1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    txtPasserror.setVisibility(View.GONE);
                }
            }
        });
    }
    private void saveOtpId(String otpID) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("otpID", otpID);
        editor.apply();
        Log.d("ConfirmCode", "Otp ID đã được lưu: " + otpID);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordField() {
        // Mặc định ẩn mật khẩu
        edtMKhau1.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edtMKhau1.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_pass, 0, R.drawable.icon_visibility_off, 0);

        edtMKhau1.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtMKhau1.getRight() - edtMKhau1.getCompoundDrawables()[2].getBounds().width())) {
                    // Đổi trạng thái hiển thị mật khẩu
                    if (isPasswordVisible) {
                        edtMKhau1.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Ẩn mật khẩu
                        edtMKhau1.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_pass, 0, R.drawable.icon_visibility_off, 0);
                    } else {
                        edtMKhau1.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); // Hiện mật khẩu
                        edtMKhau1.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_pass, 0, R.drawable.icon_visibility, 0);
                    }
                    isPasswordVisible = !isPasswordVisible;

                    // Giữ con trỏ ở cuối văn bản
                    edtMKhau1.setSelection(edtMKhau1.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Đăng ký BroadcastReceiver
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hủy đăng ký BroadcastReceiver
        unregisterReceiver(networkReceiver);
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
    private void showError() {
        isvalid= true;
        String email = edtEmail.getText().toString().trim();
        String pass = edtMKhau1.getText().toString().trim();

        txtEmailerror.setVisibility(View.GONE); // Ẩn lỗi ban đầu
        txtPasserror.setVisibility(View.GONE);
        // Kiểm tra Email
        if (!isValidEmail(email)) {
            txtEmailerror.setText("Email format is incorrect!");
            txtEmailerror.setVisibility(View.VISIBLE);
            isvalid = false;
        }
        // Kiểm tra Password
        if (!isValidPassword(pass)) {
            txtPasserror.setText("Password format is incorrect!");
            txtPasserror.setVisibility(View.VISIBLE);
            isvalid = false;
        }
    }
    private void checkInputFields() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtMKhau1.getText().toString().trim();
        boolean isChecked = cbCheck.isChecked();

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && isChecked) {
            btnUp.setEnabled(true);
            btnUp.setAlpha(1.0f); // Làm sáng nút
        } else {
            btnUp.setEnabled(false);
            btnUp.setAlpha(0.5f); // Làm mờ nút
        }
    }
    private void AnhXa() {
        edtEmail = findViewById(R.id.edtEmail);
        edtName = findViewById(R.id.edtTen);
        edtMKhau1= findViewById(R.id.edtMKhau);
        cbCheck = findViewById(R.id.cbCheck);
        btnUp = findViewById(R.id.btnUp);
        btnIn = findViewById(R.id.btnIn);
        txtEmailerror= findViewById(R.id.txtEmailError);
        txtPasserror= findViewById(R.id.txtPassError);
        // Vô hiệu hóa button ban đầu
        btnUp.setEnabled(false);
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

//    private boolean isValidPhoneNumber(String phoneNumber) {
//        String phonePattern = "^[0-9]{10,11}$";
//        Pattern pattern = Pattern.compile(phonePattern);
//        Matcher matcher = pattern.matcher(phoneNumber);
//        return matcher.matches();
//    }

    private boolean isValidPassword(String password) {
//        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!_&*]).{8,}$";
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

}
