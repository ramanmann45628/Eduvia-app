package com.example.eduvia;

import static android.content.Context.MODE_PRIVATE;
import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeesFragment extends Fragment {

    SharedPreferences sp;
    String url = BASE_URL + "fee_summery.php";

    TextView tvOverall, tvTotal, tvPending, tvPaid, tvWelcome;
    Button btnShow;
    ImageButton filter;
    BottomSheetDialog bd;
    RecyclerView studentRecyclerView;
    EditText etSearch;

    private android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    FeeStudentAdapter adapter;

    // Variable to hold the real value and state
    String overallAmount = "";
    boolean isAmountVisible = false;
    private String lastSelectedStatus = "all";  // default

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fees, container, false);

        // ---- Bind views ----
        tvOverall = v.findViewById(R.id.tvAmountHidden);
        tvWelcome = v.findViewById(R.id.tvWelcome);
        btnShow = v.findViewById(R.id.btnShowAmount);
        tvTotal = v.findViewById(R.id.tvTotalFees);
        tvPending = v.findViewById(R.id.tvPendingFees);
        tvPaid = v.findViewById(R.id.tvReceivedFees);
        filter = v.findViewById(R.id.filterButton);
        etSearch = v.findViewById(R.id.searchEditText);
        studentRecyclerView = v.findViewById(R.id.studentRecyclerView);

        // ---- SharedPreferences ----
        sp = getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");
        String adminName = sp.getString("name", "");

        // ---- Set welcome text ----
        tvWelcome.setText("Welcome ! " + adminName);

        // ---- RecyclerView setup ----
        studentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FeeStudentAdapter(new ArrayList<>());
        studentRecyclerView.setAdapter(adapter);

        // ---- Default hide overall amount ----
        tvOverall.setText("₹ ******");

        Log.d("adminId", adminId);

        // ---- Load data ----
        fetchAllfeesummery(adminId);
        fetchStudents(adminId, lastSelectedStatus, "");

        // ---- Search listener (with debounce) ----
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                final String q = s.toString().trim();
                searchRunnable = () -> fetchStudents(adminId, lastSelectedStatus, q);
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });

        // ---- Toggle button (show/hide amount) ----
        btnShow.setOnClickListener(v1 -> {
            if (isAmountVisible) {
                tvOverall.setText("₹ ******");
                btnShow.setText("Show");
                isAmountVisible = false;
            } else {
                tvOverall.setText("₹ " + overallAmount);
                btnShow.setText("Hide");
                isAmountVisible = true;
            }
        });

        // ---- Filter button ----
        filter.setOnClickListener(v1 -> {
            bd = new BottomSheetDialog(getContext());
            View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottomsheet, null);
            bd.setContentView(sheetView);

            RadioGroup radioGroup = sheetView.findViewById(R.id.radioGroupFilter);
            Button btnCancel = sheetView.findViewById(R.id.btnCancel);
            Button btnApply = sheetView.findViewById(R.id.btnApply);

            // restore last selection
            if (lastSelectedStatus.equals("paid")) {
                radioGroup.check(R.id.rbPaid);
            } else if (lastSelectedStatus.equals("pending")) {
                radioGroup.check(R.id.rbPending);
            } else {
                radioGroup.check(R.id.rbAll);
            }

            btnCancel.setOnClickListener(view -> bd.dismiss());
            btnApply.setOnClickListener(view -> {
                String status = "all";
                int selectedId = radioGroup.getCheckedRadioButtonId();

                if (selectedId == R.id.rbPaid) {
                    status = "paid";
                } else if (selectedId == R.id.rbPending) {
                    status = "pending";
                }

                lastSelectedStatus = status;
                fetchStudents(sp.getString("admin_id", ""), status, etSearch.getText().toString().trim());
                bd.dismiss();
            });

            bd.show();
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchHandler.removeCallbacksAndMessages(null);
    }

    // ---- Fetch students ----
    private void fetchStudents(String adminId, String status, String query) {
        String url = BASE_URL + "fee_summery.php";

        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("StudentResponse", response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            JSONArray arr = obj.getJSONArray("students");
                            List<StudentFeeModel> tempList = new ArrayList<>();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject st = arr.getJSONObject(i);
                                String name = st.optString("name", "Unknown");
                                String totalAmount = st.optString("total_amount", "0");
                                String paidAmount = st.optString("paid_amount", "0");
                                String pendingAmount = st.optString("pending_amount", "0");
                                String feeStatus = st.optString("fee_status", "no record");
                                tempList.add(new StudentFeeModel("", name, totalAmount, paidAmount, pendingAmount, feeStatus));
                            }
                            adapter.feeStudentList.clear();
                            adapter.feeStudentList.addAll(tempList);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Error: " + (error.getMessage() == null ? "Network error" : error.getMessage()), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("admin_id", adminId);
                params.put("status", status == null ? "all" : status);
                if (query != null && !query.isEmpty()) {
                    params.put("query", query);
                }
                return params;
            }
        };

        Volley.newRequestQueue(getContext()).add(sr);
    }

    // ---- Fetch fee summary ----
    private void fetchAllfeesummery(String adminId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("Response", response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            overallAmount = obj.getString("overall_received");

                            JSONObject month = obj.getJSONObject("this_month");
                            String totalFees = month.getString("total_fees");
                            String pendingFees = month.getString("pending_fees");
                            String paidFees = month.getString("paid_fees");

                            tvOverall.setText("₹ *******");
                            tvTotal.setText("₹ " + totalFees);
                            tvPending.setText("₹ " + pendingFees);
                            tvPaid.setText("₹ " + paidFees);
                        } else {
                            Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("admin_id", adminId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(stringRequest);
    }
}
