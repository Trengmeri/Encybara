package com.example.test.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.api.AddFlashCardApiCallback;
import com.example.test.api.FlashcardApiCallback;
import com.example.test.api.FlashcardManager;
import com.example.test.model.Definition;
import com.example.test.model.FlashcardGroup;
import com.example.test.model.FlashcardUtils;
import com.example.test.model.Meaning;
import com.example.test.model.Phonetic;
import com.example.test.model.WordData;
import com.example.test.response.ApiResponseFlashcard;
import com.example.test.response.ApiResponseFlashcardGroup;
import com.example.test.response.ApiResponseOneFlashcard;
import com.example.test.response.FlashcardGroupResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DictionaryActivity extends AppCompatActivity {

    EditText edtWord;
    ImageView btnFind;
    LinearLayout wordContainer;
    TextView dicBacktoExplore,tvNotfound;
    Button btnAdd;
    private FlashcardManager flashcardManager;
    private String selectedPhoneticsText = null;
    int page=1;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dictionary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtWord = findViewById(R.id.edtWord);
        btnFind = findViewById(R.id.btnFind);
        wordContainer = findViewById(R.id.WordContainer);
        dicBacktoExplore = findViewById(R.id.dicBacktoExplore);
        tvNotfound= findViewById(R.id.tvNotfound);
        flashcardManager = new FlashcardManager();

        btnFind.setOnClickListener(view -> {
            String word = edtWord.getText().toString().trim();

            if (isVietnamese(word)) {
                flashcardManager.translateToEnglish(word, new AddFlashCardApiCallback<String>() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onSuccess(String translatedWord) {
                        runOnUiThread(() -> {
                            showDefinition(translatedWord); // Tìm kiếm nghĩa tiếng Anh
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        runOnUiThread(() ->
                                Toast.makeText(DictionaryActivity.this, "Dịch lỗi: " + errorMessage, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            } else{
                showDefinition(word);
            }
        });


        dicBacktoExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showDefinition(String word) {
        //btnAdd.setVisibility(View.GONE);
        flashcardManager.fetchWordDefinition(word, new AddFlashCardApiCallback<WordData>() {
            @Override
            public void onSuccess() {

            }

            @SuppressLint("MissingInflatedId")
            @Override
            public void onSuccess(WordData wordData) {
                runOnUiThread(() -> {
                    wordContainer.removeAllViews(); // Xóa dữ liệu cũ
                    List<WordData> mergedData = FlashcardUtils.mergeWordData(Collections.singletonList(wordData));
                    Log.d("DEBUG", "Dữ liệu trước khi merge: " + new Gson().toJson(wordData));
                    Log.d("DEBUG", "Dữ liệu sau khi merge: " + new Gson().toJson(mergedData));

                    WordData mergedWordData = mergedData.get(0); // Chỉ lấy phần tử đầu tiên vì chỉ có 1 từ
                    LayoutInflater inflater = getLayoutInflater();
                    View contentView = inflater.inflate(R.layout.item_find_dictionary, wordContainer, false);

                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout phoneticContainer = contentView.findViewById(R.id.phoneticContainer);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout definitionContainer = contentView.findViewById(R.id.definitionContainer);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout partOfSpeechContainer = contentView.findViewById(R.id.partOfSpeechContainer);
                    btnAdd = contentView.findViewById(R.id.btnAdd);
                    TextView wordLabel = contentView.findViewById(R.id.wordLabel);
                    List<AppCompatButton> phoneticButtons = new ArrayList<>();
                    List<AppCompatButton> speechButtons = new ArrayList<>();
                    List<AppCompatButton> definitionButtons = new ArrayList<>();
                    wordLabel.setVisibility(View.VISIBLE);
                    wordLabel.setText(getString(R.string.word)  + wordData.getWord());
                    // Hiển thị phonetics
                    if (mergedWordData.getPhonetics() != null && !mergedWordData.getPhonetics().isEmpty()) {
                        phoneticButtons.clear();
                        for (Phonetic phonetic : mergedWordData.getPhonetics()) {
                            AppCompatButton btn = new AppCompatButton(DictionaryActivity.this);
                            btn.setText(phonetic.getText());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,  // Chiều rộng co giãn theo nội dung
                                    ViewGroup.LayoutParams.WRAP_CONTENT   // Chiều cao tự động co giãn
                            );
                            params.setMargins(8, 8, 8, 8); // (left, top, right, bottom)

                            btn.setLayoutParams(params);
                            btn.setBackgroundResource(R.drawable.btn_item_click);
                            btn.setTextColor(ContextCompat.getColor(DictionaryActivity.this, R.color.black));
                            btn.setTextSize(14);
                            btn.setAllCaps(false);
                            btn.setGravity(Gravity.CENTER); // Căn giữa văn bản
                            btn.setTag(false);
                            btn.setOnClickListener(v -> {
                                for (AppCompatButton otherBtn : phoneticButtons) {
                                    Log.d("DEBUG", "Số nút trong phoneticButtons: " + phoneticButtons.size());

                                    otherBtn.setSelected(false);
                                    otherBtn.setBackgroundResource(R.drawable.btn_item_click);
                                }
                                btn.setSelected(true);
                                btn.setBackgroundResource(R.drawable.btn_item_click);
                                selectedPhoneticsText = phonetic.getText() != null ? phonetic.getText() : "No phonetics available";
                                checkEnableAdd(phoneticButtons, definitionButtons, speechButtons, btnAdd, true);
                            });

                            phoneticButtons.add(btn);
                            phoneticContainer.addView(btn);
                        }
                    } else {
                        // Nếu không có phonetic, tạo một nút "giả"
                        AppCompatButton autoSelectedBtn = new AppCompatButton(DictionaryActivity.this);
                        autoSelectedBtn.setText("No phonetics available");
                        autoSelectedBtn.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        autoSelectedBtn.setBackgroundResource(R.drawable.btn_item_click);
                        autoSelectedBtn.setTextColor(ContextCompat.getColor(DictionaryActivity.this, R.color.black));
                        autoSelectedBtn.setTextSize(14);
                        autoSelectedBtn.setAllCaps(false);
                        autoSelectedBtn.setGravity(Gravity.CENTER);

                        // Đánh dấu là đã chọn
                        autoSelectedBtn.setSelected(true);
                        autoSelectedBtn.setTag(true); // Đánh dấu nút này hợp lệ
                        autoSelectedBtn.setVisibility(View.GONE); // Ẩn nút nhưng vẫn giữ trong danh sách xử lý

                        // Thêm vào danh sách phoneticButtons để tránh lỗi null
                        phoneticButtons.add(autoSelectedBtn);
                        phoneticContainer.addView(autoSelectedBtn);

                        // Thêm dòng text thông báo
                        AppCompatTextView noPhoneticText = new AppCompatTextView(DictionaryActivity.this);
                        noPhoneticText.setText("No phonetics available");
                        noPhoneticText.setTextColor(ContextCompat.getColor(DictionaryActivity.this, R.color.black));
                        noPhoneticText.setTextSize(14);
                        noPhoneticText.setGravity(Gravity.CENTER);

                        phoneticContainer.addView(noPhoneticText);
                        selectedPhoneticsText = "No phonetics";
                        checkEnableAdd(phoneticButtons, definitionButtons, speechButtons, btnAdd, false);
                    }

                    // Hiển thị Part of Speech
                    if (mergedWordData.getMeanings() != null && !mergedWordData.getMeanings().isEmpty()) {
                        speechButtons.clear();
                        Log.d("DEBUG", "Số meanings: " + mergedWordData.getMeanings().size());
                        for (int i = 0; i < mergedWordData.getMeanings().size(); i++) {
                            Meaning meaning = mergedWordData.getMeanings().get(i);
                            Log.d("DEBUG", "Thêm nút Part of Speech: " + meaning.getPartOfSpeech());
                            if (meaning.getPartOfSpeech() != null && !meaning.getPartOfSpeech().trim().isEmpty()) {

                                AppCompatButton btn = new AppCompatButton(DictionaryActivity.this);
                                btn.setText(meaning.getPartOfSpeech());
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,  // Chiều rộng co giãn theo nội dung
                                        ViewGroup.LayoutParams.WRAP_CONTENT   // Chiều cao tự động co giãn
                                );
                                params.setMargins(8, 8, 8, 8); // (left, top, right, bottom)
                                btn.setLayoutParams(params);
                                btn.setBackgroundResource(R.drawable.btn_item_click);
                                btn.setTextColor(ContextCompat.getColor(DictionaryActivity.this, R.color.black));
                                btn.setTextSize(14);
                                btn.setAllCaps(false);
                                btn.setTag(false);
                                btn.setGravity(Gravity.CENTER); // Căn giữa văn bản

                                // Sự kiện click cho button Part of Speech
                                btn.setOnClickListener(v -> {
                                    for (AppCompatButton otherBtn : speechButtons) {
                                        Log.d("DEBUG", "Số nút trong phoneticButtons: " + speechButtons.size());

                                        otherBtn.setSelected(false);
                                        otherBtn.setBackgroundResource(R.drawable.btn_item_click);
                                    }
                                    btn.setSelected(true);
                                    btn.setBackgroundResource(R.drawable.btn_item_click);
                                    checkEnableAdd(phoneticButtons, definitionButtons, speechButtons, btnAdd, true);
                                    // Hiển thị definitions cho part of speech đã chọn
                                    updateDefinitions(definitionContainer, meaning, contentView,
                                            phoneticButtons, definitionButtons, speechButtons);
                                });
                                speechButtons.add(btn);
                                partOfSpeechContainer.addView(btn);
                            }
                        }

                        // Hiển thị definitions cho part of speech đầu tiên
                        if (!mergedWordData.getMeanings().isEmpty()) {
                            updateDefinitions(definitionContainer, mergedWordData.getMeanings().get(0), contentView,
                                    phoneticButtons, definitionButtons, speechButtons);
                        }
                    }

                    wordContainer.addView(contentView); // Hiển thị toàn bộ layout thay vì dialog
                    btnAdd.setOnClickListener(v -> {
                        String wordfl = word.trim();
                        int partOfSpeechIndex = getSelectedIndex(speechButtons); // Chỉ mục loại từ
                        List<Integer> definitionIndices = getSelectedDefinitionIndices(definitionButtons); // Danh sách định nghĩa

                        if (wordfl.isEmpty()) {
                            Toast.makeText(DictionaryActivity.this, "Vui lòng nhập từ vựng!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int userId = Integer.parseInt(SharedPreferencesManager.getInstance(getApplicationContext()).getID());
                        Log.d("DEBUG", "wordflash: " + wordfl);
                        Log.d("DEBUG", "speech: " + partOfSpeechIndex);
                        Log.d("DEBUG", "definition: " + definitionIndices);
                        Log.d("DEBUG", "userid: " + userId);

                        flashcardManager.createFlashcard(getApplicationContext(), wordfl, definitionIndices, partOfSpeechIndex, userId, new AddFlashCardApiCallback<String>() {
                            @Override
                            public void onSuccess(String flashcardId) { // Lấy ID của flashcard vừa tạo
                                runOnUiThread(() -> {
                                if (flashcardId == null) {
                                    runOnUiThread(() -> Toast.makeText(DictionaryActivity.this, "Lỗi tạo flashcard!", Toast.LENGTH_SHORT).show());
                                    return;
                                }
                                Log.d("API_REQUEST", "Creating flashcard - word: " + wordfl + ", definitions: " + definitionIndices + ", partOfSpeech: " + partOfSpeechIndex);
                                Log.d("DEBUG", "Flashcard created with ID: " + flashcardId);
                                List<FlashcardGroup> allGroups = new ArrayList<>();
                                fetchFlashcardGroupNames(userId, 1, allGroups, Integer.parseInt(flashcardId));
                                });
                            }

                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                runOnUiThread(() -> {
                                    Log.e("DEBUG", "API Error: " + errorMessage);
                                    Toast.makeText(DictionaryActivity.this, "Lỗi tạo flashcard: " + errorMessage, Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    });

                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    //Toast.makeText(DictionaryActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    tvNotfound.setVisibility(View.VISIBLE);
                    tvNotfound.setText("Not found the word '" + word + "'.");
                });
            }
        });
    }

    private void updateDefinitions(LinearLayout definitionContainer, Meaning meaning, View dialogView,
                                   List<AppCompatButton> phoneticButtons, List<AppCompatButton> definitionButtons,
                                   List<AppCompatButton> speechButtons) {

        definitionContainer.removeAllViews();
        definitionButtons.clear();
        NestedScrollView definitionScrollView = dialogView.findViewById(R.id.definitionScrollView);

        int numberOfButtons = 0;
        if (meaning.getDefinitions() != null) {
            numberOfButtons += meaning.getDefinitions().size();
        }

        int buttonHeight = (int) getResources().getDimension(R.dimen.button_height);
        int scrollViewHeight = buttonHeight * Math.min(numberOfButtons, 3);

        definitionScrollView.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, scrollViewHeight));

        if (meaning.getDefinitions() != null) {
            Log.d("DEBUG", "Số lượng definitions: " + meaning.getDefinitions().size());
            for (Definition def : meaning.getDefinitions()) {
                Log.d("DEBUG", "Definition: " + def.getDefinition());
            }
        }
        // Hiển thị definitions cho part of speech đã chọn
        if (meaning.getDefinitions() != null && !meaning.getDefinitions().isEmpty()) {
            for (Definition definition : meaning.getDefinitions()) {
                AppCompatButton btn = new AppCompatButton(DictionaryActivity.this);
                btn.setText(definition.getDefinition());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,  // Chiều rộng co giãn theo nội dung
                        ViewGroup.LayoutParams.WRAP_CONTENT   // Chiều cao tự động co giãn
                );
                params.setMargins(8, 5, 0, 0); // (left, top, right, bottom)

                btn.setLayoutParams(params);
                btn.setBackgroundResource(R.drawable.btn_item_def_click);
                btn.setTextColor(ContextCompat.getColor(DictionaryActivity.this, R.color.black));
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                btn.setPadding(40, 10, 40, 10);
                btn.setAllCaps(false);
                btn.setTag(false);
                btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

                btn.setOnClickListener(v -> {
                    Log.d("DEBUG", "Selected definition: " + definition.getDefinition());

                    // Gọi API để dịch nghĩa
                    try {
                        flashcardManager.translateDefinition(definition.getDefinition(),
                                new AddFlashCardApiCallback<String>() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onSuccess(String vietnameseMeaning) {
                                        // Cập nhật UI trong luồng chính
                                        runOnUiThread(() -> {
                                            // Hiển thị nghĩa tiếng Việt
                                            TextView vietnameseMeaningTextView = dialogView
                                                    .findViewById(R.id.vietnameseMeaningTextView);
                                            vietnameseMeaningTextView.setText(vietnameseMeaning);
                                        });
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(DictionaryActivity.this, "Error: " + errorMessage,
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                        });
                                    }
                                });
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    Log.d("DEBUG", "Số nút trong definitionButtons trước khi cập nhật: " + definitionButtons.size());
                    for (AppCompatButton otherBtn : definitionButtons) {
                        Log.d("DEBUG", "Số nút trong definitionButtons: " + definitionButtons.size());

                        otherBtn.setSelected(false);
                        otherBtn.setBackgroundResource(R.drawable.btn_item_def_click);
                    }
                    btn.setSelected(true);
                    btn.setBackgroundResource(R.drawable.btn_item_def_click);
                    checkEnableAdd(phoneticButtons, definitionButtons, speechButtons, btnAdd, true);
                });
                definitionButtons.add(btn);
                definitionContainer.addView(btn);
            }
            // Log kiểm tra sau khi thêm nút
            Log.d("DEBUG", "Tổng số definitionButtons sau khi thêm: " + definitionButtons.size());
            for (AppCompatButton btn : definitionButtons) {
                Log.d("DEBUG", "Button text: " + btn.getText().toString());
            }
        } else {
            definitionContainer.addView(new androidx.appcompat.widget.AppCompatTextView(DictionaryActivity.this) {
                {
                    setText("No definitions available");
                }
            });
        }
    }


    private void checkEnableAdd(List<AppCompatButton> phoneticButtons,
                                List<AppCompatButton> speechButtons,
                                List<AppCompatButton> definitionButtons,
                                Button btnAdd, boolean hasPhonetics) {
        boolean isPhoneticSelected = false;
        boolean isSpeechSelected = false;
        boolean isDefinitionSelected = false;

        // Kiểm tra xem có ít nhất một nút được chọn trong mỗi nhóm không
        if (hasPhonetics) {
            for (AppCompatButton btn : phoneticButtons) {
                if (btn.isSelected()) {
                    isPhoneticSelected = true;
                    break;
                }
            }
        }
        for (AppCompatButton btn : speechButtons) {
            if (btn.isSelected()) {
                isSpeechSelected = true;
                break;
            }
        }
        for (AppCompatButton btn : definitionButtons) {
            if (btn.isSelected()) {
                isDefinitionSelected = true;
                break;
            }
        }

        // Nếu cả 3 nhóm đều có nút được chọn, kích hoạt nút Done
        if (isPhoneticSelected && isSpeechSelected && isDefinitionSelected) {
            btnAdd.setVisibility(View.VISIBLE);
        } else {
            btnAdd.setVisibility(View.GONE);
        }
    }

    private boolean isVietnamese(String text) {
        return text.matches(".*[aàáảãạăắằẳẵặâấầẩẫậeèéẻẽẹêếềểễệiìíỉĩịoòóỏõọôốồổỗộơớờởỡợuùúủũụưứừửữựyỳýỷỹỵđ].*");
    }


    private void showGroupSelectionDialog(List<FlashcardGroup> groups, int flashcardId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn nhóm flashcard:");

        String[] items = new String[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            items[i] = groups.get(i).getName();
        }

        builder.setItems(items, (dialog, which) -> {
            FlashcardGroup selectedGroup = groups.get(which);
            int groupId = selectedGroup.getId(); // Lấy ID nhóm

            // Hiển thị dialog loading tùy chỉnh
            Dialog loadingDialog = new Dialog(this);
            loadingDialog.setContentView(R.layout.custom_toast_success);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            Window window = loadingDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000"))); // Lớp phủ đen mờ
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            // Tìm các view trong dialog
            ProgressBar progressBar = loadingDialog.findViewById(R.id.progressBar);
            ImageView tickIcon = loadingDialog.findViewById(R.id.tickIcon);

            // Bắt đầu animation cho ProgressBar
            ObjectAnimator rotation = ObjectAnimator.ofFloat(progressBar, "rotation", 0f, 360f);
            rotation.setDuration(2000); // 1 giây quay hết 1 vòng
            rotation.setInterpolator(new LinearInterpolator()); // Quay đều
            rotation.start();

            loadingDialog.show();

            // Giả lập hoặc thực hiện thêm flashcard vào nhóm
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                addFlashcardToGroup(flashcardId, groupId); // Thêm flashcard vào nhóm

                // Tắt ProgressBar, làm rõ dấu tick
                progressBar.setVisibility(View.GONE);
                tickIcon.setAlpha(1f); // Dấu tick rõ ràng

                // Tự động đóng dialog sau 1 giây nữa
                new Handler(Looper.getMainLooper()).postDelayed(loadingDialog::dismiss, 1500);
            }, 2000); // Thời gian quay 1 vòng
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    //    private void fetchAllFlashcardGroups() {
//        int userId = Integer.parseInt(SharedPreferencesManager.getInstance(getApplicationContext()).getID());
//        List<FlashcardGroup> allGroups = new ArrayList<>();
//        fetchFlashcardGroupNames(userId, 1, 4, allGroups);
//    }
    private void fetchFlashcardGroupNames(int userId, int page, List<FlashcardGroup> allGroups, int flashcardId) {
        //userId = Integer.parseInt(SharedPreferencesManager.getInstance(getApplicationContext()).getID());
        Log.d("API_CALL", "Fetching groups: userId=" + userId + ", page=" + page); // Kiểm tra API có bị gọi sai không

        Log.d("DEBUG", "UserID khi gọi API: " + userId);
        flashcardManager.fetchFlashcardGroups(getApplicationContext(), userId, page, 4, new FlashcardApiCallback() {
            @Override
            public void onSuccess(Object response) {

            }

            @Override
            public void onSuccess(ApiResponseFlashcardGroup response) {

            }

            @Override
            public void onSuccess(FlashcardGroupResponse response) {
                if (response == null || response.getData() == null || response.getData().getContent() == null) {
                    Log.e("API_ERROR", "Response is null or invalid");
                    return;
                }

                List<FlashcardGroup> groups = response.getData().getContent();
                allGroups.addAll(groups);
                Log.d("API_RESPONSE", "Page " + page + " returned " + groups.size() + " groups");

                if (groups.size() == 4) {
                    fetchFlashcardGroupNames(userId, page + 1, allGroups, flashcardId); // Gọi trang tiếp theo
                } else {
                    Log.d("API_RESPONSE", "Fetched all groups: " + allGroups.size());
                    runOnUiThread(() -> showGroupSelectionDialog(allGroups, flashcardId));
                }
            }

            @Override
            public void onSuccess(ApiResponseFlashcard response) {

            }

            @Override
            public void onSuccess(ApiResponseOneFlashcard response) {

            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e("API_ERROR", "Lỗi khi lấy danh sách nhóm: " + errorMessage);
                    Toast.makeText(DictionaryActivity.this, "Lỗi lấy nhóm: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private int getSelectedIndex(List<AppCompatButton> buttons) {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).isSelected()) {
                return i;
            }
        }
        return -1; // Không có nút nào được chọn
    }
    private List<Integer> getSelectedDefinitionIndices(List<AppCompatButton> buttons) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).isSelected()) {
                indices.add(i);
            }
        }
        return indices;
    }
    private void addFlashcardToGroup(int flashcardId, int groupId) {
        flashcardManager.addFlashcardToGroup(flashcardId, groupId, new AddFlashCardApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    //Toast.makeText(DictionaryActivity.this, "Thêm flashcard vào nhóm thành công!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e("DEBUG", "API Error: " + errorMessage);
                    Toast.makeText(DictionaryActivity.this, "Lỗi thêm vào nhóm: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}