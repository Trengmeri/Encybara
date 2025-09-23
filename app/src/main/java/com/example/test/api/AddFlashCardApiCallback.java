package com.example.test.api;

public interface AddFlashCardApiCallback<T> {
    void onSuccess();
    void onSuccess(T response);
    void onFailure(String errorMessage);
}
