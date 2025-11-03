package in.eduvia.org;

import static in.eduvia.org.SignUp.BASE_URL;

import android.content.res.ColorStateList;
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
        String name = s.getName();
        // Capitalize each word
        String[] words = name.split(" ");
        String capitalizedName = "";
        for (String word : words) {
            capitalizedName += word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase() + " ";
        }
        h.tvName.setText(capitalizedName);
        String avatar = s.getAvatar();
        String avatarUrl = BASE_URL + avatar;
        // set profile image using glide
        if (s.getAvatar() != null) {
            Glide.with(h.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.user_profile)
                    .error(R.drawable.user_profile)
                    .into(h.avatar);
        }
        h.tvSubjects.setText(s.getSubjects());
        //
        h.tvClass.setText(String.format(Locale.getDefault(), "Class %s", s.getClassName()));

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
    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void addStudent(Student student) {
        data.add(student);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvSubjects, tvClass,active_status;
        ImageView avatar;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvSubjects = itemView.findViewById(R.id.tvSubjects);
            tvClass = itemView.findViewById(R.id.tvClass);
            active_status = itemView.findViewById(R.id.active_status);

        }
    }
}
