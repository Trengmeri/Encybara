package com.example.test;

import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onStart() {
        super.onStart();
        applyNonVietnameseFilter(findViewById(android.R.id.content));
    }

    private void applyNonVietnameseFilter(View view) {
        if (view instanceof EditText) {
            ((EditText) view).setFilters(new InputFilter[]{new NonVietnameseFilter()});
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyNonVietnameseFilter(group.getChildAt(i));
            }
        }
    }
}

