package com.example.test.ui.schedule;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AlarmService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AlarmService", "🚀 Báo thức đang chạy trong Foreground Service...");
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
