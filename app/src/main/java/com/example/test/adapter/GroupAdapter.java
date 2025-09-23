package com.example.test.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.model.Course;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private List<String> groupList;
    private Context context;
    private OnItemClickListener listener;

    public GroupAdapter(Context context, List<String> groupList, OnItemClickListener listener) {
        this.context = context;
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_course, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        String group = groupList.get(position);
        holder.txtGroupName.setText(group);

        // Bắt sự kiện khi click vào một nhóm
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView txtGroupName;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            txtGroupName = itemView.findViewById(R.id.txtGroupName);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String groupName);
    }

}

