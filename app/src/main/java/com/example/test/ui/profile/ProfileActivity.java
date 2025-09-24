package com.example.test.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
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
import com.example.test.ui.explore.ExploreActivity;
import com.example.test.ui.home.HomeActivity;
import com.example.test.ui.study.StudyActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {

    TextView userName, userEmail;
    private static final int EDIT_PROFILE_REQUEST = 100;
    LinearLayout btnLogout, btnedit, term, language;
    NetworkChangeReceiver networkReceiver;
    AuthenticationManager apiManager;
    private UserManager userManager;
    private ImageView imgAvatar;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile); // dùng lại layout fragment_profile

        userManager = new UserManager(this);
        imgAvatar = findViewById(R.id.imgAvatar);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);

        loadUserProfile();
        setupBottomBar();

        btnLogout = findViewById(R.id.btnLogout);
        btnedit = findViewById(R.id.btnEdit);
        term = findViewById(R.id.term);
        language = findViewById(R.id.language);

        language.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LanguageActivity.class);
            startActivity(intent);
        });

        term.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, TermActivity.class);
            startActivity(intent);
        });

        networkReceiver = new NetworkChangeReceiver();
        apiManager = new AuthenticationManager(this);

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isRemembered = sharedPreferences.getBoolean("rememberMe", false);
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPassword = sharedPreferences.getString("password", "");

        Log.d("ProfileActivity", "After Logout - Remember Me: " + isRemembered);
        Log.d("ProfileActivity", "After Logout - Saved Email: " + savedEmail);
        Log.d("ProfileActivity", "After Logout - Saved Password: " + savedPassword);

        btnedit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST) {
            loadUserProfile();
        }
    }

    private void loadUserProfile() {
        String userId = SharedPreferencesManager.getInstance(this).getID();
        if (userId == null) return;

        userManager.fetchUserProfile(Integer.parseInt(userId), new ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                runOnUiThread(() -> {
                    try {
                        userName.setText(result.getString("name"));
                        userEmail.setText(result.getString("email"));
                        String avatarUrl = result.optString("avatar");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            avatarUrl = avatarUrl.replace("0.0.0.0", "14.225.198.3");

                            Glide.with(ProfileActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.img_avt_profile)
                                    .error(R.drawable.img_avt_profile)
                                    .circleCrop()
                                    .into(imgAvatar);
                        }
                    } catch (JSONException e) {
                        Log.e("ProfileActivity", "Error parsing profile data: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onSuccess() { }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(ProfileActivity.this,
                                "Failed to load profile: " + errorMessage,
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dangxuat));
        builder.setMessage(getString(R.string.cfdangxuat));

        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            apiManager.sendLogoutRequest(new ApiCallback() {
                @Override
                public void onSuccess() {
                    SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Log.d("Logout", "Đã xóa SharedPreferences");
                    Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishAffinity();
                }

                @Override
                public void onSuccess(Object result) {}

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /** Bottom bar navigation **/
    private void setupBottomBar() {
        LinearLayout icHome = findViewById(R.id.ic_home);
        LinearLayout icStudy = findViewById(R.id.ic_study);
        LinearLayout icExplore = findViewById(R.id.ic_explore);
        LinearLayout icProfile = findViewById(R.id.ic_profile);

        icProfile.setOnClickListener(v -> {
            if (!(ProfileActivity.this instanceof ProfileActivity)) {
                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                overridePendingTransition(0,0);
                finish();
            }
        });

        icStudy.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, StudyActivity.class));
            overridePendingTransition(0,0);
            finish();
        });

        icExplore.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ExploreActivity.class));
            overridePendingTransition(0,0);
            finish();
        });

        icHome.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            overridePendingTransition(0,0);
            finish();
        });
    }
}
