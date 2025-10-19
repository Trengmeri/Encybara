package com.example.test;

import android.text.InputFilter;
import android.text.Spanned;

public class NonVietnameseFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        // Regex chỉ cho phép ký tự không dấu (a-z, A-Z, 0-9, ký tự đặc biệt)
        if (source.toString().matches(".*[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệ"
                + "ìíỉĩịòóỏõọôồốổỗộơờớởỡợ"
                + "ùúủũụưừứửữựỳýỷỹỵđÀÁẢÃẠĂẰẮẲẴẶÂẦẤẨẪẬ"
                + "ÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢ"
                + "ÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ].*")) {
            return ""; // chặn ký tự có dấu
        }
        return null; // cho phép bình thường
    }
}
