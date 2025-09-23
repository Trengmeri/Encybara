package com.example.test.ui.schedule;

import static android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.api.ApiCallback;
import com.example.test.api.ScheduleManager;
import com.example.test.model.Schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private TextView textViewReminderTimeHour, textViewReminderTimeMins, btnBacktoEx, Done;
    private ImageView up, down;
    ImageView Mon, Tue, Wed, Thu, Fri, Sat, Sun, check_icon2, check_icon3, check_icon4, check_icon5, check_icon6, check_icon7, check_icon0;
    ImageView Basic, Advance, LevelUp, check_basic, check_advance, check_levelup;
    private static final int PERMISSION_REQUEST_CODE = 101;

    private int currentHour = 0;
    private int currentMinute = 0;
    private int selectedGoal = 0; // 0: None, 1: Basic, 2: Advance, 3: LevelUp
    private ActivityResultLauncher<Intent> batteryOptimizationLauncher;
    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        requestNotificationPermission();
        requestIgnoreBatteryOptimizations();
        AlarmScheduler.logAllAlarms(this);


        textViewReminderTimeHour = findViewById(R.id.textViewReminderTimeHour);
        textViewReminderTimeMins = findViewById(R.id.textViewReminderTimeMins);
        btnBacktoEx = findViewById(R.id.btnBacktoEx);
        Done = findViewById(R.id.textViewDone);
        Done.setEnabled(false);

        Sun = findViewById(R.id.sun);
        Mon = findViewById(R.id.mon);
        Tue = findViewById(R.id.tue);
        Wed = findViewById(R.id.wed);
        Thu = findViewById(R.id.thu);
        Fri = findViewById(R.id.fri);
        Sat = findViewById(R.id.sat);
        Basic = findViewById(R.id.basic);
        Advance = findViewById(R.id.advance);
        LevelUp = findViewById(R.id.levelup);

        check_icon2 = findViewById(R.id.check_icon2);
        check_icon3 = findViewById(R.id.check_icon3);
        check_icon4 = findViewById(R.id.check_icon4);
        check_icon5 = findViewById(R.id.check_icon5);
        check_icon6 = findViewById(R.id.check_icon6);
        check_icon7 = findViewById(R.id.check_icon7);
        check_icon0 = findViewById(R.id.check_icon0);
        check_basic = findViewById(R.id.check_icon_basic);
        check_advance = findViewById(R.id.check_icon_advance);
        check_levelup = findViewById(R.id.check_icon_levelup);
        up = findViewById(R.id.up);   // Initialize the ImageView
        down = findViewById(R.id.down); // Initialize the ImageView

        updateTime(); // Set initial time display

        Basic.setOnClickListener(v -> {
            selectedGoal = 1; // Chọn Basic
            updateGoalSelectionUI();
        });

        Advance.setOnClickListener(v -> {
            selectedGoal = 2; // Chọn Advance
            updateGoalSelectionUI();
        });

        LevelUp.setOnClickListener(v -> {
            selectedGoal = 3; // Chọn LevelUp
            updateGoalSelectionUI();
        });

        Mon.setOnClickListener(v -> {
            Mon.setSelected(!Mon.isSelected());
            check_icon2.setVisibility(Mon.isSelected() ? View.VISIBLE : View.GONE);
            validateSchedule();
        });
        Tue.setOnClickListener(v -> {
            Tue.setSelected(!Tue.isSelected());
            check_icon3.setVisibility(Tue.isSelected() ? View.VISIBLE : View.GONE);
            validateSchedule();
        });
        Wed.setOnClickListener(v -> {
            Wed.setSelected(!Wed.isSelected());
            check_icon4.setVisibility(Wed.isSelected() ? View.VISIBLE : View.GONE);
            validateSchedule();
        });
        Thu.setOnClickListener(v -> {
            Thu.setSelected(!Thu.isSelected());
            check_icon5.setVisibility(Thu.isSelected() ? View.VISIBLE : View.GONE);
            validateSchedule();
        });
        Fri.setOnClickListener(v -> {
            Fri.setSelected(!Fri.isSelected());
            check_icon6.setVisibility(Fri.isSelected() ? View.VISIBLE : View.GONE);
            validateSchedule();
        });
        Sat.setOnClickListener(v -> {
            Sat.setSelected(!Sat.isSelected());
            check_icon7.setVisibility(Sat.isSelected() ? View.VISIBLE : View.GONE);
            validateSchedule();
        });
        Sun.setOnClickListener(v -> {
            Sun.setSelected(!Sun.isSelected());
            check_icon0.setVisibility(Sun.isSelected() ? View.VISIBLE : View.GONE);
            validateSchedule();
        });

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Determine whether to increment hour or minute based on focus
                if (textViewReminderTimeHour.isFocused()) {
                    incrementHour();
                } else if (textViewReminderTimeMins.isFocused()) {
                    incrementMinute();
                } else {
                    incrementHour();
                }
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Determine whether to decrement hour or minute based on focus
                if (textViewReminderTimeHour.isFocused()) {
                    decrementHour();
                } else if (textViewReminderTimeMins.isFocused()) {
                    decrementMinute();
                } else {
                    incrementHour();
                }
            }
        });


        // Handle focus changes (optional - for better UX)
        textViewReminderTimeHour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    textViewReminderTimeHour.setBackgroundResource(R.drawable.bg_focused);
                } else {
                    textViewReminderTimeHour.setBackgroundResource(android.R.color.transparent);
                }
            }
        });

        textViewReminderTimeMins.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    textViewReminderTimeMins.setBackgroundResource(R.drawable.bg_focused);
                } else {
                    textViewReminderTimeMins.setBackgroundResource(android.R.color.transparent);
                }
            }
        });


        btnBacktoEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent= new Intent(ScheduleActivity.this, ExploreFragment.class);
