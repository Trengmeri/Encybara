package com.example.test.response;

import com.example.test.model.Flashcard;
import com.example.test.model.FlashcardGroup;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FlashcardGroupResponse {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private FlashcardGroupData data;

    public static class FlashcardGroupData {
        @SerializedName("content")
        private List<FlashcardGroup> content;
        private int totalPages;
        private int totalElements;

        public List<FlashcardGroup> getContent() {
            return content;
        }

        public void setContent(List<FlashcardGroup> content) {
            this.content = content;
        }
        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        public int getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(int totalElements) {
            this.totalElements = totalElements;
        }
    }

    // Getters và Setters
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FlashcardGroupData getData() {
        return data;
    }

    public void setData(FlashcardGroupData data) {
        this.data = data;
    }
}