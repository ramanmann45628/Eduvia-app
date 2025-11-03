package in.eduvia.org;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.VH> {
    private final List<Subject> data = new ArrayList<>();
    private OnSubjectClickListener listener; // interface instance

    // Pass listener from fragment/activity
    public SubjectAdapter(OnSubjectClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Subject s = data.get(position);
        holder.tvSubjectName.setText(s.getTvSubjectName().trim().toUpperCase());
        holder.tvClassRangeValue.setText(s.getTvClassRangeValue());
        holder.tvChargesValue.setText(s.getTvChargesValue()+ " Rs");

        // Set click listeners for edit & delete
        holder.ibEdit.setOnClickListener(v -> listener.onEditClick(s));
        holder.ibDelete.setOnClickListener(v -> listener.onDeleteClick(s));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // method to update data
    public void setData(List<Subject> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvClassRangeValue, tvChargesValue;
        ImageButton ibEdit,ibDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvClassRangeValue = itemView.findViewById(R.id.tvClassRangeValue);
            tvChargesValue = itemView.findViewById(R.id.tvChargesValue);
            ibEdit = itemView.findViewById(R.id.ibEdit);
            ibDelete = itemView.findViewById(R.id.ibDelete);
        }
    }
    public interface OnSubjectClickListener {
        void onEditClick(Subject subject);
        void onDeleteClick(Subject subject);
    }
}
