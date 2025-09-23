package com.example.test.model;

public class EnglishLevel {
    private String name;
    private String displayName;
    private double minScore;
    private double maxScore;

    public EnglishLevel(String name, String displayName, double minScore, double maxScore) {
        this.name = name;
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public double getMinScore() { return minScore; }
    public double getMaxScore() { return maxScore; }
}
