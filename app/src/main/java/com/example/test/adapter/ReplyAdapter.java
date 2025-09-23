package com.example.test.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.test.R;
import com.example.test.SharedPreferencesManager;
import com.example.test.api.ApiCallback;
import com.example.test.api.DiscussionManager;
import com.example.test.api.UserManager;
import com.example.test.model.Discussion;
import com.example.test.model.User;

import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ViewHolder> {
    private List<Discussion> replies;
    private Context context;
    private UserManager userManager;
    private DiscussionManager discussionManager;
    private  int currentUserID;
//    private final int currentUserID = SharedPreferencesManager.getInstance(context).getUser().getId();

    public ReplyAdapter(Context context, List<Discussion> replies, int currentUserID) {
        this.context = context;
        this.replies = replies;
        this.userManager= new UserManager(context);
        this.discussionManager= new DiscussionManager(context);
        this.currentUserID = currentUserID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reply, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Discussion reply = replies.get(position);

        int userId = reply.getUserID();
        Log.d("ReplyActivity", "UserID: "+ userId);
        userManager.fetchUserById(userId, new ApiCallback<User>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(User user) {
                String avatar = user.getAvt();
                new Handler(Looper.getMainLooper()).post(() -> {
                    holder.txtUser.setText(user.getName());
                    if (avatar == null) return;
                    String uri = avatar.replace("0.0.0.0", "14.225.198.3");
                    Log.d("DiscussionAdapter", user.getName());
                    Glide.with(context)
                            .load(uri)
                            .placeholder(R.drawable.icon_lesson) // Ảnh mặc định
                            .error(R.drawable.icon_lesson)// Ảnh lỗi
                            .circleCrop()                        // Luôn hiển thị hình tròn
                            .override(200, 200)
                            .into(holder.imgAvatar);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    holder.txtUser.setText("Không tải được tên");
                    Toast.makeText(context, "Lỗi tải tên người dùng: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
        holder.txtContent.setText(reply.getContent());

        // Kiểm tra trạng thái like
        discussionManager.isDiscussionLiked(currentUserID, reply.getId(), new ApiCallback<Boolean>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(Boolean isLiked) {
                reply.setLiked(isLiked);
                new Handler(Looper.getMainLooper()).post(() -> {
                    holder.btnLike.setSelected(isLiked);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("DiscussionAdapter", "Lỗi kiểm tra like: " + errorMessage);
            }
        });
        holder.txtLikeCount.setText(String.valueOf(reply.getNumLike()));


        // Xử lý sự kiện bấm nút Like
        holder.btnLike.setOnClickListener(v -> {
            boolean isLiked = reply.isLiked();
            int newLikeCount = isLiked ? reply.getNumLike() - 1 : reply.getNumLike() + 1;

            // Cập nhật UI ngay lập tức
            reply.setNumLike(newLikeCount);
            reply.setLiked(!isLiked);
            holder.txtLikeCount.setText(String.valueOf(newLikeCount));
            holder.btnLike.setSelected(!isLiked);

            // Gửi API like/unlike
            if (!isLiked) {
                discussionManager.likeDiscussion(currentUserID, reply.getId(), new ApiCallback<Void>() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onSuccess(Void response) {
                        Log.d("DiscussionAdapter", "Like thành công");
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        rollbackLike(holder, reply, isLiked);
                    }
                });
            } else {
                discussionManager.unlikeDiscussion(currentUserID, reply.getId(), new ApiCallback<Void>() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onSuccess(Void response) {
                        Log.d("DiscussionAdapter", "Unlike thành công");
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        rollbackLike(holder, reply, isLiked);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    private void rollbackLike(ReplyAdapter.ViewHolder holder, Discussion discussion, boolean previousState) {
        discussion.setNumLike(previousState ? discussion.getNumLike() + 1 : discussion.getNumLike() - 1);
        discussion.setLiked(previousState);

        // Đảm bảo chạy trên UI Thread
        new Handler(Looper.getMainLooper()).post(() -> {
            holder.txtLikeCount.setText(String.valueOf(discussion.getNumLike()));
            holder.btnLike.setSelected(previousState);

            // Chạy Toast trên UI Thread
            if (context != null) {
                Toast.makeText(context, "Lỗi khi like/unlike", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUser, txtContent, txtCreatedAt, txtLikeCount;
        ImageView btnLike, imgAvatar;

        public ViewHolder(View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.txtUser);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtLikeCount= itemView.findViewById(R.id.txtLikeCount);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnLike= itemView.findViewById(R.id.btnLike);
//            txtCreatedAt = itemView.findViewById(R.id.txtCreatedAt);
        }
    }
}
