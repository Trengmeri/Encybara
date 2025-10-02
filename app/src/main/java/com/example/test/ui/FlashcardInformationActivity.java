package com.example.test.ui;

import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.animation.AnimatorInflater;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Toast;
import android.util.Log;
import android.media.MediaPlayer;
import android.media.AudioAttributes;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.example.test.R;
import com.example.test.api.AddFlashCardApiCallback;
import com.example.test.api.AnimationEndCallback;
import com.example.test.response.ApiResponseFlashcard;
import com.example.test.api.FlashcardManager;
import com.example.test.api.FlashcardApiCallback;
import com.example.test.response.ApiResponseFlashcardGroup;
import com.example.test.response.ApiResponseOneFlashcard;
import com.example.test.response.FlashcardGroupResponse;
import com.example.test.model.Flashcard;

public class FlashcardInformationActivity extends AppCompatActivity {
    private boolean isFrontVisible = true;
    private View frontSide, backSide;
    private TextView tvDefinition;
    private AnimatorSet flipIn, flipOut;
    private ImageView btnX;
    private TextView tvAddedDate, tvWord, tvPronunciation, tvExamples, txtNumRed, txtNumGreen;
    private AppCompatButton btnDefinition;
    private AppCompatButton btnExample;
    private ImageView btnSound;
    private MediaPlayer mediaPlayer;
    FrameLayout flashcardContainer;
    LinearLayout tvBackContent;
    private int countRed = 0;  // Đếm số lần vuốt phải
    private int countGreen = 0; // Đếm số lần vuốt trái
    private float x1, x2;
    private static final int SWIPE_THRESHOLD = 150; // Ngưỡng vuốt tối thiểu
    private List<Flashcard> flashcardList;
    private Flashcard selectedFlashcard;
    private int flashcardIndex ; // Bắt đầu từ flashcard đầu tiên
    FlashcardManager flashcardManager = new FlashcardManager();
    private int currentPage;
    private int totalPages;
    private int groupId;
    boolean isLastFlashcardSwiped;
    private boolean isAnimating = false; // 🔹 Thêm biến này
    @SuppressLint({"ResourceType", "MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashcard_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeViews();
        setupAnimations();
        setupClickListeners();
        // Lấy ID flashcard từ Intent
        // Nhận danh sách flashcards từ Intent
        flashcardList = getIntent().getParcelableArrayListExtra("FLASHCARD_LIST");
        // Nhận chỉ mục flashcard từ Intent, mặc định là 0 nếu không có
        flashcardIndex = getIntent().getIntExtra("FLASHCARD_INDEX", 0);
        int flashcardId = getIntent().getIntExtra("FLASHCARD_ID", -1);
        currentPage = getIntent().getIntExtra("CURRENT_PAGE", 1);
        totalPages = getIntent().getIntExtra("TOTAL_PAGES", 1);
        groupId = getIntent().getIntExtra("GROUP_ID", -1);
        if (flashcardId != -1) {
            Log.d("FlashcardInfo", "Starting to fetch flashcard with ID: " + flashcardId);
            fetchFlashcardData(flashcardId);
        } else {
            Log.e("FlashcardInfo", "Invalid flashcard ID");
            Toast.makeText(this, "Không tìm thấy flashcard", Toast.LENGTH_SHORT).show();
        }

        flashcardContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX(); // Lưu tọa độ bắt đầu
                        return true;

                    case MotionEvent.ACTION_UP:
                        x2 = event.getX(); // Lưu tọa độ kết thúc
                        float deltaX = x2 - x1;

                        if (Math.abs(deltaX) > SWIPE_THRESHOLD) { // Kiểm tra khoảng cách vuốt
                            if (selectedFlashcard == null) {
                                Log.e("FlashcardSwipe", "Error: selectedFlashcard is null");
                                return true; // Dừng lại nếu không có flashcard nào được chọn
                            }
                            //int flashcardID = selectedFlashcard.getId();
                            if (flashcardIndex == flashcardList.size() - 1) {
                                // Nếu đây là flashcard cuối cùng và đã vuốt một lần thì chặn vuốt tiếp
                                if (isLastFlashcardSwiped) {
                                    Log.d("FlashcardSwipe", "Không thể vuốt nữa, đã đến flashcard cuối cùng.");
                                    return true;
                                }
                                isLastFlashcardSwiped = true; // Đánh dấu đã vuốt flashcard cuối cùng
                            }
                            if (deltaX > 0) {
                                // Vuốt sang phải (Đánh dấu đã học)
                                flashcardManager.markFlashcardAsLearned(getApplicationContext(), selectedFlashcard.getId(), new FlashcardApiCallback() {
                                    @Override
                                    public void onSuccess(Object response) {
                                        runOnUiThread(() -> {
                                        Log.d("FlashcardSwipe", "Marked as learned: " + selectedFlashcard.getId());

                                            // Chỉ đổi flashcard sau khi API hoàn tất
                                        animateSwipe(flashcardContainer, 400, true, new AnimationEndCallback() {
                                            @Override
                                            public void onAnimationEnd() {
                                                countGreen++;
                                                txtNumGreen.setText(String.valueOf(countGreen));

                                            }
                                        });
                                        });
                                    }

                                    @Override
                                    public void onSuccess(ApiResponseFlashcardGroup response) {

                                    }

                                    @Override
                                    public void onSuccess(FlashcardGroupResponse response) {

                                    }

                                    @Override
                                    public void onSuccess(ApiResponseFlashcard response) {

                                    }

                                    @Override
                                    public void onSuccess(ApiResponseOneFlashcard response) {

                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Log.e("FlashcardSwipe", "Failed to mark as learned: " + errorMessage);
                                    }
                                });

                            } else {
                                // Vuốt sang trái (Đánh dấu chưa học)
                                flashcardManager.markFlashcardAsUnlearned(getApplicationContext(), selectedFlashcard.getId(), new FlashcardApiCallback() {
                                    @Override
                                    public void onSuccess(Object response) {
                                        runOnUiThread(() -> {
                                        Log.d("FlashcardSwipe", "Marked as unlearned: " + selectedFlashcard.getId());

                                            // Chỉ đổi flashcard sau khi API hoàn tất
                                        animateSwipe(flashcardContainer, 400, false, new AnimationEndCallback() {
                                            @Override
                                            public void onAnimationEnd() {
                                                countRed++;
                                                txtNumRed.setText(String.valueOf(countRed));

                                            }
                                        });
                                        });
                                    }

                                    @Override
                                    public void onSuccess(ApiResponseFlashcardGroup response) {

                                    }

                                    @Override
                                    public void onSuccess(FlashcardGroupResponse response) {

                                    }

                                    @Override
                                    public void onSuccess(ApiResponseFlashcard response) {

                                    }

                                    @Override
                                    public void onSuccess(ApiResponseOneFlashcard response) {

                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Log.e("FlashcardSwipe", "Failed to mark as unlearned: " + errorMessage);
                                    }
                                });

                            }
                        }
                        return true;
                }
                return false;
            }
        });
        mediaPlayer = new MediaPlayer();
    }

    private void initializeViews() {
        frontSide = findViewById(R.id.frontSide);
        backSide = findViewById(R.id.backSide);
        tvDefinition = findViewById(R.id.tvDefinition);
        btnDefinition = findViewById(R.id.btnDefinition);
        //btnExample = findViewById(R.id.btnExample);
        btnX = findViewById(R.id.btnX);
        tvAddedDate = findViewById(R.id.tvAddedDate);
        tvExamples = findViewById(R.id.tvExamples);
        tvWord = findViewById(R.id.tvWord);
        tvPronunciation = findViewById(R.id.tvPronunciation);
        btnSound = findViewById(R.id.btnAudio);
        txtNumGreen= findViewById(R.id.txtNumGreen);
        txtNumRed= findViewById(R.id.txtNumRed);
        flashcardContainer= findViewById(R.id.flashcardContainer);
        tvBackContent= findViewById(R.id.tvBackContent);
    }

    private void setupAnimations() {
        flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_in);
        flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_out);
    }

    private void setupClickListeners() {
        btnX.setOnClickListener(view -> {
            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_down);
        });

    }

    private void fetchFlashcardData(int flashcardId) {
        Log.d("FlashcardInfo", "Fetching flashcard with ID: " + flashcardId);


        flashcardManager.fetchFlashcardById(flashcardId, new FlashcardApiCallback() {
            @Override
            public void onSuccess(ApiResponseOneFlashcard response) {
                Log.d("FlashcardInfo", "API call successful");
                if (response != null && response.getData() != null) {
                    //Flashcard flashcard = response.getData();
                    selectedFlashcard = response.getData();
                    Log.d("FlashcardInfo", "Received flashcard: " + selectedFlashcard.toString());
                    runOnUiThread(() -> {
                            updateUI(selectedFlashcard);
                    });
                } else {
                    Log.e("FlashcardInfo", "Response or data is null");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("FlashcardInfo", "API call failed: " + errorMessage);
                runOnUiThread(() -> {
                    Toast.makeText(FlashcardInformationActivity.this,
                            "Lỗi khi lấy dữ liệu: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onSuccess(Object response) {

            }

            // Implement remaining callback methods
            @Override
            public void onSuccess(ApiResponseFlashcardGroup response) {
            }

            @Override
            public void onSuccess(FlashcardGroupResponse response) {
            }

            @Override
            public void onSuccess(ApiResponseFlashcard response) {
            }
        });
    }

    private void updateUI(Flashcard flashcard){
        selectedFlashcard= flashcard;
        if (flashcard != null) {
            Log.d("FlashcardInfo", "Updating UI with flashcard data");
            tvWord.setText(flashcard.getWord());

//            SharedPreferences sharedPreferences = getSharedPreferences("FlashcardPrefs", Context.MODE_PRIVATE);
//            String phoneticText = sharedPreferences.getString("phoneticText" + flashcard.getWord(), "");
//            Log.d("DEBUG_PHONETIC", "Phonetic from SharedPreferences: " + phoneticText);
            SharedPreferences sharedPreferences = getSharedPreferences("FlashcardPrefs", Context.MODE_PRIVATE);
            String phoneticText = sharedPreferences.getString("phoneticText" + flashcard.getWord(), "");
            String cleanedPhonetic = getUniquePhonetic(phoneticText);
            Log.d("DEBUG_CLEANED_PHONETIC", "Cleaned phonetic: " + cleanedPhonetic);

            runOnUiThread(() -> tvPronunciation.setText(cleanedPhonetic));


            String addeddate=flashcard.getAddedDate();
            tvAddedDate.setText("Added date: " + flashcard.extractDateTimeVietnam(addeddate));

            final String definitions = flashcard.getDefinitions();
            final String examples = flashcard.getExamples();
            Log.d("DEBUG_DEFINITIONS", "Definitions: " + definitions);
            Log.d("DEBUG_EXAMPLES", "Examples: " + examples);
            // Thiết lập sự kiện click cho các nút với dữ liệu từ flashcard
            TextView tvVietnameseMeaning = findViewById(R.id.tvVietnameseDef);
            if (definitions != null && !definitions.isEmpty()) {
                try {
                    flashcardManager.translateDefinition(definitions,
                            new AddFlashCardApiCallback<String>() {
                                @Override
                                public void onSuccess() {}

                                @Override
                                public void onSuccess(String vietnameseMeaning) {
                                    runOnUiThread(() -> {
                                        tvVietnameseMeaning.setText(vietnameseMeaning); // Hiển thị nghĩa tiếng Việt
                                        Log.d("FlashcardInfo", "Nghĩa tiếng Việt: " + vietnameseMeaning);
                                    });
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(FlashcardInformationActivity.this,
                                                "Lỗi khi dịch: " + errorMessage,
                                                Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                } catch (UnsupportedEncodingException e) {
                    Log.e("FlashcardInfo", "Lỗi mã hóa: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            } else {
                tvVietnameseMeaning.setText("Không có định nghĩa để dịch");
            }
            btnDefinition.setOnClickListener(v -> {
                Log.d("FlashcardInfo", "BtnDef clicked: " + definitions + ", example: " + examples);
                flipCard(definitions, examples);
            });
//            btnExample.setOnClickListener(v -> {
//                Log.d("FlashcardInfo", "Example button clicked. Content: " + examples);
//                flipCard(examples);
//            });

            // Xử lý nút phát âm thanh
            String audioUrlRaw = flashcard.getPhoneticAudio();
            if (audioUrlRaw != null && !audioUrlRaw.isEmpty()) {
                // Loại bỏ dấu chấm phẩy và khoảng trắng ở cuối URL
                final String audioUrl = audioUrlRaw.trim().replaceAll(";\\s*$", "");
                Log.d("FlashcardInfo", "Cleaned Audio URL: " + audioUrl);

                btnSound.setEnabled(true);
                btnSound.setOnClickListener(v -> {
                    //Toast.makeText(this, "Đang tải âm thanh...", Toast.LENGTH_SHORT).show();
                    playAudio(audioUrl);
                });
            } else {
                Log.w("FlashcardInfo", "No audio URL available");
                btnSound.setEnabled(false);
            }

            Log.d("FlashcardInfo", "UI update completed");
        } else {
            Log.e("FlashcardInfo", "Cannot update UI - flashcard is null");
        }
    }

    private void flipCard(String definition, String example) {
        Log.d("FlashcardInfo", "Flipping card with definition: " + definition + ", example: " + example);

        runOnUiThread(() -> {
            if (isFrontVisible) {
                // flipOut.setTarget(frontSide);
                flipIn.setTarget(backSide);
                flipOut.start();
                flipOut.addListener(new android.animation.Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        frontSide.setVisibility(View.GONE);
                        backSide.setVisibility(View.VISIBLE);
                        tvDefinition.setText("Definition: " + definition);
                        tvExamples.setText("Example: " + example);
                        flipIn.start();
                    }

                    @Override
                    public void onAnimationStart(android.animation.Animator animation) {
                    }

                    @Override
                    public void onAnimationCancel(android.animation.Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(android.animation.Animator animation) {
                    }
                });
            } else {
                flipOut.setTarget(backSide);
                flipIn.setTarget(frontSide);
                flipOut.start();
                flipOut.addListener(new android.animation.Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        backSide.setVisibility(View.GONE);
                        frontSide.setVisibility(View.VISIBLE);
                        flipIn.start();
                    }

                    @Override
                    public void onAnimationStart(android.animation.Animator animation) {
                    }

                    @Override
                    public void onAnimationCancel(android.animation.Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(android.animation.Animator animation) {
                    }
                });
            }
            isFrontVisible = !isFrontVisible;
        });
    }

    private void playAudio(String audioUrl) {
        try {
            // Reset MediaPlayer nếu đang phát
            mediaPlayer.reset();

            // Hiển thị loading indicator
            btnSound.setEnabled(false);

            Log.d("FlashcardInfo", "Starting to play audio from URL: " + audioUrl);

            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());

            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d("FlashcardInfo", "MediaPlayer prepared, starting playback");
                btnSound.setEnabled(true);
                mp.start();
                Toast.makeText(this, "Đang phát âm thanh", Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d("FlashcardInfo", "Playback completed");
                btnSound.setEnabled(true);
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                btnSound.setEnabled(true);
                String errorMessage = "Lỗi: " + what + ", " + extra;
                Toast.makeText(this, "Không thể phát âm thanh: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("FlashcardInfo", "MediaPlayer error: " + errorMessage);
                return true;
            });

        } catch (IOException e) {
            Log.e("FlashcardInfo", "Error playing audio", e);
            Toast.makeText(this, "Lỗi khi phát âm thanh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnSound.setEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
        private void animateSwipe(View view, int duration, boolean toRight, AnimationEndCallback callback) {
            if (isAnimating) return; // 🔹 tránh double swipe crash
            isAnimating = true;

            float translationX = toRight ? view.getWidth() : -view.getWidth();

            // Swipe ra ngoài (trượt + mờ dần)
            view.animate()
                    .translationX(translationX)
                    .alpha(0f)
                    .setDuration(duration)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        // Reset lại vị trí + alpha
                        view.setTranslationX(0);
                        view.setAlpha(0f);

                        // Đổi flashcard
                        showNextFlashcard();

                        // Swipe vào (fade in mượt hơn)
                        view.animate()
                                .alpha(1f)
                                .setDuration(250)
                                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                                .withEndAction(() -> {
                                    isAnimating = false; // 🔹 cho phép swipe mới
                                    if (callback != null) {
                                        callback.onAnimationEnd();
                                    }
                                })
                                .start();
                    })
                    .start();
        }


    private String getUniquePhonetic(String phoneticText) {
        if (phoneticText == null || phoneticText.isEmpty()) {
            return "No pronunciation available";
        }

        // Chia chuỗi thành từng phần bằng dấu ";"
        String[] phoneticArray = phoneticText.split(";");

        // Sử dụng Set để loại bỏ các phiên âm trùng lặp
        Set<String> uniquePhonetics = new LinkedHashSet<>();
        for (String phonetic : phoneticArray) {
            uniquePhonetics.add(phonetic.trim()); // Loại bỏ khoảng trắng thừa
        }

        // Ghép lại thành chuỗi mới, mỗi phiên âm cách nhau dấu "; "
        return String.join("; ", uniquePhonetics);
    }
    private void showNextFlashcard() {
        if (flashcardList != null && !flashcardList.isEmpty()) {
            if (flashcardIndex < flashcardList.size() - 1) {
                flashcardIndex++;
                updateUI(flashcardList.get(flashcardIndex));
            } else {
                if (currentPage < totalPages) {
                    currentPage++;
                    loadNextPage(); // Gọi API để tải trang tiếp theo
                } else {
                    Toast.makeText(this, "Bạn đã xem hết flashcard!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Không có flashcard nào để hiển thị!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadNextPage() {
        FlashcardManager flashcardManager = new FlashcardManager();
        flashcardManager.fetchFlashcardsInGroup(groupId, currentPage, 4, new FlashcardApiCallback() {
            @Override
            public void onSuccess(Object response) {

            }

            @Override
            public void onSuccess(ApiResponseFlashcardGroup response) {

            }

            @Override
            public void onSuccess(FlashcardGroupResponse response) {

            }

            @Override
            public void onSuccess(ApiResponseFlashcard response) {
                runOnUiThread(() -> {
                    if (response.getData() != null && !response.getData().getContent().isEmpty()) {
                        flashcardList.addAll(response.getData().getContent());
                        flashcardIndex++;
                        updateUI(flashcardList.get(flashcardIndex));
                    } else {
                        Toast.makeText(FlashcardInformationActivity.this, "Không có flashcard mới!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(ApiResponseOneFlashcard response) {

            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(FlashcardInformationActivity.this, "Lỗi tải flashcard: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


}