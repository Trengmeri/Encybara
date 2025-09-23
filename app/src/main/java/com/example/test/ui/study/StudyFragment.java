package com.example.test.ui.study;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;
import com.example.test.R;
import com.example.test.model.Course;

import java.util.List;

public class StudyFragment extends Fragment {
    private Button btnMyCourse, btnAllCourse;
    private ViewPager2 viewPager;
    private StudyPagerAdapter adapter;

    public StudyFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_study, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnMyCourse = view.findViewById(R.id.btnMyCourse);
        btnAllCourse = view.findViewById(R.id.btnAllCourse);
        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setSaveEnabled(false);

        adapter = new StudyPagerAdapter(getChildFragmentManager(), getLifecycle());
        adapter.addFragment(new MyCourseFragment());
        adapter.addFragment(new AllCourseFragment());
        viewPager.setAdapter(adapter);

       // btnMyCourse.setOnClickListener(v -> viewPager.setCurrentItem(0));
        btnMyCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
                btnMyCourse.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_mycourse));
                btnAllCourse.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_allcourse));
            }
        });
        //btnAllCourse.setOnClickListener(v -> viewPager.setCurrentItem(1));
        btnAllCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
                btnAllCourse.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_mycourse));
                btnMyCourse.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_allcourse));
            }
        });

        // Kiểm tra nếu có courseId được truyền vào
        Bundle args = getArguments();
        if (args != null && args.containsKey("selected_course_id")) {
            int selectedCourseId = args.getInt("selected_course_id");
            selectCourse(selectedCourseId);
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                refreshCurrentFragment(position);
            }
        });
    }

    private void refreshCurrentFragment(int position) {
        if (!isAdded()) return; // ⛔ Tránh gọi khi chưa attach

        Fragment fragment = getChildFragmentManager().findFragmentByTag("f" + position);
        if (fragment instanceof MyCourseFragment && position == 0) {
            ((MyCourseFragment) fragment).onResume();
        } else if (fragment instanceof AllCourseFragment && position == 1) {
            ((AllCourseFragment) fragment).onResume();
        }
    }


    public void selectCourse(int courseId) {
        Log.d("StudyFragment", "Selecting course with ID: " + courseId);
        viewPager.setCurrentItem(0, true);

        Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f0");
        if (currentFragment instanceof MyCourseFragment) {
            Log.d("StudyFragment", "Found existing MyCourseFragment");
            MyCourseFragment myCourseFragment = (MyCourseFragment) currentFragment;
            // Thay vì scroll, gọi phương thức mở lesson
            myCourseFragment.openFirstLesson(courseId);
        } else {
            Log.e("StudyFragment", "MyCourseFragment not found");
        }
    }
}