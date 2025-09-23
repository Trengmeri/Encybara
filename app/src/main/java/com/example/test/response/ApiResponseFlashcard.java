package com.example.test.response;

import com.example.test.model.Flashcard;
import com.example.test.model.Pageable;
import com.example.test.model.Sort;

import java.util.List;

public class ApiResponseFlashcard {
    private int statusCode;
    private String message;
    private FlashcardData data;

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

    public FlashcardData getData() {
        return data;
    }

    public void setData(FlashcardData data) {
        this.data = data;
    }

    public static class FlashcardData {
        private List<Flashcard> content;
        private Pageable pageable;
        private int totalPages;
        private int totalElements;
        private boolean last;
        private int size;
        private int number;
        private Sort sort;
        private int numberOfElements;
        private boolean first;
        private boolean empty;

        public List<Flashcard> getContent() {
            return content;
        }

        public void setContent(List<Flashcard> content) {
            this.content = content;
        }

        public int getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(int totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isLast() {
            return last;
        }

        public void setLast(boolean last) {
            this.last = last;
        }
    }
}