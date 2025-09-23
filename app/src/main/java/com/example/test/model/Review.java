package com.example.test.model;

public class Review {
    private int id;          // ID của Review, thường được server trả về sau khi tạo
    private int userId;      // ID của người dùng tạo Review
    private int courseId;    // ID của khóa học mà Review gắn với
    private String reContent;// Nội dung đánh giá
    private String reSubject;// Chủ đề đánh giá
    private int numStar;     // Số sao đánh giá (ví dụ: 1-5)
    private String status;   // Trạng thái của Review (CONTRIBUTING, CONTENT, MISTAKE)
    private int numLike;     // Số lượt thích của Review
    private boolean liked;   // Trạng thái Liked của người dùng hiện tại

    // Constructor mặc định (cần cho Gson khi parse JSON)
    public Review() {
    }

    // Constructor đầy đủ tham số (dùng khi tạo Review cục bộ)
    public Review(int userId, int courseId, String reContent, String reSubject, int numStar, String status) {
        this.userId = userId;
        this.courseId = courseId;
        this.reContent = reContent;
        this.reSubject = reSubject;
        this.numStar = numStar;
        this.status = status;
        this.numLike = 0; // Giá trị mặc định
        this.liked = false; // Giá trị mặc định
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getReContent() {
        return reContent;
    }

    public void setReContent(String reContent) {
        this.reContent = reContent;
    }

    public String getReSubject() {
        return reSubject;
    }

    public void setReSubject(String reSubject) {
        this.reSubject = reSubject;
    }

    public int getNumStar() {
        return numStar;
    }

    public void setNumStar(int numStar) {
        this.numStar = numStar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNumLike() {
        return numLike;
    }

    public void setNumLike(int numLike) {
        this.numLike = numLike;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    // Phương thức toString để debug dễ dàng
    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", userId=" + userId +
                ", courseId=" + courseId +
                ", reContent='" + reContent + '\'' +
                ", reSubject='" + reSubject + '\'' +
                ", numStar=" + numStar +
                ", status='" + status + '\'' +
                ", numLike=" + numLike +
                ", liked=" + liked +
                '}';
    }
}