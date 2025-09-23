package com.example.test.model;

public class MediaFile {
    private int id;
    private String fileType; // Loại file (ví dụ: "mp3", "jpg", "png")
    private String materLink; // Đường dẫn đến file
    private String uploadedAt; // Thời gian upload
    private Integer questionId; // ID của câu hỏi (có thể null)
    private Integer lessonId; // ID của bài học (có thể null)

    // Constructor
    public MediaFile(String fileType, String materLink, String uploadedAt, Integer questionId, Integer lessonId) {
        this.fileType = fileType;
        this.materLink = materLink;
        this.uploadedAt = uploadedAt;
        this.questionId = questionId;
        this.lessonId = lessonId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getMaterLink() {
        return materLink;
    }

    public void setMaterLink(String materLink) {
        this.materLink = materLink;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getLessonId() {
        return lessonId;
    }

    public void setLessonId(Integer lessonId) {
        this.lessonId = lessonId;
    }
}