package com.example.test.api;

import java.util.List;

public interface ApiCallback<T> {
    void  onSuccess();

    void onSuccess(T result);  // Phương thức này sẽ nhận bất kỳ kiểu dữ liệu nào
    void onFailure(String errorMessage);
}
