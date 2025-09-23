package com.example.test.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.NotificationStorage;
import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.ui.home.HomeActivity;
import com.example.test.adapter.NotificationAdapter;
import com.example.test.model.Notification;

import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    TextView notiBack;
    RecyclerView recyclerView;
    NotificationAdapter adapter;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notiBack= findViewById(R.id.notiBack);
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        notiBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NotificationActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
//        // Lấy danh sách thông báo và hiển thị
//        List<Notification> notifications = NotificationManager.getInstance().getNotifications();
//        NotificationAdapter adapter = new NotificationAdapter(notifications);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 📌 Lấy userID từ SharedPreferences
        String userID = SharedPreferencesManager.getInstance(this).getID();

        // 📌 Lấy thông báo từ SharedPreferences theo userID
        List<Notification> notifications = NotificationStorage.getInstance(this).getNotifications(userID);

        adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);

    }
}