package com.example.test.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewPassActivity extends BaseActivity {

    EditText edtPass, edtRePass;
    TextView tPasserror, tNewpasserror;
    Button btnNext;
    ImageView icback;
    NetworkChangeReceiver networkReceiver;
    AuthenticationManager apiManager;
    private boolean isPasswordVisible = false;
    boolean isValid = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_pass);

        AnhXa();
        setupPasswordField();
        networkReceiver = new NetworkChangeReceiver();
        apiManager = new AuthenticationManager(this);

        btnNext.setEnabled(false);
        btnNext.setAlpha(0.5f);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputFields();
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        edtPass.addTextChangedListener(textWatcher);
        edtRePass.addTextChangedListener(textWatcher);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showerror();
                btnNext.setEnabled(false);
                btnNext.setAlpha(0.5f);
                if (isValid) {
                    if (!apiManager.isInternetAvailable(NewPassActivity.this)) {
                        Toast.makeText(NewPassActivity.this, "Vui lòng kiểm tra kết nối Internet của bạn.", Toast.LENGTH_LONG).show();
                    } else {
                        String pass = edtPass.getText().toString().trim();
                        String repass = edtRePass.getText().toString().trim();
                        String token = getTokenFromSharedPreferences();
//                if (!pass.equals(repass)) {
//                    Toast.makeText(NewPassActivity.this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
//                }
                        Log.d("Token", "Token lay duoc :" + token);
                        apiManager.updatePassword(pass, repass, token, new ApiCallback<String>() {
                            @Override
                            public void onSuccess(String token) {
                            }

                            @Override
                            public void onSuccess() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showCustomDialog("Update password successfully.");
                                        Intent intent = new Intent(NewPassActivity.this, LoadPassActivity.class);
                                        startActivity(intent);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showCustomDialog("Lỗi: " + errorMessage);
                                        //Toast.makeText(NewPassActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        });
                    }
                }
            }
        });

        icback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewPassActivity.this, ForgotPassWordActivity.class);
                startActivity(intent);
            }
        });
    }

    public String getTokenFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("TOKEN_KEY", null);  // Lấy token từ sharedPreferences, nếu không có thì trả về null
    }

    //    Ẩn hiện mk
    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordField() {
        // Mặc định ẩn mật khẩu
        edtPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edtPass.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_pass, 0, R.drawable.icon_visibility_off, 0);
        // Mặc định ẩn mật khẩu
        edtRePass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edtRePass.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_pass, 0, R.drawable.icon_visibility_off, 0);

        edtPass.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtPass.getRight() - edtPass.getCompoundDrawables()[2].getBounds().width())) {
                    // Đổi trạng thái hiển thị mật khẩu
                    if (isPasswordVisible) {
                        edtPass.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Ẩn mật khẩu
                        edtPass.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_pass, 0, R.drawable.icon_visibility_off, 0);
                    } else {
                        edtPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); // Hiện mật khẩu
                        edtPass.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_pass, 0, R.drawable.icon_visibility, 0);
                    }
                    isPasswordVisible = !isPasswordVisible;

                    // Giữ con trỏ ở cuối văn bản
                    edtPass.setSelection(edtPass.getText().length());
                    return true;
                }
            }
            return false;
        });
        edtRePass.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtRePass.getRight() - edtRePass.getCompoundDrawables()[2].getBounds().width())) {
                    // Đổi trạng thái hiển thị mật khẩu
                    if (isPasswordVisible) {
                        edtRePass.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Ẩn mật khẩu
                        edtRePass.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_pass, 0, R.drawable.icon_visibility_off, 0);
                    } else {
                        edtRePass.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); // Hiện mật khẩu
                        edtRePass.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_pass, 0, R.drawable.icon_visibility, 0);
                    }
                    isPasswordVisible = !isPasswordVisible;

                    // Giữ con trỏ ở cuối văn bản
                    edtRePass.setSelection(edtRePass.getText().length());
                    return true;
                }
            }
            return false;
        });


    }
    private void AnhXa() {
        edtPass = (EditText) findViewById(R.id.edtPass);
        edtRePass = (EditText) findViewById(R.id.edtRePass);
        btnNext = findViewById(R.id.btnNext);
        icback = findViewById(R.id.icback);
        tPasserror = findViewById(R.id.tPassError); // Thay thế bằng ID của TextView
        tNewpasserror = findViewById(R.id.tNewPassError);
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
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$";
//        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!_&*]).{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
    private void checkInputFields() {
        String newpass = edtPass.getText().toString().trim();
        String renewpass = edtRePass.getText().toString().trim();

        if (!newpass.isEmpty() && !renewpass.isEmpty()) {
            btnNext.setEnabled(true);
            btnNext.setAlpha(1.0f);
        } else {
            btnNext.setEnabled(false);
            btnNext.setAlpha(0.5f);
        }
    }
    private void showerror() {
        isValid= true;
        String newpass = edtPass.getText().toString().trim();
        String renewpass = edtRePass.getText().toString().trim();


        tPasserror.setVisibility(View.GONE); // Ẩn lỗi ban đầu
        tNewpasserror.setVisibility(View.GONE);
        if (!isValidPassword(newpass)) {
            tPasserror.setText("Password format is incorrect.");
            tPasserror.setVisibility(View.VISIBLE);
            isValid = false;
        }
        // Kiểm tra Password
        if (!newpass.equals(renewpass)) {
            tNewpasserror.setText("Re-Password doesn't match.");
            tNewpasserror.setVisibility(View.VISIBLE);
            isValid = false;
        }
    }
}