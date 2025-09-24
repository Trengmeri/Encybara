package com.example.test.ui.profile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.api.ApiCallback;
import com.example.test.api.BaseApiManager;
import com.example.test.api.UserManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class EditProfileActivity extends AppCompatActivity {

    TextView backtoProfile;
     static final int PICK_IMAGE = 1;
     ImageView imgAvatar;
     String currentAvatarUrl;
     EditText edtName, edtSdt;
     Spinner spnField;
     Button btnUpdate;
     UserManager userManager;
     String initialName = "";
     String initialPhone = "";
     int initialPosition = -1;
    String initialAvatarUrl = "";
     Spinner spnLevel;
     String initialLevel = "";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        spnLevel = findViewById(R.id.spnLevel);
        setupSpinners();
        enableUpdateButton(false);

        userManager = new UserManager(this);
        backtoProfile= findViewById(R.id.backtoProfile);

        backtoProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//        imgAvatar = findViewById(R.id.imgAvatar);
//        imgAvatar.setOnClickListener(v -> selectImage());
        loadUserProfile();
        btnUpdate.setOnClickListener(view -> updateProfile());
    }
    private void initViews() {
        backtoProfile = findViewById(R.id.backtoProfile);
        edtName = findViewById(R.id.edtName);
        edtSdt = findViewById(R.id.edtSdt);
        spnField = findViewById(R.id.spnField);
        btnUpdate = findViewById(R.id.btnUpdate);
    }
    private void setupChangeListeners() {
        initialAvatarUrl = currentAvatarUrl;
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkForChanges();
            }
        };

        edtName.addTextChangedListener(textWatcher);
        edtSdt.addTextChangedListener(textWatcher);

        spnField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkForChanges();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spnLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkForChanges();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void checkForChanges() {
        Log.d("EditProfile", "Initial Avatar URL: " + initialAvatarUrl);
        Log.d("EditProfile", "Current Avatar URL: " + currentAvatarUrl);
        boolean hasChanges = !edtName.getText().toString().equals(initialName) ||
                !edtSdt.getText().toString().equals(initialPhone) ||
                spnField.getSelectedItemPosition() != initialPosition ||
                !spnLevel.getSelectedItem().toString().equals(initialLevel) ||
                !currentAvatarUrl.equals(initialAvatarUrl);
        Log.d("EditProfile", "Has Changes: " + hasChanges);
        enableUpdateButton(hasChanges);
    }
    private void setupSpinners() {
        ArrayAdapter<CharSequence> fieldAdapter = ArrayAdapter.createFromResource(this,
                R.array.fields_array, android.R.layout.simple_spinner_item);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnField.setAdapter(fieldAdapter);

        // Setup level spinner
        ArrayAdapter<CharSequence> levelAdapter = ArrayAdapter.createFromResource(this,
                R.array.english_levels, android.R.layout.simple_spinner_item);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLevel.setAdapter(levelAdapter);


        spnField.setPadding(40, 25, 25, 25);
        spnField.setBackground(ContextCompat.getDrawable(this, R.drawable.spinner_background));
        spnField.setPopupBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rounded_corner));

        spnLevel.setPadding(40, 25, 25, 25);
        spnLevel.setBackground(ContextCompat.getDrawable(this, R.drawable.spinner_background));
        spnLevel.setPopupBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rounded_corner));
    }
    private void loadUserProfile() {
        String userId = SharedPreferencesManager.getInstance(this).getID();

        userManager.fetchUserProfile(Integer.parseInt(userId), new ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                runOnUiThread(() -> {
                    try {
                        initialName = result.getString("name");
                        edtName.setText(initialName);

                        String phoneNumber = "";
                        if (result.has("phone")) {
                            if (result.getString("phone").equals("null")) {
                                phoneNumber = "";
                            } else{
                                phoneNumber = result.getString("phone");
                            }
                        }
                        initialPhone = phoneNumber;
                        edtSdt.setText(initialPhone);

                        String speciField = result.getString("speciField");
                        ArrayAdapter adapter = (ArrayAdapter) spnField.getAdapter();
                        initialPosition = adapter.getPosition(speciField);
                        if (initialPosition >= 0) {
                            spnField.setSelection(initialPosition);
                        }

                        // Replace IP address in avatar URL
                        currentAvatarUrl = result.optString("avatar");
                        if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                            currentAvatarUrl = BaseApiManager.replaceHost(currentAvatarUrl);
                            Log.d("EditProfile", "Modified avatar URL: " + currentAvatarUrl);

                            imgAvatar = findViewById(R.id.imgAvatar); // Find the ImageView
                            imgAvatar.setOnClickListener(v -> selectImage()); // Set click listener

                            Glide.with(EditProfileActivity.this)
                                    .load(currentAvatarUrl)
                                    .placeholder(R.drawable.img_avt_profile)
                                    .error(R.drawable.img_avt_profile)
                                    .circleCrop()
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            Log.e("EditProfile", "Failed to load avatar: " + e.getMessage());
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            Log.d("EditProfile", "Avatar loaded successfully");
                                            return false;
                                        }
                                    })
                                    .into(imgAvatar);
                        }
                        String englishLevel = result.getString("englishlevel");
                        Log.d("EditProfile", "English level: " + englishLevel);
                        initialLevel = englishLevel;
                        ArrayAdapter levelAdapter = (ArrayAdapter) spnLevel.getAdapter();
                        int levelPosition = levelAdapter.getPosition(englishLevel);
                        Log.d("EditProfile", "Level position: " + englishLevel);
                        Log.d("EditProfile", "Available levels: " + (levelAdapter));

                        if (levelPosition >= 0) {
                            spnLevel.setSelection(levelPosition);
                            Log.d("EditProfile", "Selected level: " + spnLevel.getSelectedItem().toString());
                        }
                        else {
                            Log.d("EditProfile", "Level not found in spinner items");
                            spnLevel.setSelection(0);
                        }

                        setupChangeListeners();
                        enableUpdateButton(false);
                    } catch (JSONException e) {
                        Toast.makeText(EditProfileActivity.this,
                                "Error loading profile data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess() {
                // Not used
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this,
                            "Failed to load profile: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private void enableUpdateButton(boolean enable) {
        btnUpdate.setAlpha(enable ? 1.0f : 0.5f);
        btnUpdate.setEnabled(enable);
    }
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Avatar"), PICK_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            uploadAvatar(imageUri);
        }
    }
    private void uploadAvatar(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading avatar...");
        progressDialog.show();

        try {
            // Create temp file
            File tempFile = new File(getCacheDir(), "temp_avatar.jpg");

            // Copy image data to temp file
            try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
                 OutputStream outputStream = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // Upload the temp file
            String userId = SharedPreferencesManager.getInstance(this).getID();
            userManager.uploadAvatar(Integer.parseInt(userId), tempFile, new ApiCallback<String>() {
                @Override
                public void onSuccess(String newAvatarUrl) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        if (newAvatarUrl != null && !newAvatarUrl.isEmpty()) {
                            String modifiedUrl = BaseApiManager.replaceHost(newAvatarUrl);
                            currentAvatarUrl = modifiedUrl;
                            Glide.with(EditProfileActivity.this)
                                    .load(modifiedUrl)
                                    .placeholder(R.drawable.img_avt_profile)
                                    .error(R.drawable.img_avt_profile)
                                    .circleCrop()
                                    .into(imgAvatar);
                            checkForChanges();
                            setResult(RESULT_OK);
                            Toast.makeText(EditProfileActivity.this,
                                    "Avatar updated successfully", Toast.LENGTH_SHORT).show();
                        }
                        tempFile.delete(); // Clean up temp file
                    });
                }

                @Override
                public void onSuccess() {
                    // Not used
                }

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this,
                                "Failed to update avatar: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                        tempFile.delete(); // Clean up temp file
                    });
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void updateProfile() {
        String userId = SharedPreferencesManager.getInstance(this).getID();
        String name = edtName.getText().toString().trim();
        String phone = edtSdt.getText().toString().trim();
        String specialField = spnField.getSelectedItem().toString();
        String level = spnLevel.getSelectedItem().toString();

        if (name.isEmpty()) {
            edtName.setError("Name is required");
            return;
        }

        btnUpdate.setEnabled(false);
        Toast.makeText(this, "Updating profile...", Toast.LENGTH_SHORT).show();

        userManager.updateProfile(Integer.parseInt(userId), name, phone, specialField,level, new ApiCallback<Object>() {
            @Override
            public void onSuccess() {
                userManager.updateEnglishLevel(Integer.parseInt(userId), level, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(EditProfileActivity.this,
                                    "Profile and English level updated successfully", Toast.LENGTH_SHORT).show();
                            btnUpdate.setEnabled(true);
                            setResult(RESULT_OK);
                            finish();
                        });
                    }

                    @Override
                    public void onSuccess(Void result) {

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        runOnUiThread(() -> {
                            Toast.makeText(EditProfileActivity.this,
                                    "Failed to update English level: " + errorMessage, Toast.LENGTH_SHORT).show();
                            btnUpdate.setEnabled(true);
                        });
                    }
                });
            }

            @Override
            public void onSuccess(Object result) {
                // Not used in this case
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this,
                            "Update failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    btnUpdate.setEnabled(true);
                });
            }
        });
    }
}