//                startActivity(intent);
                finish();
            }
        });

        // Add an onClick listener to a button to trigger scheduling
        Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị ProgressDialog
                progressDialog = new ProgressDialog(ScheduleActivity.this);
                progressDialog.setMessage(getString(R.string.load));
                progressDialog.setCancelable(false);
                progressDialog.show();
                createSchedule();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        batteryOptimizationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d("ScheduleActivity", "NGuoi dung cap quyen thanh cong");
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isIgnoringBatteryOptimizations()) {
            requestBatteryOptimization();
        }
    }

    private boolean isIgnoringBatteryOptimizations() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        return powerManager.isIgnoringBatteryOptimizations(getPackageName());
    }


    private void requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            batteryOptimizationLauncher.launch(intent);
        }
    }

    private void updateGoalSelectionUI() {
        check_basic.setVisibility(selectedGoal == 1? View.VISIBLE: View.GONE);
        check_advance.setVisibility(selectedGoal == 2? View.VISIBLE: View.GONE);
        check_levelup.setVisibility(selectedGoal == 3? View.VISIBLE: View.GONE);
    }

    private void incrementHour() {
        currentHour++;
        if (currentHour > 23) {
            currentHour = 0;
        }
        updateTime();
    }

    private void decrementHour() {
        currentHour--;
        if (currentHour < 0) {
            currentHour = 23;
        }
        updateTime();
    }

    private void incrementMinute() {
        currentMinute += 15;
        if (currentMinute >= 60) {
            currentMinute = 0;
            incrementHour(); // Increment hour when minutes roll over
        }
        updateTime();
    }

    private void decrementMinute() {
        currentMinute -= 15;
        if (currentMinute < 0) {
            currentMinute = 45;
            decrementHour(); // Decrement hour when minutes roll under
        }
        updateTime();
    }

    private void updateTime() {
        String hourStr = String.format("%02d", currentHour);
        String minuteStr = String.format("%02d", currentMinute);
        textViewReminderTimeHour.setText(hourStr);
        textViewReminderTimeMins.setText(minuteStr);
    }

    private void createSchedule() {
        List<Schedule> schedules = gatherScheduleData();

        if (schedules.isEmpty()) {
            Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show();
            return;
        }

        ScheduleManager scheduleManager = new ScheduleManager(this);
        for (Schedule schedule: schedules) {
            scheduleManager.createSchedule(schedule, new ApiCallback() {
                @Override
                public void onSuccess() {
                    Log.d("ScheduleActivity", "Tạo lịch học thành công: " + schedule.toString());
                    progressDialog.dismiss();
                    showDialog(getString(R.string.success), getString(R.string.lichmoi));
                }

                @Override
                public void onSuccess(Object result) {}

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("ScheduleActivity", "Lỗi tạo lịch học: " + errorMessage);
                    progressDialog.dismiss();
                    showDialog(getString(R.string.error), getString(R.string.trunglich));
                }
            });
        }
    }
    private void showDialog(String title, String message) {
        runOnUiThread(() -> {
            AlertDialog dialog = new AlertDialog.Builder(ScheduleActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .create();

            dialog.show();

            // Tự động đóng dialog & activity sau 2 giây
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                dialog.dismiss();
                finish(); // Kết thúc Activity
            }, 2000);
        });
    }




    private List<Schedule> gatherScheduleData() {
        String hourStr = textViewReminderTimeHour.getText().toString();
        String minuteStr = textViewReminderTimeMins.getText().toString();

        List<Schedule> schedules = new ArrayList<>();
        String userId = SharedPreferencesManager.getInstance(this).getID(); // Replace with your actual method

        // Check each day and add a schedule if selected
        if (Mon.isSelected()) {
            String mondayDate = convertDayOfWeekToDate(DayOfWeek.MONDAY);
            String scheduleTime = mondayDate + "T" + hourStr + ":" + minuteStr + ":00Z";
            schedules.add(new Schedule(userId, scheduleTime, false, null));
        }
        if (Tue.isSelected()) {
            String tuesdayDate = convertDayOfWeekToDate(DayOfWeek.TUESDAY);
            String scheduleTime = tuesdayDate + "T" + hourStr + ":" + minuteStr + ":00Z";
            schedules.add(new Schedule(userId, scheduleTime, false, null));
        }
        if (Wed.isSelected()) {
            String wednesdayDate = convertDayOfWeekToDate(DayOfWeek.WEDNESDAY);
            String scheduleTime = wednesdayDate + "T" + hourStr + ":" + minuteStr + ":00Z";
            schedules.add(new Schedule(userId, scheduleTime, false, null));
        }
        if (Thu.isSelected()) {
            String thursdayDate = convertDayOfWeekToDate(DayOfWeek.THURSDAY);
            String scheduleTime = thursdayDate + "T" + hourStr + ":" + minuteStr + ":00Z";
            schedules.add(new Schedule(userId, scheduleTime, false, null));
        }
        if (Fri.isSelected()) {
            String fridayDate = convertDayOfWeekToDate(DayOfWeek.FRIDAY);
            String scheduleTime = fridayDate + "T" + hourStr + ":" + minuteStr + ":00Z";
            schedules.add(new Schedule(userId, scheduleTime, false, null));
        }
        if (Sat.isSelected()) {
            String saturdayDate = convertDayOfWeekToDate(DayOfWeek.SATURDAY);
            String scheduleTime = saturdayDate + "T" + hourStr + ":" + minuteStr + ":00Z";
            schedules.add(new Schedule(userId, scheduleTime, false, null));
        }
        if (Sun.isSelected()) {
            String sundayDate = convertDayOfWeekToDate(DayOfWeek.SUNDAY);
            String scheduleTime = sundayDate + "T" + hourStr + ":" + minuteStr + ":00Z";
            schedules.add(new Schedule(userId, scheduleTime, false, null));
        }

        return schedules;
    }

    private void validateSchedule() {
        List<Schedule> schedules = gatherScheduleData();
        Done.setEnabled(!schedules.isEmpty());
        Done.setTextColor((getResources().getColor(R.color.btncolor)));
    }

    private void requestIgnoreBatteryOptimizations() {
        Log.d("MainActivity", "👉 Đang kiểm tra quyền thông báo...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 trở lên
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    public void requestNotificationPermission() {
        Log.d("MainActivity", "👉 Đang kiểm tra quyền thông báo...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 trở lên
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // Nhận kết quả khi người dùng chọn cấp quyền hoặc từ chối
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Quyền được cấp: " + permissions[i]);
                } else {
                    Log.e("MainActivity", "Quyền bị từ chối: " + permissions[i]);
                }
            }
        }
    }

    private String convertDayOfWeekToDate(DayOfWeek dayOfWeek) {
        // Lấy ngày hiện tại
        LocalDate currentDate = LocalDate.now();

        // Lấy ngày & giờ hiện tại
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();


        // Tính số ngày cần cộng để đến ngày được chọn
        int daysToAdd = (dayOfWeek.getValue() - currentDate.getDayOfWeek().getValue() + 7) % 7;
        LocalDate selectedDate = currentDate.plusDays(daysToAdd);

        // Nếu ngày được chọn là hôm nay nhưng thời gian đã qua, dời sang tuần sau
        if (daysToAdd == 0) { // Người dùng đặt lịch vào hôm nay
            int selectedHour = Integer.parseInt(textViewReminderTimeHour.getText().toString());
            int selectedMinute = Integer.parseInt(textViewReminderTimeMins.getText().toString());

            if (selectedHour < currentHour || (selectedHour == currentHour && selectedMinute <= currentMinute)) {
                // Nếu giờ đã qua, cộng thêm 7 ngày
                selectedDate = selectedDate.plusDays(7);
            }
        }

        // Trả về ngày đã được định dạng đúng
        return selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

}