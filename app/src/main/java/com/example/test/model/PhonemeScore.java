package com.example.test.model;

import java.io.Serializable;

public class PhonemeScore implements Serializable {
    private String character;
    private String phoneme;
    private String quality;
    private int wordIndex; // <-- THÊM DÒNG NÀY

    // Cập nhật constructor
    public PhonemeScore(String character, String phoneme, String quality, int wordIndex) {
        this.character = character;
        this.phoneme = phoneme;
        this.quality = quality;
        this.wordIndex = wordIndex; // <-- THÊM DÒNG NÀY
    }

    public String getCharacter() { return character; }
    public String getPhoneme() { return phoneme; }
    public String getQuality() { return quality; }
    public int getWordIndex() { return wordIndex; } // <-- THÊM HÀM NÀY
}