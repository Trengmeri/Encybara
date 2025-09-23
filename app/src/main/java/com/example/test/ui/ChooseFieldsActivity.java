package com.example.test.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.FieldManager;
import com.example.test.model.Field;
import com.example.test.ui.question_data.RecordQuestionActivity;

import java.util.ArrayList;
import java.util.List;

public class ChooseFieldsActivity extends AppCompatActivity {
    private Button btnEco, btnConstruct, btnIT, btnMechan, btnOther;
    private FieldManager fieldManager;
    private List<String> fields = new ArrayList<>();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_fields);

        initViews();
        fieldManager = new FieldManager(this);
        fetchFields();
    }

    private void initViews() {
        btnEco = findViewById(R.id.btnEconomic);
        btnConstruct = findViewById(R.id.btnConstruct);
        btnIT = findViewById(R.id.btnIT);
        btnMechan = findViewById(R.id.btnMechanic);
        btnOther = findViewById(R.id.btnOthers);
    }

    private void fetchFields() {
        fieldManager.fetchFields(new ApiCallback<List<Field>>() {
            @Override
            public void onSuccess(List<Field> result) {
                runOnUiThread(() -> {
                    fields.clear();
                    for (Field field : result) {
                        fields.add(field.getName());
                    }
                    updateButtonTexts();
                    setupClickListeners();
                });
            }

            @Override
            public void onSuccess() {
                // Not used
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(ChooseFieldsActivity.this,
                                "Error loading fields: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateButtonTexts() {
        if (fields.size() >= 5) {
            btnEco.setText(fields.get(0));
            btnConstruct.setText(fields.get(1));
            btnIT.setText(fields.get(2));
            btnMechan.setText(fields.get(3));
            btnOther.setText(fields.get(4));
        }
    }

    private void setupClickListeners() {
        View.OnClickListener listener = v -> {
            Button clicked = (Button) v;
            String selectedField = clicked.getText().toString();
            updateUserField(selectedField);
        };

        btnEco.setOnClickListener(listener);
        btnConstruct.setOnClickListener(listener);
        btnIT.setOnClickListener(listener);
        btnMechan.setOnClickListener(listener);
        btnOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseFieldsActivity.this, Test2.class);
                startActivity(intent);

            }
        });
    }

    private void updateUserField(String field) {
        fieldManager.updateUserField(field, new ApiCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ChooseFieldsActivity.this,
                            "Field updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChooseFieldsActivity.this, SelectActivity.class);
                    startActivity(intent);
                    finish();
                });
            }


            @Override
            public void onSuccess(Object result) {
                // Not used
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(ChooseFieldsActivity.this,
                                "Error updating field: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//        // Kiểm tra nếu chưa chọn trong SelectActivity thì mới lưu
//        boolean hasSelectedOption = sharedPreferences.getBoolean("hasSelectedOption", false);
//        if (!hasSelectedOption) {
//            editor.putString("lastActivity", this.getClass().getName());
//            editor.apply();
//        }
//    }


}