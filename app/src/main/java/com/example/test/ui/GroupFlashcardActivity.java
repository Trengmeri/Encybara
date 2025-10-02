package com.example.test.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.response.ApiResponseFlashcard;
import com.example.test.response.ApiResponseOneFlashcard;
import com.example.test.api.FlashcardApiCallback;
import com.example.test.api.FlashcardManager;
import com.example.test.model.FlashcardGroup;
import com.example.test.response.ApiResponseFlashcardGroup;
import com.example.test.response.FlashcardGroupResponse;
import java.util.List;

import java.util.ArrayList;

public class GroupFlashcardActivity extends AppCompatActivity {

    //AppCompatButton groupFlcid;
    TextView backtoExplore;
    ImageView btnaddgroup, iconEdit;
    LinearLayout groupContainer;
    private FlashcardManager flashcardManager;
    private final ArrayList<AppCompatButton> groupButtons = new ArrayList<>();
    private int currentPage = 1;
    private int totalPages;
    private int totalGroup;
    private final int pageSize = 4; // Mỗi trang hiển thị 4 nhóm flashcard
    private ImageView btnNext, btnPrevious;
    @SuppressLint({ "MissingInflatedId", "ClickableViewAccessibility" })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_flashcard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //groupFlcid = findViewById(R.id.groupFlcid);
        backtoExplore = findViewById(R.id.flBacktoExplore);
        btnaddgroup = findViewById(R.id.btnAddGroup);
        groupContainer = findViewById(R.id.groupContainer);
        iconEdit= findViewById(R.id.iconEdit);
        flashcardManager = new FlashcardManager();
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnaddgroup.setOnClickListener(view -> showAddGroupDialog());
        btnNext.setAlpha(0.5f);
        btnNext.setEnabled(false);

        btnPrevious.setAlpha(0.5f);
        btnPrevious.setEnabled(false);

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                fetchFlashcardGroups(currentPage);
            } else {
                Toast.makeText(this, "Đã đến trang cuối!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                fetchFlashcardGroups(currentPage);
            } else {
                Toast.makeText(this, "Đây là trang đầu tiên!", Toast.LENGTH_SHORT).show();
            }
        });
