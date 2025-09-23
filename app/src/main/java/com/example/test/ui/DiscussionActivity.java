package com.example.test.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.adapter.DiscussionAdapter;
import com.example.test.api.ApiCallback;
import com.example.test.api.DiscussionManager;
import com.example.test.model.Course;
import com.example.test.model.Discussion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscussionActivity extends AppCompatActivity implements DiscussionAdapter.OnReplyClickListener  {

    private int currentPage = 1; // Bắt đầu từ trang 1
    private boolean isLoading = false; // Để tránh tải dữ liệu nhiều lần
    private boolean hasMoreData = true; // Để biết còn dữ liệu để tải không
    private Map<Integer, String> discussionUserMap = new HashMap<>();
    private int lessonID;
    private DiscussionManager discussionManager= new DiscussionManager(this);;
    private DiscussionAdapter discussionAdapter ;
    private int currentParentId = -1;

    LinearLayout replyContainer;
    RecyclerView rv_discussions;
    EditText editDiscussion;
    ImageView btSendDisussion;
    TextView back, txtCancelReply, txtReplyUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dicussion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Thêm vào phương thức onCreate() ngay sau super.onCreate()

        khaiBao();
        ScrollView scrollView = findViewById(R.id.scrollView);
        editDiscussion = findViewById(R.id.editDiscussion);
        editDiscussion.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.postDelayed(() -> {
                        scrollView.smoothScrollTo(0, v.getBottom());
                    }, 200);
                }
            }
        });
        // Khởi tạo adapter với danh sách rỗng ngay từ đầu
        discussionAdapter = new DiscussionAdapter(DiscussionActivity.this, new ArrayList<>(), DiscussionActivity.this);
        rv_discussions.setLayoutManager(new LinearLayoutManager(DiscussionActivity.this));
        rv_discussions.setAdapter(discussionAdapter);
        discussionUserMap = discussionAdapter.getDiscussionUserMap();


        rv_discussions.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastVisibleItemPosition() == discussionAdapter.getItemCount() - 1) {
                    fetchDiscussions(); // Gọi API tải trang tiếp theo
                }
            }
        });


        btSendDisussion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDiscussion();
            }
        });

        txtCancelReply.setOnClickListener(v -> cancelReply());


        fetchDiscussions();



        back.setOnClickListener(v -> {
            finish();
        });
        
    }
    private void khaiBao(){
        lessonID = getIntent().getIntExtra("lessonId",1);
        btSendDisussion = findViewById(R.id.btSendDiscussion);

        back = findViewById(R.id.back);
        rv_discussions = findViewById(R.id.rv_discussions);
        replyContainer = findViewById(R.id.replyContainer); // Ánh xạ replyContainer
        txtReplyUser = findViewById(R.id.txtReplyUser); // Tên người dùng được trả lời
        txtCancelReply = findViewById(R.id.txtCancelReply); // Nút hủy reply
    }
    private void fetchDiscussions() {
        if (isLoading || !hasMoreData) return; // Nếu đang tải hoặc hết dữ liệu thì không tải nữa
        isLoading = true;

        discussionManager.fetchDiscussionsByLesson(lessonID, currentPage, new ApiCallback<List<Discussion>>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(List<Discussion> discussions) {
                runOnUiThread(() -> {
                    if (discussions == null || discussions.isEmpty()) {
                        hasMoreData = false;
                        return;
                    }

                    if (discussionAdapter == null) {
                        discussionAdapter = new DiscussionAdapter(DiscussionActivity.this, discussions, DiscussionActivity.this);

                        rv_discussions.setLayoutManager(new LinearLayoutManager(DiscussionActivity.this));
                        rv_discussions.setAdapter(discussionAdapter);
                    } else {
                        discussionAdapter.addMoreDiscussions(discussions);
                    }

                    currentPage++;
                    isLoading = false;
                });
            }


            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    isLoading = false;
                    Toast.makeText(DiscussionActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void focusOnReply(int discussionId, String userName) {
        currentParentId = discussionId; // Lưu ID bài viết cha
        replyContainer.setVisibility(View.VISIBLE); // Hiển thị giao diện reply
        txtReplyUser.setText(userName); // Hiển thị tên người dùng đang trả lời
        editDiscussion.setHint("Write a reply for " + userName);
        editDiscussion.requestFocus();

    }
    private void cancelReply() {
        currentParentId = -1; // Xóa trạng thái reply
        replyContainer.setVisibility(View.GONE); // Ẩn giao diện reply
        editDiscussion.setHint("Write a discussion"); // Đổi lại hint mặc định
    }




//
    private void sendDiscussion() {
        String id = SharedPreferencesManager.getInstance(this).getID();
        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = Integer.parseInt(id);
        String discussionText = editDiscussion.getText().toString().trim();
        if (discussionText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }

        discussionManager.createDiscussion(userId, lessonID, discussionText,
                currentParentId == -1 ? null : currentParentId, new ApiCallback<Discussion>() {

                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onSuccess(Discussion result) {
                        runOnUiThread(() -> {
                            Toast.makeText(DiscussionActivity.this, "Bình luận đã gửi!", Toast.LENGTH_SHORT).show();
                            editDiscussion.setText("");


                            boolean isReplyAdded = false;

                            // Duyệt danh sách discussion để tìm bài viết cha
                            for (Discussion discussion : discussionAdapter.getDiscussions()) {
                                if (discussion.getReplies() != null) {
                                    for (Discussion reply : discussion.getReplies()) {
                                        if (reply.getId() == currentParentId) {
                                            reply.getReplies().add(result);
                                            discussionAdapter.notifyDataSetChanged();
                                            isReplyAdded = true;
                                            break;
                                        }
                                    }
                                }
                                if (discussion.getId() == currentParentId) {
                                    discussion.getReplies().add(result);
                                    discussionAdapter.notifyDataSetChanged();
                                    isReplyAdded = true;
                                    break;
                                }
                            }

                            // Nếu không tìm thấy bài viết cha, thêm vào danh sách chính
                            if (!isReplyAdded) {
                                discussionAdapter.addDiscussion(result);
                                discussionAdapter.notifyDataSetChanged();
                            }

                            cancelReply(); // Hủy chế độ reply sau khi gửi bình luận
                        });
                    }




                    @Override
                    public void onFailure(String errorMessage) {
                        runOnUiThread(() -> {
                            Toast.makeText(DiscussionActivity.this, "Gửi bình luận thất bại!", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

//    private void showKeyboard() {
//        // Đảm bảo EditText luôn hiển thị khi bàn phím xuất hiện
//        editDiscussion.post(() -> {
//            editDiscussion.requestFocus();
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            if (imm != null) {
//                imm.showSoftInput(editDiscussion, InputMethodManager.SHOW_IMPLICIT);
//            }
//
//            // Đợi một chút để bàn phím hiển thị, sau đó đảm bảo EditText hiển thị
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                // Đảm bảo EditText hiển thị hoàn toàn bằng cách yêu cầu nó hiển thị trong vùng nhìn
//                Rect rect = new Rect(0, 0, editDiscussion.getWidth(), editDiscussion.getHeight());
//                editDiscussion.requestRectangleOnScreen(rect, true);
//
//                // Cuộn RecyclerView đến vị trí cuối cùng
//                if (rv_discussions.getAdapter() != null && rv_discussions.getAdapter().getItemCount() > 0) {
//                    rv_discussions.smoothScrollToPosition(rv_discussions.getAdapter().getItemCount() - 1);
//                }
//            }, 300); // Đợi 300ms cho bàn phím hiển thị
//        });
//    }



    @Override
    public void onReplyClicked(int discussionId) {
        String ownerName = discussionUserMap.get(discussionId);
        if (ownerName != null) {
            focusOnReply(discussionId, ownerName);
        } else {
            Toast.makeText(this, "Không tìm thấy chủ nhân!", Toast.LENGTH_SHORT).show();
        }
    }
}