package com.example.test.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.test.NetworkChangeReceiver;
import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.api.ApiCallback;
import com.example.test.api.AuthenticationManager;
import com.example.test.api.UserManager;
import com.example.test.ui.SignInActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileFragment extends Fragment {
    TextView userName, userEmail;
    private static final int EDIT_PROFILE_REQUEST = 100;
    LinearLayout btnLogout,btnedit, term , language;
    NetworkChangeReceiver networkReceiver;
    AuthenticationManager apiManager;
    private UserManager userManager;
    private ImageView imgAvatar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }
    private boolean isFragmentActive() {
        return isAdded() && getActivity() != null;
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        userManager = new UserManager(requireContext());
        imgAvatar = view.findViewById(R.id.imgAvatar);
        userName = view.findViewById(R.id.userName);
            userEmail = view.findViewById(R.id.userEmail);

//            User user = SharedPreferencesManager.getInstance(getContext()).getUser();
//            Log.d("Userow", "abc: " + SharedPreferencesManager.getInstance(getContext()).getUser());
//            if (user != null) {
//                userName.setText(user.getName());
//                userEmail.setText(user.getEmail());
//            }
        loadUserProfile();
            btnLogout= view.findViewById(R.id.btnLogout);
            btnedit= view.findViewById(R.id.btnEdit);

            term = view.findViewById(R.id.term);
            language = view.findViewById(R.id.language);

        language.setOnClickListener(v -> {
            if (isFragmentActive()) {
                Intent intent = new Intent(getActivity(), LanguageActivity.class);
                startActivity(intent);
            }
        });


        term.setOnClickListener(v -> {
            if (isFragmentActive()) {
                Intent intent = new Intent(getActivity(), TermActivity.class);
                startActivity(intent);
            }
        });

        // Tạo đối tượng NetworkChangeReceiver
            networkReceiver = new NetworkChangeReceiver();
            apiManager = new AuthenticationManager(requireContext());
            btnLogout.setOnClickListener(v -> showLogoutDialog());
            SharedPreferences sharedPreferences = null;
            if (isFragmentActive()) {
                sharedPreferences = getActivity().getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            }
        boolean isRemembered = sharedPreferences.getBoolean("rememberMe", false);
            String savedEmail = sharedPreferences.getString("email", "");
            String savedPassword = sharedPreferences.getString("password", "");

            Log.d("ProfileFragment", "After Logout - Remember Me: " + isRemembered);
            Log.d("ProfileFragment", "After Logout - Saved Email: " + savedEmail);
            Log.d("ProfileFragment", "After Logout - Saved Password: " + savedPassword);

        btnedit.setOnClickListener(v -> {
            if (isFragmentActive()) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivityForResult(intent, EDIT_PROFILE_REQUEST);
            }
        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST) {
            loadUserProfile(); // Load ngay khi quay lại từ EditProfile
        }
    }
    private void loadUserProfile() {
        String userId = SharedPreferencesManager.getInstance(requireContext()).getID();
        if (userId == null) return;

        userManager.fetchUserProfile(Integer.parseInt(userId), new ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                if (!isFragmentActive()) return;

                getActivity().runOnUiThread(() -> {
                    if (!isFragmentActive()) return;

                    try {
                        userName.setText(result.getString("name"));
                        userEmail.setText(result.getString("email"));
                        String avatarUrl = result.optString("avatar");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            avatarUrl = avatarUrl.replace("0.0.0.0", "14.225.198.3");

                            Glide.with(ProfileFragment.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.img_avt_profile)
                                    .error(R.drawable.img_avt_profile)
                                    .circleCrop()
                                    .into(imgAvatar);
                        }
                    } catch (JSONException e) {
                        Log.e("ProfileFragment", "Error parsing profile data: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isFragmentActive()) return;

                getActivity().runOnUiThread(() -> {
                    if (!isFragmentActive()) return;

                    Toast.makeText(getContext(),
                            "Failed to load profile: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile when returning from EditProfile
        loadUserProfile();
    }

    private void showLogoutDialog() {
        if (!isFragmentActive()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dangxuat));
        builder.setMessage(getString(R.string.cfdangxuat));

        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            apiManager.sendLogoutRequest(new ApiCallback() {
                @Override
                public void onSuccess() {
                    if (!isFragmentActive()) return;

                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.commit();

                    Log.d("Logout", "Đã xóa SharedPreferences");
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finishAffinity();
                }

                @Override
                public void onSuccess(Object result) {}

                @Override
                public void onFailure(String errorMessage) {
                    if (isFragmentActive()) {
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}