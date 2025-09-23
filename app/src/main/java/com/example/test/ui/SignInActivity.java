package com.example.test.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.NetworkChangeReceiver;
import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.api.ApiCallback;
import com.example.test.api.AuthenticationManager;
import com.example.test.api.FieldManager;
import com.example.test.model.Answer;
import com.example.test.model.Course;
import com.example.test.model.Enrollment;
import com.example.test.model.Lesson;
import com.example.test.model.MediaFile;
import com.example.test.model.Question;
import com.example.test.model.Result;
import com.example.test.ui.home.HomeActivity;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    EditText edtEmail, edtMKhau;
    TextView tvEmailerror, tvPasserror,btnUp;
    CheckBox cbRemember;
    Button btnIn, btnForgot;
    NetworkChangeReceiver networkReceiver;
    AuthenticationManager apiManager;
    FieldManager fieldManager;
    boolean isValid = true;
    private boolean isPasswordVisible = false;
    private long lastClickTime = 0; // Biến để chặn multi-click
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        //SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isRemembered = sharedPreferences.getBoolean("rememberMe", false);
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPassword = sharedPreferences.getString("password", "");
        boolean hasSelectedOption = sharedPreferences.getBoolean("hasSelectedOption", false);
        String lastActivity = sharedPreferences.getString("lastActivity", "");

// 🛑 In log để kiểm tra dữ liệu trong SharedPreferences
        Log.d("SignInActivity", "Remember Me: " + isRemembered);
        Log.d("SignInActivity", "Saved Email: " + savedEmail);
        Log.d("SignInActivity", "Saved Password: " + savedPassword);
        Log.d("DEBUG", "Last activity from SharedPreferences: " + lastActivity);

        setContentView(R.layout.activity_sign_in);
        AnhXa();
        setupPasswordField();


        editor = sharedPreferences.edit();
        // Hiển thị thông tin đăng nhập nếu Remember Me được chọn trước đó
        if (sharedPreferences.getBoolean("rememberMe", false)) {
            edtEmail.setText(sharedPreferences.getString("email", ""));
            edtMKhau.setText(sharedPreferences.getString("password", ""));
            cbRemember.setChecked(true);
            checkInputFields();
        }

        // Ban đầu vô hiệu hóa nút
