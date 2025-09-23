package com.example.test.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.test.R;
import com.example.test.ui.home.adapter.MainAdapter;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.api.ResultManager;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    LinearLayout btnstudy,btnexplore,btnprofile, icHome;
    ViewPager2 vpgMain;
    LinearLayout bottomBar;
    ImageView imgHome, imgStudy, imgExplore, imgProfile;
    TextView txtHome, txtStudy, txtExplore, txtProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);


        vpgMain = findViewById(R.id.vpg_main);
        bottomBar = findViewById(R.id.bottom_bar);

        // Gán Adapter cho ViewPager2
        vpgMain.setAdapter(new MainAdapter(this));
        vpgMain.setCurrentItem(0);
        vpgMain.setOffscreenPageLimit(3);

        icHome= bottomBar.findViewById(R.id.ic_home);
        btnexplore= bottomBar.findViewById(R.id.ic_explore);
        btnprofile= bottomBar.findViewById(R.id.ic_profile);
        btnstudy = bottomBar.findViewById(R.id.ic_study);

        imgHome = findViewById(R.id.imgHome);
        imgStudy = findViewById(R.id.imgStudy);
        imgExplore = findViewById(R.id.imgEx);
        imgProfile = findViewById(R.id.imgPro);

        txtHome = findViewById(R.id.txtHome);
        txtStudy = findViewById(R.id.txtStudy);
        txtExplore = findViewById(R.id.txtEx);
        txtProfile = findViewById(R.id.txtPro);

        vpgMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIconColors(position);
            }
        });
        icHome.setOnClickListener(v -> reloadFragment(0));
        btnstudy.setOnClickListener(v -> reloadFragment(1));
        btnexplore.setOnClickListener(v -> reloadFragment(2));
        btnprofile.setOnClickListener(v -> reloadFragment(3));


        updateIconColors(0);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật Intent mới

        int targetPage = intent.getIntExtra("targetPage", 0);
        ViewPager2 viewPager = findViewById(R.id.vpg_main);
        viewPager.setCurrentItem(targetPage, true);
        reloadFragment(targetPage);
    }

    private void reloadFragment(int position) {
        MainAdapter adapter = (MainAdapter) vpgMain.getAdapter();
        if (adapter != null) {
            Fragment fragment = adapter.getFragment(position);
            if (fragment != null && fragment.isAdded()) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragment)
                        .commit();
            }
            adapter.notifyItemChanged(position);
        }

        vpgMain.setCurrentItem(position, false);
    }



    private void updateIconColors(int position) {
        int selectedColor = ContextCompat.getColor(this, R.color.color_selected);
        int unselectedColor = ContextCompat.getColor(this, R.color.color_unselected);

        // Home
        imgHome.setColorFilter(position == 0 ? selectedColor : unselectedColor);
        txtHome.setTextColor(position == 0 ? selectedColor : unselectedColor);

        // Study
        imgStudy.setColorFilter(position == 1 ? selectedColor : unselectedColor);
        txtStudy.setTextColor(position == 1 ? selectedColor : unselectedColor);

        // Explore
        imgExplore.setColorFilter(position == 2 ? selectedColor : unselectedColor);
        txtExplore.setTextColor(position == 2 ? selectedColor : unselectedColor);

        // Profile
        imgProfile.setColorFilter(position == 3 ? selectedColor : unselectedColor);
        txtProfile.setTextColor(position == 3 ? selectedColor : unselectedColor);
    }
    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("Language", "vn");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

}