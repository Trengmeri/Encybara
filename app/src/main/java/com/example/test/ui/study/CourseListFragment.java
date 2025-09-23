package com.example.test.ui.study;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.test.R;
import com.example.test.adapter.CourseAdapter;
import com.example.test.api.ApiCallback;
import com.example.test.api.EnrollmentManager;
import com.example.test.api.ResultManager;
import com.example.test.model.Course;
import com.example.test.model.Enrollment;
import com.example.test.ui.CourseInformationActivity;
import com.example.test.ui.VerticalSpaceItemDecoration;
import com.example.test.ui.home.HomeActivity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CourseListFragment extends Fragment {
    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private List<Course> courseList;
    private FrameLayout join;
    private ResultManager resultManager;

    public static CourseListFragment newInstance(List<Course> courses) {
        CourseListFragment fragment = new CourseListFragment();
        Bundle args = new Bundle();
        args.putSerializable("courses", (Serializable) courses);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_list, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        resultManager = new ResultManager(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        int spacingInPixels = (int) (20 * getResources().getDisplayMetrics().density); // convert 20dp to pixels
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(spacingInPixels));


        if (getArguments() != null) {
            courseList = (List<Course>) getArguments().getSerializable("courses");
            adapter = new CourseAdapter("None", getContext(), courseList);
            recyclerView.setAdapter(adapter);
        }
    }
}