//        btnIn.setEnabled(false);
//        btnIn.setAlpha(0.5f);

        // Lắng nghe thay đổi trên EditText
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
        edtEmail.addTextChangedListener(textWatcher);
        edtMKhau.addTextChangedListener(textWatcher);


        // Tạo đối tượng NetworkChangeReceiver
        networkReceiver = new NetworkChangeReceiver();
        apiManager = new AuthenticationManager(this);
        fieldManager= new FieldManager(this);

        btnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showerror();
                btnIn.setEnabled(false);
                btnIn.setAlpha(0.5f);

                String email = edtEmail.getText().toString().trim();
                String pass = edtMKhau.getText().toString().trim();

                if (isValid) {
                    if (!apiManager.isInternetAvailable(SignInActivity.this)) {
                        Toast.makeText(SignInActivity.this, "Không có kết nối Internet!", Toast.LENGTH_SHORT).show();
                    } else {
                        apiManager.sendLoginRequest(email, pass, new ApiCallback() {
                            @Override
                            public void onSuccess() {
                                // Lấy userId từ SharedPreferencesManager
                                String userId = SharedPreferencesManager.getInstance(SignInActivity.this).getID();
                                fieldManager.fetchUserById(userId, new ApiCallback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onSuccess(Object result) {
                                        try {
                                            JSONObject userData = (JSONObject) result;
                                            String speciField = userData.optString("speciField", "");
                                            boolean hasSelectedField = !speciField.isEmpty() && !speciField.equals("null");

                                            editor.putBoolean("hasSelectedOption", hasSelectedField);
                                            if (cbRemember.isChecked()) {
                                                editor.putBoolean("rememberMe", true);
                                                editor.putString("email", email);
                                                editor.putString("password", pass);
                                            } else {
                                                editor.putBoolean("rememberMe", false);
                                                editor.remove("email");
                                                editor.remove("password");
                                                editor.remove("lastActivity");
                                            }
                                            editor.apply();

                                            Intent intent = hasSelectedField
                                                    ? new Intent(SignInActivity.this, HomeActivity.class)
                                                    : new Intent(SignInActivity.this, ChooseFieldsActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } catch (Exception e) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(SignInActivity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                                                btnIn.setEnabled(true);
                                                btnIn.setAlpha(1.0f);
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            btnIn.setEnabled(true);
                                            btnIn.setAlpha(1.0f);
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onSuccess(Object result) {

                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                runOnUiThread(() -> {
                                    Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    btnIn.setEnabled(true);
                                    btnIn.setAlpha(1.0f);
                                });
                            }
                        });
                    }
                }
            }
        });

        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("SignInActivity","onclicked");
                Intent intent = new Intent(SignInActivity.this, ForgotPassWordActivity.class);
                startActivity(intent);
            }
        });

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Ẩn lỗi khi người dùng nhấn vào EditText để sửa
        edtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tvEmailerror.setVisibility(View.GONE);
                }
            }
        });
        edtMKhau.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tvPasserror.setVisibility(View.GONE);
                }
            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordField() {
        // Mặc định ẩn mật khẩu
        edtMKhau.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edtMKhau.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_pass, 0, R.drawable.icon_visibility_off, 0);

        edtMKhau.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtMKhau.getRight() - edtMKhau.getCompoundDrawables()[2].getBounds().width())) {
                    // Đổi trạng thái hiển thị mật khẩu
                    if (isPasswordVisible) {
                        edtMKhau.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Ẩn mật khẩu
                        edtMKhau.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_pass, 0, R.drawable.icon_visibility_off, 0);
                    } else {
                        edtMKhau.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); // Hiện mật khẩu
                        edtMKhau.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_pass, 0, R.drawable.icon_visibility, 0);
                    }
                    isPasswordVisible = !isPasswordVisible;

                    // Giữ con trỏ ở cuối văn bản
                    edtMKhau.setSelection(edtMKhau.getText().length());
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

    private void showerror() {
        isValid= true;
        String email = edtEmail.getText().toString().trim();
        String pass = edtMKhau.getText().toString().trim();

        tvEmailerror.setVisibility(View.GONE); // Ẩn lỗi ban đầu
        tvPasserror.setVisibility(View.GONE);
        // Kiểm tra Email
        if (!isValidEmail(email)) {
            tvEmailerror.setText("Email is incorrect!");
            tvEmailerror.setVisibility(View.VISIBLE);
            isValid = false;
        }
        // Kiểm tra Password
        if (!isValidPassword(pass)) {
            tvPasserror.setText("Password is incorrect!");
            tvPasserror.setVisibility(View.VISIBLE);
            isValid = false;
        }
    }
    private void checkInputFields() {
        String email = edtEmail.getText().toString().trim();
        String password = edtMKhau.getText().toString().trim();

        if (!email.isEmpty() && !password.isEmpty()) {
            btnIn.setEnabled(true);
            btnIn.setAlpha(1.0f);
        } else {
            btnIn.setEnabled(false);
            btnIn.setAlpha(0.5f);
        }
    }
    private void AnhXa() {
        edtEmail = findViewById(R.id.edtPass);
        edtMKhau = findViewById(R.id.edtMKhau);
        cbRemember = findViewById(R.id.cbRemember);
        btnIn = findViewById(R.id.btnIn);
        btnUp = findViewById(R.id.btnUp);
        btnForgot =(Button) findViewById(R.id.btnForgot);
        tvEmailerror= findViewById(R.id.tvEmailError);
        tvPasserror= findViewById(R.id.tvPassError);
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
//        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!_&*]).{8,}$";
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

}