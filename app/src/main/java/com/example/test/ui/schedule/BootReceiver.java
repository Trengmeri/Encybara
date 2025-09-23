package com.example.test.ui.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.test.SharedPreferencesManager;
import com.example.test.api.ApiCallback;
import com.example.test.api.ScheduleManager;
import com.example.test.model.Schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "📥 Đang lấy lịch học từ API...");
            rescheduleAlarms(context);

            ScheduleManager scheduleManager = new ScheduleManager(context);

            scheduleManager.fetchSchedulesByUserId(new ApiCallback<List<Schedule>>() {
                @Override
                public void onSuccess() {}

                @Override
                public void onSuccess(List<Schedule> schedules) {
                    Log.d("BootReceiver", "✅ Nhận được " + schedules.size() + " lịch học");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    long currentTime = System.currentTimeMillis();

                    for (Schedule schedule : schedules) {
                        try {
                            long scheduleTime = sdf.parse(schedule.getScheduleTime()).getTime();
                            if (scheduleTime > currentTime) { // Chỉ đặt lịch chưa đến giờ
                                Log.d("BootReceiver", "📅 Lên lịch báo thức cho: " + schedule.getScheduleTime());
                                AlarmScheduler.scheduleAlarm(context, schedule.getScheduleTime());
                            } else {
                                Log.d("BootReceiver", "⏳ Bỏ qua lịch cũ: " + schedule.getScheduleTime());
                            }
                        } catch (ParseException e) {
                            Log.e("BootReceiver", "❌ Lỗi khi phân tích thời gian: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("BootReceiver", "❌ Lỗi lấy lịch học: " + errorMessage);
                }
            });
        }
    }

    public static void rescheduleAlarms(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        Set<String> alarms = sharedPreferences.getStringSet("alarms", new HashSet<>());

        if (alarms.isEmpty()) {
            Log.d("BootReceiver", "📭 Không có báo thức nào để khôi phục.");
            return;
        }

        for (String alarmData : alarms) {
            try {
                String[] parts = alarmData.split(":");
                int requestCode = Integer.parseInt(parts[0]); // Đọc requestCode từ SharedPreferences
                String scheduleTime = parts[1];

                Log.d("BootReceiver", "🔄 Khôi phục báo thức: ID=" + requestCode + ", Time=" + scheduleTime);

                // Gọi AlarmScheduler với requestCode đã lưu, không tạo mới!
                AlarmScheduler.scheduleAlarmWithRequestCode(context, scheduleTime, requestCode);
            } catch (Exception e) {
                Log.e("BootReceiver", "❌ Lỗi khi đọc báo thức từ SharedPreferences: " + e.getMessage());
            }
        }
    }



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
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

}
