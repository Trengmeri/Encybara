package com.example.test.ui.study;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.R;
import com.example.test.adapter.GroupAdapter;
import com.example.test.api.ApiCallback;
import com.example.test.api.CourseManager;
import com.example.test.api.LessonManager;
import com.example.test.model.Course;

import java.util.ArrayList;
import java.util.List;

public class AllCourseFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<String> groupList;
    private GroupAdapter adapter;
    private CourseManager courseManager;

    public AllCourseFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_course, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Kiểm tra nếu RecyclerView đang bị ẩn thì hiển thị lại
        recyclerView.setVisibility(View.VISIBLE);

        // Xoá `CourseListFragment` nếu nó đang hiển thị
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment courseListFragment = fragmentManager.findFragmentById(R.id.frameLayoutContainer);
        if (courseListFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(courseListFragment)
                    .commit();
            fragmentManager.popBackStack();
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        recyclerView = view.findViewById(R.id.recyclerView4);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupList = new ArrayList<>();
        adapter = new GroupAdapter(getContext(), groupList, groupName -> {
            fetchCourseByGroup(groupName);
        });
        recyclerView.setAdapter(adapter);

        courseManager = new CourseManager(getContext());
        fetchGroupCourses();
    }

    private void fetchGroupCourses() {
        courseManager.fetchGroupCourses(new ApiCallback<List<String>>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(List<String> data) {
                getActivity().runOnUiThread(() -> {
                    groupList.clear();
                    groupList.addAll(data);
                    adapter.notifyItemRangeChanged(0, groupList.size());
                });
            }


            @Override
            public void onFailure(String errorMessage) {
                Log.e(String.valueOf(getContext()), "Lỗi: " + errorMessage);
            }
        });
    }

    private void fetchCourseByGroup(String groupName) {
        recyclerView.setVisibility(View.GONE);
        getActivity().runOnUiThread(() -> {
            // Kiểm tra xem CourseListFragment đã tồn tại hay chưa
            Fragment existingFragment = getActivity().getSupportFragmentManager().findFragmentByTag("CourseListFragment");
            if (existingFragment != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .remove(existingFragment) // Xóa fragment cũ
                        .commit();
            }
        });
        courseManager.fetchCoursesByGroupName(groupName,new ApiCallback<List<Course>>() {
            @Override
            public void onSuccess() {}

            @Override
            public void onSuccess(List<Course> courseIds) {
                getActivity().runOnUiThread(() -> {
                    // Mở màn hình mới hoặc cập nhật RecyclerView để hiển thị danh sách khóa học
                    showCoursesFragment(courseIds);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("MyCourseFragment", "❌ Lỗi khi lấy danh sách khóa học: " + errorMessage);
            }
        });
    }

    private void showCoursesFragment(List<Course> courses) {
        CourseListFragment courseListFragment = CourseListFragment.newInstance(courses);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutContainer, courseListFragment)
                .addToBackStack(null)
                .commit();
    }
}