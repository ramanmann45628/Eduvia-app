package in.eduvia.org;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.VH> {

    private final List<AnnouncementModel> data;
    private final Context context;
    private final OnDeleteClickListener deleteClickListener;

    public AnnouncementAdapter(Context context, List<AnnouncementModel> data, OnDeleteClickListener listener) {
        this.context = context;
        this.data = data;
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_annoucement, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AnnouncementModel ann = data.get(position);

        holder.tvTitle.setText(ann.getTitle().toUpperCase(Locale.ROOT));
        holder.tvMessage.setText(ann.getMessage());
        holder.tvStartDateTime.setText("Start: " + ann.getStartDate() + " \n" + ann.getStartTime());
        holder.tvExpiryDateTime.setText("Expiry: " + ann.getExpiryDate() + "\n " + ann.getExpiryTime());

        holder.ivDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvClass, tvStartDateTime, tvExpiryDateTime, tvSendTo;
        ImageView ivDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvStartDateTime = itemView.findViewById(R.id.tvStartDateTime);
            tvExpiryDateTime = itemView.findViewById(R.id.tvExpiryDateTime);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
}
