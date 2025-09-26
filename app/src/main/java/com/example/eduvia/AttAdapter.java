package com.example.eduvia;

import static com.example.eduvia.SignUp.BASE_URL;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AttAdapter extends RecyclerView.Adapter<AttAdapter.VH> {
    private final Context context;
    private final List<AttStModel> data = new ArrayList<>();
    private final OnItemClick onItemClick;

    public AttAdapter(Context context, OnItemClick onItemClick) {
        this.context = context;
        this.onItemClick = onItemClick;
    }

    public void setItems(List<AttStModel> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_student, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AttStModel s = data.get(position);
        h.tvName.setText(s.getName());

        // Avatar
        if (s.getAvatar() != null && !s.getAvatar().isEmpty() && !s.getAvatar().equals("user_profile")) {
            String avatarUrl = BASE_URL + s.getAvatar();
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.user_profile)
                    .error(R.drawable.user_profile)
                    .into(h.avatar);
        } else {
            h.avatar.setImageResource(R.drawable.user_profile);
        }

        // ----------------- Attendance status mapping -----------------
        String status = s.getStatus(); // "present", "absent", or ""
        if ("present".equalsIgnoreCase(status)) {
            h.ivPresent.setImageResource(R.drawable.circle_green);
            h.ivAbsent.setImageResource(R.drawable.circle_gray);
        } else if ("absent".equalsIgnoreCase(status)) {
            h.ivPresent.setImageResource(R.drawable.circle_gray);
            h.ivAbsent.setImageResource(R.drawable.circle_red);
        } else {
            // empty or unknown
            h.ivPresent.setImageResource(R.drawable.circle_gray);
            h.ivAbsent.setImageResource(R.drawable.circle_gray);
        }

        // ----------------- Click listeners -----------------
        h.ivPresent.setOnClickListener(v -> {
            h.ivPresent.setImageResource(R.drawable.circle_green);
            h.ivAbsent.setImageResource(R.drawable.circle_gray);
            s.setStatus("present"); // update model
            if (onItemClick != null) onItemClick.onStatusChange(s.getId(), "present");
        });

        h.ivAbsent.setOnClickListener(v -> {
            h.ivPresent.setImageResource(R.drawable.circle_gray);
            h.ivAbsent.setImageResource(R.drawable.circle_red);
            s.setStatus("absent"); // update model
            if (onItemClick != null) onItemClick.onStatusChange(s.getId(), "absent");
        });
    }


    public void updateStudentStatus(int studentId, String status) {
        for (AttStModel student : data) {
            if (student.getId() == studentId) {
                student.setStatus(status); // status = "present" or "absent"
                break;
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClick {
        void onClick(AttStModel s);
        void onStatusChange(int studentId, String status);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView avatar, ivAbsent, ivPresent;

        VH(@NonNull View itemView) {
            super(itemView);
            // ⚠️ FIX THIS: make sure in your layout, avatar = ImageView with id "ivAvatar"
            avatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            ivAbsent = itemView.findViewById(R.id.ivAbsent);
            ivPresent = itemView.findViewById(R.id.ivPresent);
        }
    }
}
