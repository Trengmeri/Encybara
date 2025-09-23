package com.example.test;

import android.util.Log;

import com.example.test.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private static NotificationManager instance;
    private List<Notification> notificationList;

    private NotificationManager() {
        notificationList = new ArrayList<>();
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void addNotification(String title, String message, String date) {
        notificationList.add(new Notification(title, message, date));
        Log.d("NotificationManager", "Thông báo mới đã được thêm: " + title);
    }

    public List<Notification> getNotifications() {
        return notificationList;
    }
}

