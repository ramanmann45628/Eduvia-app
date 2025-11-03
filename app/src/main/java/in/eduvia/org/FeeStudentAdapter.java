package in.eduvia.org;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeeStudentAdapter extends RecyclerView.Adapter<FeeStudentAdapter.VH> {
    public List<StudentFeeModel> feeStudentList;

    public FeeStudentAdapter(List<StudentFeeModel> feeStudentList) {
        this.feeStudentList = feeStudentList;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fee_student, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        StudentFeeModel model = feeStudentList.get(position);
        holder.Studentname.setText(model.getStudentName());
        // set capitalize name each word
        String name = model.getStudentName();
        String[] names = name.split(" ");
        String capitalizeName = "";
        for (String s : names) {
            String first = s.substring(0, 1);
            String afterfirst = s.substring(1);
            capitalizeName += first.toUpperCase() + afterfirst + " ";
        }
        holder.Studentname.setText(capitalizeName);
        holder.TotalAmount.setText("Total Fees : ₹" + model.getTotalAmount());
        holder.profileImage.setImageResource(R.drawable.user_profile);
        holder.FeeStatus.setText(model.getFeeStatus().toUpperCase());
        if (model.getFeeStatus().equals("paid")) {
            holder.FeeStatus.setTextColor(Color.GREEN);
        } else if (model.getFeeStatus().equals("pending")) {
            holder.FeeStatus.setTextColor(Color.MAGENTA);
        } else {
            holder.FeeStatus.setTextColor(Color.RED);
        }
        holder.paidAmount.setText("Paid Amount : " + model.getPaidAmount());
        holder.pendingAmount.setText("Pending Amount : " + model.getPendingAmount());
    }

    @Override
    public int getItemCount() {
        return feeStudentList.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView Studentname, TotalAmount, FeeStatus, paidAmount, pendingAmount;
        ImageView profileImage;

        VH(@NonNull View itemView) {
            super(itemView);
            Studentname = itemView.findViewById(R.id.tvStudentName);
            TotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            FeeStatus = itemView.findViewById(R.id.tvFeeStatus);
            paidAmount = itemView.findViewById(R.id.tvpaidAmount);
            pendingAmount = itemView.findViewById(R.id.tvpendingAmount);
            profileImage = itemView.findViewById(R.id.imgStudent);
        }
    }
}
