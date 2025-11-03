package com.example.eduvia;

import static android.content.Context.MODE_PRIVATE;
import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceView extends Fragment {
    Loader loader;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private TabLayout dateTabs;
    private TextView tvClass, tvTotalStudents;
    private SharedPreferences sp;
    private String url = BASE_URL + "attendance.php";
    private RequestQueue queue;
    private RecyclerView rvStudents;
    private AttAdapter attAdapter;
    private String lastExt = "th";
    private String selectedDate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance_view, container, false);

        // Initialize SharedPreferences
        sp = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        dateTabs = view.findViewById(R.id.dateTabs);
        tvClass = view.findViewById(R.id.tvClass);
        tvTotalStudents = view.findViewById(R.id.tvTotalStudents);
        rvStudents = view.findViewById(R.id.recyclerAttendance);
        rvStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        loader = new Loader(requireContext());


        //Initialize adapter with status change callback
        attAdapter = new AttAdapter(requireContext(), new AttAdapter.OnItemClick() {
            @Override
            public void onClick(AttStModel s) {
                AttendanceStDetails fragment = new AttendanceStDetails();

                Bundle args = new Bundle();
                args.putString("student_id", String.valueOf(s.getId())); // assuming getId() returns string, else use putInt
                fragment.setArguments(args);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }


            @Override
            public void onStatusChange(int studentId, String status) {
                updateAttendance(studentId, status);
            }
        });
        rvStudents.setAdapter(attAdapter);

        queue = Volley.newRequestQueue(requireContext());

        // Setup date tabs
        Calendar calendar = Calendar.getInstance();
        List<String> dates = new ArrayList<>();
        selectedDate = sdf.format(calendar.getTime()); // today (default)
        dates.add("Today");

        for (int i = 1; i <= 5; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            dates.add(new SimpleDateFormat("dd MMM", Locale.getDefault()).format(calendar.getTime()));
        }
        for (int i = dates.size() - 1; i >= 0; i--) {
            dateTabs.addTab(dateTabs.newTab().setText(dates.get(i)));
        }
        dateTabs.getTabAt(dateTabs.getTabCount() - 1).select();

        // Handle tab clicks
        dateTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Calendar cal = Calendar.getInstance();
                int index = tab.getPosition(); // selected tab index
                cal.add(Calendar.DAY_OF_MONTH, -(dates.size() - 1 - index));
                selectedDate = sdf.format(cal.getTime()); // update date

                String selectedClass = getArguments() != null ? getArguments().getString("selectedClass") : "";
                fetchTotalStudents(selectedClass);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Load initial class data
        if (getArguments() != null) {
            String selectedClass = getArguments().getString("selectedClass");
            if (selectedClass.equals("1")) lastExt = "st";
            else if (selectedClass.equals("2")) lastExt = "nd";
            else if (selectedClass.equals("3")) lastExt = "rd";
            else if (selectedClass.equals("Other")) lastExt = "";
            else lastExt = "th";

            tvClass.setText("Class : " + selectedClass + lastExt);
            fetchTotalStudents(selectedClass);
            tvTotalStudents.setText("Total Students : 0");
        }
        return view;
    }

    // Fetch students list
    private void fetchTotalStudents(String selectedClass) {
        loader.show();
        String adminId = sp.getString("admin_id", "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    loader.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            int totalStudents = jsonObject.getInt("total");
                            tvTotalStudents.setText("Total Students : " + totalStudents);

                            JSONArray arr = jsonObject.getJSONArray("students");
                            List<AttStModel> tempList = new ArrayList<>();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);

                                AttStModel student = new AttStModel(
                                        obj.getInt("id"),
                                        obj.getString("name"),
                                        obj.optString("profile_img", "user_profile"),
                                        obj.optString("status", "")
                                );
                                tempList.add(student);
                            }
                            attAdapter.setItems(tempList);
                        } else {
                            Toast.makeText(getContext(),
                                    jsonObject.optString("message", "Something went wrong"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    if (error.networkResponse != null) {
                      Toast.makeText(getContext(), "Error: " + new String(error.networkResponse.data), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("adminId", adminId);
                params.put("markedBy", adminId);
                params.put("action", "totalStudents");
                params.put("selectedClass", selectedClass);
                params.put("date", selectedDate);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void updateAttendance(int studentId, String status) {
        loader.show();
        String adminId = sp.getString("admin_id", "");
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    loader.dismiss();

                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(getContext(), "Updated!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updateAttendance");
                params.put("adminId", adminId);
                params.put("studentId", String.valueOf(studentId));
                params.put("date", selectedDate);
                params.put("status", status.toLowerCase().trim());


                return params;
            }
        };
        queue.add(request);
    }
}
