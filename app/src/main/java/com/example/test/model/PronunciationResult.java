package com.example.test.model;

import java.io.Serializable;
import java.util.List;

public class PronunciationResult implements Serializable {
    private double overallScore;
    private List<PhonemeScore> phonemeScores;

    public PronunciationResult(double overallScore, List<PhonemeScore> phonemeScores) {
        this.overallScore = overallScore;
        this.phonemeScores = phonemeScores;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public List<PhonemeScore> getPhonemeScores() {
        return phonemeScores;
    }
}