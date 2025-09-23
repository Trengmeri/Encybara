package com.example.test.model;

public class EvaluationResult {
    private String improvements;
    private String evaluation;
    private double score;

    public EvaluationResult(String improvements, String evaluation, double score) {
        this.improvements = improvements;
        this.evaluation = evaluation;
        this.score = score;
    }

    public EvaluationResult(double confidence) {
        this.score = Math.round(confidence * 10 * 10.0) / 10.0;

        if (confidence > 0.85) {
            evaluation = "Phát âm rất tốt!";
            improvements = "Tiếp tục duy trì phong độ!";
        } else if (confidence > 0.6) {
            evaluation = "Khá ổn";
            improvements = "Cố gắng phát âm rõ hơn ở các từ khó.";
        } else {
            evaluation = "Cần cải thiện";
            improvements = "Hãy nói chậm và rõ hơn, chú ý phát âm từng âm tiết.";
        }
    }


    public String getimprovements() {
        return improvements;
    }

    public void setimprovements(String improvements) {
        this.improvements = improvements;
    }

    public String getevaluation() {
        return evaluation;
    }

    public void setevaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public double getPoint() {
        return score;
    }

    public void setPoint(double score) {
        this.score = score;
    }
}
