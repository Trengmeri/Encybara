package com.example.test.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.ApiService;
import com.example.test.api.ResultManager;
import com.example.test.model.Enrollment;
import com.example.test.ui.englishlevel.EnglishLevelActivity;
import com.example.test.ui.home.HomeActivity;

public class SelectActivity extends AppCompatActivity {
    Button btnTest, btnNew, btnpick;
    TextView btnBack;
    int enrollmentId;
    ApiService apiService = new ApiService(this);
    ResultManager resultManager = new ResultManager(this);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select);
        btnNew = findViewById(R.id.btnNextProgram);
        btnTest = findViewById(R.id.btnTest);
        btnBack= findViewById(R.id.btnBack);
        btnpick = findViewById(R.id.btnpick);
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        btnpick.setOnClickListener(view -> {
            Intent intent = new Intent(SelectActivity.this, EnglishLevelActivity.class);
            startActivity(intent);
        });

        btnTest.setOnClickListener(view -> {
            apiService.startTest(new ApiCallback() {
                @Override
                public void onSuccess() {
                    resultManager.getEnrollment(1, new ApiCallback<Enrollment>() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onSuccess(Enrollment enrollment) {
                            enrollmentId = enrollment.getId();
                            Log.d("SelectActivity", String.valueOf(enrollmentId));
                            Intent intent = new Intent(SelectActivity.this, LoadingTestActivity.class);
                            intent.putExtra("enrollmentId", enrollmentId);
                            startActivity(intent);
//                 Lưu trạng thái đã chọn
                    editor.putBoolean("hasSelectedOption", true);
                    editor.putString("lastActivity", HomeActivity.class.getName()); // Chuyển đến HomeActivity
                    editor.apply();
                            finish(); // Đóng SelectActivity
                        }

                        @Override
                        public void onFailure(String errorMessage) {

                        }
                    });
                }

                @Override
                public void onSuccess(Object result) {

                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("DEBUG", "API Failed: " + errorMessage);

                }
            });
        });

        btnNew.setOnClickListener(view -> {
            apiService.skipTest(new ApiCallback() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(SelectActivity.this, HomeActivity.class);
                    startActivity(intent);
                    // Lưu trạng thái đã chọn
                editor.putBoolean("hasSelectedOption", true);
                editor.putString("lastActivity", HomeActivity.class.getName()); // Chuyển đến HomeActivity
                editor.apply();

                    finish(); // Đóng SelectActivity
                }

                @Override
                public void onSuccess(Object result) {

                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("DEBUG", "API Failed: " + errorMessage);

                }
            });
        });
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(SelectActivity.this, ChooseFieldsActivity.class);
            startActivity(intent);
        });
    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//        // Nếu chưa chọn gì, đánh dấu đang ở SelectActivity
//        boolean hasSelectedOption = sharedPreferences.getBoolean("hasSelectedOption", false);
//        if (!hasSelectedOption) {
//            editor.putString("lastActivity", this.getClass().getName());
//            editor.apply();
//        }
//    }
//@Override
//protected void onStop() {
//    super.onStop();
//    SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
//    SharedPreferences.Editor editor = sharedPreferences.edit();
//
//    boolean hasSelectedOption = sharedPreferences.getBoolean("hasSelectedOption", false);
//
//    if (!hasSelectedOption) {
//        Log.d("DEBUG", "Saving lastActivity as SelectActivity");  // Kiểm tra log
//        editor.putString("lastActivity", SelectActivity.class.getName());
//        editor.apply();
//    }
//}


}