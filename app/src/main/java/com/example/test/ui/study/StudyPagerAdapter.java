package com.example.test.ui.study;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.test.model.Course;

import java.util.ArrayList;
import java.util.List;

public class StudyPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();
    private AllCourseFragment allCourseFragment;
    private MyCourseFragment myCourseFragment;


    public StudyPagerAdapter(@NonNull FragmentManager fm, @NonNull Lifecycle lifecycle) {
        super(fm, lifecycle);
        allCourseFragment = new AllCourseFragment();
        myCourseFragment = new MyCourseFragment();
    }
    public void addFragment(Fragment fragment) {
        fragmentList.add(fragment);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MyCourseFragment();
            case 1:
                return new AllCourseFragment();
            default:
                return new MyCourseFragment();
        }
    }


    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    // Override để tránh lỗi Fragment bị mất trạng thái
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        for (Fragment fragment : fragmentList) {
            if (fragment.hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }

}
