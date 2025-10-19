package com.example.test;

import android.text.InputFilter;
import android.text.Spanned;

import java.text.Normalizer;

public class NonVietnameseFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        // Loại bỏ dấu tiếng Việt
        String normalized = Normalizer.normalize(source, Normalizer.Form.NFD);
        // Xóa toàn bộ ký tự dấu (tổ hợp Unicode)
        String noAccent = normalized.replaceAll("\\p{M}", "");

        // Trả về chuỗi không dấu để hiển thị
        return noAccent;
    }
}

