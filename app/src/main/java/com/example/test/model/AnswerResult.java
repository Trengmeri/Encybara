package com.example.test.model;

public class AnswerResult {
    private final boolean isCorrect;
    private final int currentScore;
    private final boolean gameCompleted;
    private final String errorMessage; // Dùng để truyền lỗi nghiệp vụ (ví dụ: "Game session has ended")

    // Constructor cho trường hợp thành công (trả lời đúng/sai)
    public AnswerResult(boolean isCorrect, int currentScore, boolean gameCompleted) {
        this.isCorrect = isCorrect;
        this.currentScore = currentScore;
        this.gameCompleted = gameCompleted;
        this.errorMessage = null;
    }

    // Constructor cho trường hợp lỗi nghiệp vụ (Game Ended)
    public AnswerResult(String errorMessage) {
        this.isCorrect = false;
        this.currentScore = 0;
        this.gameCompleted = true;
        this.errorMessage = errorMessage;
    }

    // Getters
    public boolean isCorrect() { return isCorrect; }
    public int getCurrentScore() { return currentScore; }
    public boolean isGameCompleted() { return gameCompleted; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isError() { return errorMessage != null; }
}