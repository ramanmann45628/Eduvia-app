package in.eduvia.org;

import static android.content.Context.MODE_PRIVATE;
import static in.eduvia.org.SignIn.PREF_NAME;
import static in.eduvia.org.SignUp.BASE_URL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AttendanceStDetails extends Fragment {

    private CalendarView cal;
    private SharedPreferences sp;
    private String url = BASE_URL + "attendance.php";
    Loader loader;

    private TextView tvClass, TodayStatus, tvName, tvselectedDate, tvSelectedMonthYear, tvPercentage, tvTotalDays;
    private ProgressBar progressAttendance;
    private ImageView ivAbsent, ivPresent;

    private String studentId = "1";
    private String studentStartDate;
    private Map<String, String> attendanceMap = new HashMap<>(); 

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance_st_details, container, false);

        sp = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (getArguments() != null) {
            studentId = getArguments().getString("student_id", "1");
        }

        // Initialize views
        tvClass = view.findViewById(R.id.tvClass);
        TodayStatus = view.findViewById(R.id.tvTodayStatus);
        tvName = view.findViewById(R.id.tvName);
        tvselectedDate = view.findViewById(R.id.tvSelectedDate);
        tvSelectedMonthYear = view.findViewById(R.id.tvSelectedMonthYear);
        tvPercentage = view.findViewById(R.id.tvPercentage);
        tvTotalDays = view.findViewById(R.id.tvTotalDays);
        progressAttendance = view.findViewById(R.id.progressAttendance);
        ivAbsent = view.findViewById(R.id.ivAbsent);
        ivPresent = view.findViewById(R.id.ivPresent);
        cal = view.findViewById(R.id.calendarView);
        loader = new Loader(getContext());

        // Disable attendance clicks until data loaded
        ivPresent.setEnabled(false);
        ivAbsent.setEnabled(false);

        // Set today's date initially
        Calendar today = Calendar.getInstance();
        String todayStr = today.get(Calendar.DAY_OF_MONTH) + "-" + (today.get(Calendar.MONTH) + 1) + "-" + today.get(Calendar.YEAR);
        tvselectedDate.setText(todayStr);

        // Fetch student details and attendance
        fetchStudentSummary(today.get(Calendar.DAY_OF_MONTH),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.YEAR));

        // Set attendance click listeners
        ivPresent.setOnClickListener(v -> markAttendance("present"));
        ivAbsent.setOnClickListener(v -> markAttendance("absent"));

        return view;
    }

    private void fetchStudentSummary(int day, int month, int year) {
        loader.show();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
            loader.dismiss();
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONObject student = json.getJSONObject("student");
                            tvName.setText(student.getString("name"));
                            tvClass.setText("Class: " + student.getString("class"));
                            studentStartDate = student.getString("start_date"); // yyyy-MM-dd

                            JSONObject summary = json.getJSONObject("summary");
                            tvSelectedMonthYear.setText(summary.getString("month"));
                            tvPercentage.setText(summary.getInt("percentage") + "%");
                            tvTotalDays.setText(summary.getInt("present") + "/" + summary.getInt("total_days") + " Days Present / Total");
                            progressAttendance.setProgress(summary.getInt("percentage"));

                            // Map attendance data
                            attendanceMap.clear();
                            JSONArray attendanceArray = json.getJSONArray("attendance");
                            for (int i = 0; i < attendanceArray.length(); i++) {
                                JSONObject att = attendanceArray.getJSONObject(i);
                                String date = att.getString("date"); // yyyy-MM-dd
                                String status = att.getString("status").toLowerCase();
                                attendanceMap.put(date, status);
                            }

                            // Enable attendance marking
                            ivPresent.setEnabled(true);
                            ivAbsent.setEnabled(true);

                            // Update today's status and dot **after data is loaded**
                            String todayStr = tvselectedDate.getText().toString();
                            updateTodayStatusAndDot(todayStr);


                            // Now set calendar listener (after start date is known)
                            setupCalendarListener();

                        } else {
                            Toast.makeText(getContext(), "Failed to load student data", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getStudentSummary");
                params.put("studentId", studentId);
                params.put("day", String.valueOf(day));
                params.put("month", String.format("%02d", month));
                params.put("year", String.valueOf(year));
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(request);
    }

    private void setupCalendarListener() {
        cal.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDateStr = dayOfMonth + "-" + (month + 1) + "-" + year;

            try {
                SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                SimpleDateFormat sdfServer = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                Date selectedDate = sdfDisplay.parse(selectedDateStr);
                Date startDate = sdfServer.parse(studentStartDate); // start date from server

                if (selectedDate.before(startDate)) {
                    // Show alert
                    new androidx.appcompat.app.AlertDialog.Builder(getContext())
                            .setTitle("Invalid Date")
                            .setMessage("This student started from " + studentStartDate + ". Cannot select a date before that.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                    return;
                }

                // Valid date
                tvselectedDate.setText(selectedDateStr);
                updateTodayStatus(selectedDateStr);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateTodayStatus(String selectedDate) {
        String serverDate = convertDateToServerFormat(selectedDate); // dd-MM-yyyy -> yyyy-MM-dd
        String status = attendanceMap.getOrDefault(serverDate, "");

        TodayStatus.setText("Today: " + (status.isEmpty() ? "Not Marked" : capitalize(status)));

        // Update dot icons
        if ("present".equalsIgnoreCase(status)) {
            ivPresent.setImageResource(R.drawable.circle_green);
            ivAbsent.setImageResource(R.drawable.circle_gray);
        } else if ("absent".equalsIgnoreCase(status)) {
            ivPresent.setImageResource(R.drawable.circle_gray);
            ivAbsent.setImageResource(R.drawable.circle_red);
        } else {
            ivPresent.setImageResource(R.drawable.circle_gray);
            ivAbsent.setImageResource(R.drawable.circle_gray);
        }
    }

    private void markAttendance(String status) {
        loader.show();
        String adminId = sp.getString("admin_id", "");
        if (adminId.isEmpty()) {
            Toast.makeText(getContext(), "Admin ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedDate = convertDateToServerFormat(tvselectedDate.getText().toString());

        // Prevent marking before start date
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date selected = sdf.parse(selectedDate);
            Date start = sdf.parse(studentStartDate);
            if (selected.before(start)) {
                Toast.makeText(getContext(), "Cannot mark attendance before joining date", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
            loader.dismiss();
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(getContext(), "Attendance Updated", Toast.LENGTH_SHORT).show();
                            attendanceMap.put(selectedDate, status); // update local map
                            updateTodayStatus(tvselectedDate.getText().toString());
                            decorateCalendar(); // update dot

                            // Refetch summary for live percentage update
                            Calendar today = Calendar.getInstance();
                            fetchStudentSummary(today.get(Calendar.DAY_OF_MONTH),
                                    today.get(Calendar.MONTH) + 1,
                                    today.get(Calendar.YEAR));

                        } else {
                            Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updateAttendance");
                params.put("adminId", adminId);
                params.put("studentId", studentId);
                params.put("date", selectedDate);
                params.put("status", status.toLowerCase());
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(request);
    }

    private String convertDateToServerFormat(String date) {
        try {
            SimpleDateFormat sdfClient = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat sdfServer = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdfServer.format(sdfClient.parse(date));
        } catch (Exception e) {
            e.printStackTrace();
            return date;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private void decorateCalendar() {
        String selectedDate = tvselectedDate.getText().toString();
        updateTodayStatusAndDot(selectedDate);
    }

    private void updateTodayStatusAndDot(String selectedDate) {
        String serverDate = convertDateToServerFormat(selectedDate); // dd-MM-yyyy -> yyyy-MM-dd
        String status = attendanceMap.getOrDefault(serverDate, "");

        TodayStatus.setText("Today: " + (status.isEmpty() ? "Not Marked" : capitalize(status)));

        // Update dot icons
        if ("present".equalsIgnoreCase(status)) {
            ivPresent.setImageResource(R.drawable.circle_green);
            ivAbsent.setImageResource(R.drawable.circle_gray);
        } else if ("absent".equalsIgnoreCase(status)) {
            ivPresent.setImageResource(R.drawable.circle_gray);
            ivAbsent.setImageResource(R.drawable.circle_red);
        } else {
            ivPresent.setImageResource(R.drawable.circle_gray);
            ivAbsent.setImageResource(R.drawable.circle_gray);
        }
    }

}
