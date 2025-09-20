package com.example.eduvia;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.VH> {

    private final List<Student> data = new ArrayList<>();
    private final OnItemClick onItemClick;

    public StudentAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }



    public void setItems(List<Student> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Student s = data.get(position);
        h.tvName.setText(s.getName());
        h.tvSubjects.setText(s.getSubjects());
        //
        h.tvClass.setText(String.format(Locale.getDefault(), "Class %s", s.getClassName()));
        h.tvFeeAndDate.setText(s.getStatus());
        if (s.getActiveStaus() == 1) {
            h.active_status.setText("Active");
            h.active_status.setBackgroundTintList(ColorStateList.valueOf(
                    h.itemView.getResources().getColor(R.color.tt_success)
            ));
        } else {
            h.active_status.setText("Inactive");
            h.active_status.setBackgroundTintList(ColorStateList.valueOf(
                    h.itemView.getResources().getColor(R.color.tt_danger)
            ));
        }


        h.tvFeeAndDate.setTextColor(s.getStatus().equals("Paid") ? h.itemView.getResources().getColor(R.color.tt_success) : h.itemView.getResources().getColor(R.color.tt_danger));
        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(s);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClick {
        void onClick(Student s);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvSubjects, tvClass, tvFeeAndDate,active_status;
        ImageView avatar;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvSubjects = itemView.findViewById(R.id.tvSubjects);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvFeeAndDate = itemView.findViewById(R.id.tvFeeState);
            active_status = itemView.findViewById(R.id.active_status);

        }
    }
}