//        groupFlcid.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(GroupFlashcardActivity.this, FlashcardActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
        backtoExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fetchFlashcardGroups(currentPage);
    }

    private void showEditGroupDialog(TextView groupTextView, int groupId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit_group, null);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        EditText edtEditGroupName = dialogView.findViewById(R.id.edtGroupName);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button btnEdit = dialogView.findViewById(R.id.btnEdit);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        ImageView btnClose= dialogView.findViewById(R.id.btnclose);

        edtEditGroupName.setText(groupTextView.getText().toString().trim());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Bật/tắt nút "Edit" nếu có thay đổi trong EditText
        edtEditGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnEdit.setEnabled(
                        !s.toString().trim().isEmpty() && !s.toString().equals(groupTextView.getText().toString()));
                btnEdit.setAlpha(1.0f);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Sự kiện khi nhấn "Edit" (Cập nhật tên nhóm)
        btnEdit.setOnClickListener(v -> {
            String newName = edtEditGroupName.getText().toString().trim();

            // Gọi API để cập nhật tên nhóm
            flashcardManager.updateFlashcardGroup(groupId, newName, new FlashcardApiCallback() {
                @Override
                public void onSuccess(Object response) {

                }

                @Override
                public void onSuccess(ApiResponseFlashcardGroup response) {
                    runOnUiThread(() -> {
                        groupTextView.setText(newName);
                        dialog.dismiss();
                        Toast.makeText(GroupFlashcardActivity.this, "Group updated successfully", Toast.LENGTH_SHORT)
                                .show();
                    });
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
                    runOnUiThread(() -> {
                        Toast.makeText(GroupFlashcardActivity.this, "Error updating group: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        btnClose.setOnClickListener(v-> dialog.dismiss());

        // Sự kiện khi nhấn "Delete" (Xóa nhóm)
        btnDelete.setOnClickListener(v -> {
            // Gọi API để xóa nhóm
            flashcardManager.deleteFlashcardGroup(groupId, new FlashcardApiCallback() {
                @Override
                public void onSuccess(Object response) {

                }

                @Override
                public void onSuccess(ApiResponseFlashcardGroup response) {
                    runOnUiThread(() -> {
                        // Thêm hiệu ứng mờ dần khi xóa nhóm
                        groupTextView.animate()
                                .setDuration(200) // Hiệu ứng kéo dài 300ms
                                .withEndAction(() -> {
                                    groupContainer.removeView(groupTextView); // Xóa view sau hiệu ứng
                                    dialog.dismiss();
                                    Toast.makeText(GroupFlashcardActivity.this, "Group deleted successfully", Toast.LENGTH_SHORT).show();

                                    // Đợi một chút rồi tải lại danh sách nhóm
                                    new android.os.Handler().postDelayed(() -> {
                                        fetchFlashcardGroups(currentPage);
                                    }, 200); // Đợi 300ms để tránh giật
                                })
                                .start();
                    });
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
                    runOnUiThread(() -> {
                        Toast.makeText(GroupFlashcardActivity.this, "Error deleting group: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_group, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        EditText edtGroupName = dialogView.findViewById(R.id.edtGroupName);
        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Ban đầu disable nút Add
        btnAdd.setEnabled(false);
        btnAdd.setAlpha(0.5f); // Làm mờ nút Add

        // Lắng nghe sự thay đổi của EditText
        edtGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    btnAdd.setEnabled(false);
                    btnAdd.setAlpha(0.5f); // Làm mờ nút Add
                } else {
                    btnAdd.setEnabled(true);
                    btnAdd.setAlpha(1.0f); // Hiển thị rõ nút Add
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Xử lý sự kiện nút Cancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Xử lý sự kiện nút Add
        btnAdd.setOnClickListener(v -> {
            String groupName = edtGroupName.getText().toString().trim();
            if (!groupName.isEmpty()) {
                int userId = Integer.parseInt(SharedPreferencesManager.getInstance(getApplicationContext()).getID());// Thay đổi ID người dùng nếu cần
                flashcardManager.createFlashcardGroup(groupName, userId, new FlashcardApiCallback() {
                    @Override
                    public void onSuccess(Object response) {

                    }
                    @Override
                    public void onSuccess(ApiResponseFlashcardGroup response) {
                        Log.d("GroupFlashcardActivity", "onSuccess(ApiResponseFlashcardGroup) được gọi");
                        runOnUiThread(() -> { // Đảm bảo cập nhật UI trên UI thread
                            FlashcardGroup newGroup = response.getData(); // Đảm bảo rằng getData() trả về
                            addGroupButton(newGroup.getName(), newGroup.getId()); // Sử dụng tên nhóm từ phản hồi

                            int newTotalPages = (int) Math.ceil((double) totalGroup / pageSize);
                            int newPage = newTotalPages; // Flashcard mới nằm ở trang cuối

                            // Chuyển đến trang mới chứa flashcard
                            currentPage = newPage;
                            fetchFlashcardGroups(currentPage); // Cập nhật danh sách nhóm
                            dialog.dismiss(); // Đóng hộp thoại
                        });
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
                        Log.e("GroupFlashcardActivity", "Error creating group: " + errorMessage);
                        runOnUiThread(() -> {
                            Toast.makeText(GroupFlashcardActivity.this, "Error creating group: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void fetchFlashcardGroups(int page) {
        //groupContainer.removeAllViews(); // Xóa danh sách cũ
        int userId = Integer.parseInt(SharedPreferencesManager.getInstance(getApplicationContext()).getID());
        flashcardManager.fetchFlashcardGroups(this,userId, page,4, new FlashcardApiCallback() {
            @Override
            public void onSuccess(Object response) {

            }

            @Override
            public void onSuccess(ApiResponseFlashcardGroup response) {
                // Không làm gì ở đây
            }

            @Override
            public void onSuccess(FlashcardGroupResponse response) {
                runOnUiThread(() -> { // Đảm bảo cập nhật UI trên UI thread
                    // Xóa các nút cũ trước khi cập nhật danh sách
                    groupContainer.removeAllViews();
                    if ((response.getData() == null || response.getData().getContent().isEmpty()) && currentPage > 1) {
                        currentPage--; // Lùi về trang trước đó
                        fetchFlashcardGroups(currentPage); // Gọi lại để cập nhật danh sách
                        return;
                    }

                    // Kiểm tra xem có dữ liệu không
                    if (response.getData() != null && response.getData().getContent() != null) {
                        List<FlashcardGroup> groups = response.getData().getContent();
                        totalPages = response.getData().getTotalPages();
                        totalGroup= response.getData().getTotalElements();
                        for (FlashcardGroup group : groups) {
                            addGroupButton(group.getName(), group.getId());
                        }
                    }
                    updateButtonState();
                });
            }

            @Override
            public void onSuccess(ApiResponseFlashcard response) {

            }

            @Override
            public void onSuccess(ApiResponseOneFlashcard response) {

            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("GroupFlashcardActivity", "Error fetching groups: " + errorMessage);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")

    private void addGroupButton(String groupName, int groupId) {
        LinearLayout layoutFlashcards = findViewById(R.id.groupContainer);

        // Inflate item_groupflash.xml
        View groupView = LayoutInflater.from(this).inflate(R.layout.item_groupflash, layoutFlashcards, false);

        // Kiểm tra xem inflate có thành công không
        if (groupView == null) {
            Log.e("addGroupButton", "groupView is null");
            return;
        }

        // Tìm các thành phần trong item_groupflash.xml
        TextView textViewGroup = groupView.findViewById(R.id.textViewGroup);
        ImageView editIcon = groupView.findViewById(R.id.iconEdit);

        // Kiểm tra xem editIcon có null không
        if (editIcon == null) {
            Log.e("addGroupButton", "editIcon is null");
            return;
        }

        // Thiết lập dữ liệu
        textViewGroup.setText(groupName);
        groupView.setTag(groupId);

        // Sự kiện khi nhấn vào nhóm
        groupView.setOnClickListener(v -> {
            Intent intent = new Intent(GroupFlashcardActivity.this, FlashcardActivity.class);
            intent.putExtra("GROUP_ID", groupId);
            intent.putExtra("GROUP_NAME",groupName);
            startActivity(intent);
        });

        // Sự kiện khi nhấn vào biểu tượng chỉnh sửa
        editIcon.setOnClickListener(v -> {
            showEditGroupDialog(textViewGroup, groupId);
        });

        // Thêm View đã inflate vào container
        layoutFlashcards.addView(groupView);
    }
    public void updateButtonState() {
        if (totalPages > 1) {
            btnPrevious.setEnabled(currentPage > 1);
            btnPrevious.setAlpha(currentPage > 1  ? 1.0f : 0.5f);

            btnNext.setEnabled(currentPage < totalPages);
            btnNext.setAlpha(currentPage < totalPages? 1.0f : 0.5f);
        } else {
            // Nếu chỉ có 1 trang, làm mờ và vô hiệu hóa cả hai nút
            btnNext.setEnabled(false);
            btnNext.setAlpha(0.5f);

            btnPrevious.setEnabled(false);
            btnPrevious.setAlpha(0.5f);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        fetchFlashcardGroups(currentPage);
    }
}
