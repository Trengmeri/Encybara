package com.example.test.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.R;
import com.example.test.api.FlashcardApiCallback;
import com.example.test.api.FlashcardManager;
import com.example.test.model.Flashcard;
import com.example.test.response.ApiResponseFlashcard;
import com.example.test.response.ApiResponseFlashcardGroup;
import com.example.test.response.ApiResponseOneFlashcard;
import com.example.test.response.FlashcardGroupResponse;
import com.example.test.ui.FlashcardActivity;
import com.example.test.ui.FlashcardInformationActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {
    private Context context;
    private List<Flashcard> flashcards;
    private int currentPage;
    private int totalPages;
    private int groupId;
    public FlashcardAdapter(Context context, List<Flashcard> flashcards,int currentPage, int totalPages, int groupId) {
        this.context = context;
        this.flashcards = flashcards;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.groupId = groupId;
    }
    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.wordTextView.setText(flashcard.getWord());
        String lastRv= flashcard.getLastReviewed();
        //holder.tvLastReviewed.setText("Last reviewed: "+flashcard.timeAgo(lastRv));
        String timeAgo = flashcard.timeAgo(lastRv);
        boolean tvLearnedStatus= flashcard.isLearnedStatus();
        if (timeAgo == null || timeAgo.equals("Lỗi thời gian")) {
            holder.tvLastReviewed.setText("Không có thời gian");
        } else {
            holder.tvLastReviewed.setText("Last reviewed: "+ timeAgo);
        }

        if (holder.iconRemove == null) {
            Log.e("FlashcardAdapter", "iconRemove is null at position " + position);
        } else {
            Log.d("FlashcardAdapter", "iconRemove found at position " + position);
        }
        if (tvLearnedStatus) {
            holder.tvStatus.setText("● learned");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setText("● unlearned");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        }
        // Thêm sự kiện click cho item flashcard
        holder.itemView.setOnClickListener(v -> {
            int selectedIndex = holder.getAdapterPosition();
            Intent intent = new Intent(context, FlashcardInformationActivity.class);
            intent.putParcelableArrayListExtra("FLASHCARD_LIST", new ArrayList<>(flashcards)); // Gửi danh sách flashcards
            intent.putExtra("FLASHCARD_ID", flashcard.getId()); // Gửi ID flashcard đến FlashcardInformationActivity
            intent.putExtra("FLASHCARD_INDEX", selectedIndex);
            intent.putExtra("CURRENT_PAGE", currentPage); // Truyền currentPage
            intent.putExtra("TOTAL_PAGES", totalPages); // Truyền totalPages
            intent.putExtra("GROUP_ID", groupId); // Truyền groupId để gọi API khi cần
            context.startActivity(intent); // Khởi động activity thông tin flashcard
        });
        // Sự kiện click vào iconRemove để hiển thị dialog xác nhận
        holder.iconRemove.setOnClickListener(v -> {
            Log.d("FlashcardAdapter", "Clicked on remove icon at position " + position);
            showRemoveDialog(position);
        });
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    public static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView;
        TextView tvLastReviewed;
        TextView tvStatus;
        ImageView iconRemove;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView);
            tvLastReviewed = itemView.findViewById(R.id.tvLastReviewed);
            iconRemove = itemView.findViewById(R.id.iconRemove);
            tvStatus= itemView.findViewById(R.id.tvlearned);
        }
    }
    private void showRemoveDialog(int position) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.dialog_remove_flash); // Tạo file XML cho dialog
        Button btnCancel = bottomSheetDialog.findViewById(R.id.btnCancel);
        Button btnRemove = bottomSheetDialog.findViewById(R.id.btnRemove);
        TextView tvNameFlash=bottomSheetDialog.findViewById(R.id.tvNameFlash);
        TextView tvRemove= bottomSheetDialog.findViewById(R.id.tvRemove);

        Flashcard flashcard = flashcards.get(position);

        String groupName = ((FlashcardActivity) context).getIntent().getStringExtra("GROUP_NAME");
        tvRemove.setText("Remove from " + groupName+ " ?");

        tvNameFlash.setText(flashcard.getWord());
        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        FlashcardManager flashcardManager = new FlashcardManager();
        btnRemove.setOnClickListener(v -> {
            int flashcardId = flashcards.get(position).getId(); // Lấy ID của flashcard cần xóa
            flashcardManager.deleteFlashcardById(flashcardId, new FlashcardApiCallback() {
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
                }

                @Override
                public void onSuccess(ApiResponseOneFlashcard response) {

                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("FlashcardAdapter", "Failed to delete flashcard: " + errorMessage);
                }
            });
            flashcards.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, flashcards.size()); // Cập nhật lại các vị trí item còn lại
            Log.d("FlashcardAdapter", "Flashcard deleted successfully ");

            // Nếu xoá hết flashcard của trang hiện tại, lùi về trang trước đó
            if (flashcards.isEmpty() && currentPage > 1) {
                currentPage--;
            }
            new android.os.Handler().postDelayed(() -> {
                ((FlashcardActivity) context).fetchFlashcards(groupId, currentPage);
                ((FlashcardActivity) context).updateButtonState(); // Cập nhật nút phân trang
            }, 300); // Độ trễ 300ms

            bottomSheetDialog.dismiss(); // Đóng dialog
        });
        bottomSheetDialog.show();
    }

}