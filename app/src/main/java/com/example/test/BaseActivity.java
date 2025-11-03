package com.example.test;

import android.content.Context;
import android.graphics.Rect;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v != null && v instanceof android.widget.EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}

