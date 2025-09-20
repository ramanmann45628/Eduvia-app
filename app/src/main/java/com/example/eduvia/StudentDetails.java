package com.example.eduvia;

import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StudentDetails extends Fragment {

    // UI Elements
    private TextView tvName, tvEmail, tvClass, tvPhone, tvDob, tvFeeStatus,
            tvStatususer, tvSubjects, tvGender, tvParentName,
            tvParentContact, tvRegDate, tv_total_fee,tvEdit;

    private Button btnBack, btnDelete;
    private SwitchMaterial switchStatus;

    // Vars
    private String stId = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student_details, container, false);

        // Initialize Views
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvClass = view.findViewById(R.id.tvClass);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvDob = view.findViewById(R.id.tvDob);
        tvFeeStatus = view.findViewById(R.id.tvFeesStatus);
        tvSubjects = view.findViewById(R.id.tvSubjects);
        tvGender = view.findViewById(R.id.tvGender);
        tvParentName = view.findViewById(R.id.tvParentName);
        tvParentContact = view.findViewById(R.id.tvParentContact);
        tvRegDate = view.findViewById(R.id.tvRegDate);
        tv_total_fee = view.findViewById(R.id.tv_total_fee);
        tvStatususer = view.findViewById(R.id.tvStatususer);
        tvEdit = view.findViewById(R.id.tvEdit);
        tvEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("student_id", stId);
            AddStudent addstudent = new AddStudent();
            addstudent.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, addstudent)
                    .addToBackStack(null)
                    .commit();
        });

        btnBack = view.findViewById(R.id.btnBack);
        btnDelete = view.findViewById(R.id.btndelete);

        switchStatus = view.findViewById(R.id.switchStatus);

        // Back Button
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Switch Listener
        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> activeStatus(isChecked));

        // Get Arguments
        Bundle args = getArguments();
        if (args != null) {
            String studentId = args.getString("student_id");
            stId = studentId;

            fetchStudentsFromServer(studentId);
            fetchFeesFromServer(studentId);

            // Delete Student
            btnDelete.setOnClickListener(v -> showDeleteDialog(studentId));
        }

        return view;
    }

    /**
     * Update Active/Inactive Status
     */
    private void activeStatus(boolean isActive) {
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");
        String status = isActive ? "1" : "0";

        String url = BASE_URL + "student.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            int statusInt = jsonObject.getInt("status");
                            switchStatus.setChecked(statusInt == 1);
                            tvStatususer.setText(statusInt == 1 ? "Active" : "Inactive");
                            Log.d("STATUS", "Student status updated: " + statusInt);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(),
                        "Network error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "active_status");
                params.put("active_status", status);
                params.put("admin_id", adminId);
                params.put("student_id", stId);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * Show Delete Confirmation Dialog
     */
    private void showDeleteDialog(String studentId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Yes", (dialog, which) -> DeleteStudent(studentId))
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    /**
     * Delete Student
     */
    private void DeleteStudent(String studentId) {
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");
        String url = BASE_URL + "student.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("DeleteStudentResponse", response);
                    Toast.makeText(getContext(), "Student deleted successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                },
                error -> Toast.makeText(getContext(),
                        "Network error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "delete_student");
                params.put("admin_id", adminId);
                params.put("student_id", studentId);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * Fetch Student Details
     */
    private void fetchStudentsFromServer(String studentId) {
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        String url = BASE_URL + "student.php?action=get_studentDetails&admin_id=" + adminId + "&student_id=" + studentId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONObject sobj = jsonObject.getJSONObject("student");

                            // Extract details
                            String name = sobj.getString("name");
                            String email = sobj.getString("email");
                            String gender = sobj.getString("gender");
                            String phone = sobj.getString("phone");
                            String dob = sobj.getString("dob");
                            String clas = sobj.getString("class");
                            String subjects = sobj.getString("subjects");
                            String feeStatus = sobj.getString("fee_status");
                            String parentName = sobj.getString("parent_name");
                            String parentContact = sobj.getString("parent_phone");
                            String regDate = sobj.getString("created_at");
                            int statusInt = sobj.getInt("status");

                            // Set UI
                            tvName.setText(name);
                            tvEmail.setText(email);
                            tvGender.setText(gender);
                            tvClass.setText(clas);
                            tvPhone.setText(phone);
                            tvDob.setText(dob);
                            tvFeeStatus.setText(feeStatus);
                            tvSubjects.setText(subjects);
                            tvParentName.setText(parentName);
                            tvParentContact.setText(parentContact);
                            tvRegDate.setText(regDate);
                            tvStatususer.setText(statusInt == 1 ? "Active" : "Inactive");

                            switchStatus.setChecked(statusInt == 1);

                            // Fee color
                            if (feeStatus.equals("Paid")) {
                                tvFeeStatus.setTextColor(getResources().getColor(R.color.tt_success));
                            } else {
                                tvFeeStatus.setTextColor(getResources().getColor(R.color.tt_danger));
                            }

                        } else {
                            Toast.makeText(getContext(),
                                    jsonObject.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(),
                        "Network error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * Fetch Fees Details
     */
    private void fetchFeesFromServer(String studentId) {
        String url = BASE_URL + "student.php?action=total_fees&student_id=" + studentId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {

                            // Get totals
                            String totalAmount = jsonObject.getString("total_amount");
                            String paidAmount = jsonObject.getString("paid_amount");
                            String pendingAmount = jsonObject.getString("pending_amount");

                            Log.d("Fees", "Total: " + totalAmount +
                                    ", Paid: " + paidAmount +
                                    ", Pending: " + pendingAmount);

                            tv_total_fee.setText("₹" + totalAmount);

                            // Fees details
                            JSONArray feesArray = jsonObject.getJSONArray("fees_details");
                            for (int i = 0; i < feesArray.length(); i++) {
                                JSONObject feeObj = feesArray.getJSONObject(i);
                                String subjectId = feeObj.getString("subject_id");
                                String amount = feeObj.getString("amount");
                                String status = feeObj.getString("status");
                                Log.d("Fees", "Subject ID: " + subjectId +
                                        ", Amount: " + amount +
                                        ", Status: " + status);
                            }
                        } else {
                            Toast.makeText(getContext(),
                                    jsonObject.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(),
                        "Network error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(request);
    }
}
