package com.example.test.ui.home.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.HashMap;
import java.util.Map;

import com.example.test.ui.home.HomeFragment;
import com.example.test.ui.explore.ExploreFragment;
import com.example.test.ui.profile.ProfileFragment;
import com.example.test.ui.study.StudyFragment;

public class MainAdapter extends FragmentStateAdapter {
    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public MainAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (!fragmentMap.containsKey(position)) {
            switch (position) {
                case 0:
                    fragmentMap.put(0, new HomeFragment());
                    break;
                case 1:
                    fragmentMap.put(1, new StudyFragment());
                    break;
                case 2:
                    fragmentMap.put(2, new ExploreFragment());
                    break;
                case 3:
                    fragmentMap.put(3, new ProfileFragment());
                    break;
            }
        }
        return fragmentMap.get(position);
    }

    @Override
    public int getItemCount() {
        return 4; // Số lượng Fragment
    }

    // Hàm này giúp lấy Fragment theo vị trí, tránh tạo lại không cần thiết
    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }
}
