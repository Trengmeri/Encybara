package com.example.test.ui.explore;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.model.User;
import com.example.test.ui.DictionaryActivity;
import com.example.test.ui.GroupFlashcardActivity;
import com.example.test.ui.schedule.ScheduleActivity;

public class ExploreFragment extends Fragment {
    private ImageView btnstudy,btnhome,btnprofile;
    LinearLayout btnChat, btnDic, btnVoice, btnFlash, btnSche;
    TextView tvName;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.activity_explore, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnstudy = view.findViewById(R.id.ic_study);
        btnhome = view.findViewById(R.id.ic_home);
        btnprofile = view.findViewById(R.id.ic_profile);
        btnFlash= view.findViewById(R.id.btnFlash);
        btnSche= view.findViewById(R.id.btnSche);
        btnDic= view.findViewById(R.id.btnDic);
        tvName= view.findViewById(R.id.tvName);
        if (isAdded() && getContext() != null) {
            User user = SharedPreferencesManager.getInstance(getContext()).getUser();
            if (user != null) {
                tvName.setText("Hi " + user.getName() + "!");
            }
        }

        btnFlash.setOnClickListener(v -> openActivity(GroupFlashcardActivity.class));
        btnSche.setOnClickListener(v -> openActivity(ScheduleActivity.class));
        btnDic.setOnClickListener(v -> openActivity(DictionaryActivity.class));


    }
    private void openActivity(Class<?> activityClass) {
        if (isAdded() && getActivity() != null) {
            startActivity(new Intent(getActivity(), activityClass));
        }
    }

}
