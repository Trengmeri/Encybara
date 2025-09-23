package com.example.test;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.test.model.Notification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class NotificationStorage {
    private static final String PREF_NAME = "user_notifications";
    private static NotificationStorage instance;
    private final SharedPreferences sharedPreferences;

    private NotificationStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized NotificationStorage getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationStorage(context);
        }
        return instance;
    }

    public void saveNotification(String id, String title, String message, String date) {
        try {
            JSONArray notifications = getNotificationsArray(id);

            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("message", message);
            notification.put("date", date);
            notifications.put(notification);

            sharedPreferences.edit().putString(id, notifications.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getNotifications(String id) {
        List<Notification> notificationsList = new ArrayList<>();
        try {
            JSONArray notifications = getNotificationsArray(id);
            for (int i = 0; i < notifications.length(); i++) {
                JSONObject obj = notifications.getJSONObject(i);
                notificationsList.add(new Notification(
                        obj.getString("title"),
                        obj.getString("message"),
                        obj.getString("date")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return notificationsList;
    }

    private JSONArray getNotificationsArray(String id) {
        String storedData = sharedPreferences.getString(id, "[]");
        try {
            return new JSONArray(storedData);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }
}

