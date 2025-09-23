package com.example.test;

import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.text.Spanned;
import android.text.GetChars;

public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    private static class PasswordCharSequence implements CharSequence {
        private CharSequence mSource;

        PasswordCharSequence(CharSequence source) {
            mSource = source; // Store char sequence
        }

        public char charAt(int index) {
            return '*';  // All characters are replaced with '*'
        }

        public int length() {
            return mSource.length();
        }

        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end);
        }
    }
}

