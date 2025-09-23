package com.example.test.ui.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AlarmScheduler {

    public static void scheduleAlarmWithRequestCode(Context context, String scheduleTime, int requestCode) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(sdf.parse(scheduleTime));
        } catch (ParseException e) {
            Log.e("AlarmScheduler", "❌ Lỗi khi phân tích thời gian: " + e.getMessage());
            return;
        }

        long triggerTime = calendar.getTimeInMillis();
        Log.d("AlarmScheduler", "⏰ Đặt lại báo thức: " + scheduleTime + " với requestCode: " + requestCode);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }


    public static void scheduleAlarm(Context context, String scheduleTime) {
        int requestCode = getNextRequestCode(context);
        Log.d("AlarmScheduler", "👉 Đặt báo thức cho: " + scheduleTime + " với requestCode: " + requestCode);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(sdf.parse(scheduleTime));
        } catch (ParseException e) {
            Log.e("AlarmScheduler", "❌ Lỗi khi phân tích thời gian: " + e.getMessage());
            return;
        }

        long triggerTime = calendar.getTimeInMillis();
        Log.d("AlarmScheduler", "⏰ Thời gian báo thức (millis): " + triggerTime);

        Intent intent = new Intent(context, AlarmReceiver.class);
        int flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, flag
        );


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.e("AlarmScheduler", "❌ Ứng dụng chưa được cấp quyền đặt báo thức chính xác!");
                return;
            }

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.d("AlarmScheduler", "✅ Báo thức đã được đặt vào: " + calendar.getTime());
            saveAlarmToSharedPreferences(context, requestCode, scheduleTime);
//            context.sendBroadcast(intent);
        } else {
            Log.e("AlarmScheduler", "❌ AlarmManager không hoạt động!");
        }
    }
    private static void saveAlarmToSharedPreferences(Context context, int requestCode, String scheduleTime) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> alarms = new HashSet<>(sharedPreferences.getStringSet("alarms", new HashSet<>()));
        alarms.add(requestCode + ":" + scheduleTime); // Lưu theo format "requestCode:scheduleTime"

        editor.putStringSet("alarms", alarms);
        editor.apply();

        Log.d("AlarmScheduler", "💾 Lịch báo thức đã lưu: ID=" + requestCode + ", Time=" + scheduleTime);
    }


    public static void logAllAlarms(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        Set<String> alarms = sharedPreferences.getStringSet("alarms", new HashSet<>());

        if (alarms.isEmpty()) {
            Log.d("AlarmScheduler", "📭 Không có lịch báo thức nào!");
        } else {
            Log.d("AlarmScheduler", "📋 Danh sách báo thức đã đặt:");
            for (String alarm : alarms) {
                Log.d("AlarmScheduler", "⏰ " + alarm);
            }
        }
    }
    private static int getNextRequestCode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        Set<String> alarms = sharedPreferences.getStringSet("alarms", new HashSet<>());

        int maxRequestCode = 0;
        for (String alarm : alarms) {
            try {
                int savedRequestCode = Integer.parseInt(alarm.split(":")[0]);
                if (savedRequestCode > maxRequestCode) {
                    maxRequestCode = savedRequestCode;
                }
            } catch (Exception e) {
                Log.e("AlarmScheduler", "⚠ Lỗi đọc requestCode từ SharedPreferences: " + e.getMessage());
            }
        }
        return maxRequestCode + 1; // Đảm bảo không bị trùng requestCode
    }
}
