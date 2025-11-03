package com.example.eduvia;

import static android.content.Context.MODE_PRIVATE;
import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Announcement extends Fragment {

    EditText etStartDate, etExpiryDate, etStartTime, etExpiryTime,etTitle,etMessage;
    Spinner etClass;
    RadioGroup rgSendTo;
    RadioButton rbAll, rbClass;
    RequestQueue queue;
    Button btnSubmit, btnCancel;

    String url = BASE_URL + "announcement.php";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view1 = inflater.inflate(R.layout.fragment_announcement, container, false);

        // Initialize views
        etTitle = view1.findViewById(R.id.etTitle);
        etMessage = view1.findViewById(R.id.etMessage);
        etClass = view1.findViewById(R.id.select_class);
        etStartDate = view1.findViewById(R.id.etStartDate);
        etExpiryDate = view1.findViewById(R.id.etExpiryDate);
        etStartTime = view1.findViewById(R.id.etStartTime);
        etExpiryTime = view1.findViewById(R.id.etExpiryTime);
        btnSubmit = view1.findViewById(R.id.btnSubmit);
        btnCancel = view1.findViewById(R.id.btnCancel);
        rgSendTo = view1.findViewById(R.id.rgSendTo);
        rbAll = view1.findViewById(R.id.rbAll);
        rbClass = view1.findViewById(R.id.rbClass);

        // RadioGroup logic (Show/Hide Class field)
        rgSendTo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbClass) {
                etClass.setVisibility(View.VISIBLE);
            } else {
                etClass.setVisibility(View.GONE);
            }
        });

        // Step 1: Prepare class list with hint
        List<String> classList = new ArrayList<>();
        classList.add("Select Class");
        for (int i = 1; i <= 12; i++) {
            classList.add("" + i);
        }
        classList.add("Other");

        // Step 2: Set adapter spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, classList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etClass.setAdapter(adapter);

        etStartDate.setOnClickListener(v -> {
            // Get today's date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        etStartDate.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
                        etStartDate.setError(null);
                    },
                    year, month, day // pass current date here
            );

            datePicker.show();
        });

        etExpiryDate.setOnClickListener(v -> {
            // Get today's date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Set calendar to the picked date
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);


                        int nextDay = selectedDate.get(Calendar.DAY_OF_MONTH);
                        int nextMonth = selectedDate.get(Calendar.MONTH) + 1;
                        int nextYear = selectedDate.get(Calendar.YEAR);

                        etExpiryDate.setText(nextDay + "/" + nextMonth + "/" + nextYear);
                        etExpiryDate.setError(null);
                    },
                    year, month, day // use current date as default
            );

            datePicker.show();
        });


        etStartTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                    (view, hour, minute) -> {
                        etStartTime.setText(hour + ":" + minute);
                        etStartTime.setError(null);
                    },
                    12, 0, true);
            timePicker.show();
        });

        etExpiryTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                    (view, hour, minute) -> {
                        etExpiryTime.setText(hour + ":" + minute);
                        etExpiryTime.setError(null);
                    },
                    12, 0, true);
            timePicker.show();
        });

        btnSubmit.setOnClickListener(v -> {
            validateAndSend();
        });
        btnCancel.setOnClickListener(v -> {
            etClass.setSelection(0);
            etTitle.setText("");
            etMessage.setText("");
            etStartDate.setText("");
            etExpiryDate.setText("");
            etStartTime.setText("");
            etExpiryTime.setText("");
            rgSendTo.clearCheck();
            etClass.setVisibility(View.GONE);
            // Go back to home fragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new HomeFragment())
                    .commit();
        });

        return view1;
    }

    private void validateAndSend() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String className = etClass.getSelectedItem().toString();
        String startDate = etStartDate.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String expiryTime = etExpiryTime.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(message)) {
            etMessage.setError("Message is required");
            etMessage.requestFocus();
            return;
        }
        if (rgSendTo.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "Please select Send To option", Toast.LENGTH_SHORT).show();
            rgSendTo.requestFocus();
            return;
        }

        if (rbClass.isChecked() && (className.equals("Select Class"))) {
            Toast.makeText(getContext(), "Please select a class", Toast.LENGTH_SHORT).show();
            rbClass.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(startDate)) {
            etStartDate.setError("Select Start Date");
            etStartDate.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(expiryDate)) {
            etExpiryDate.setError("Select Expiry Date");
            etExpiryDate.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(startTime)) {
            etStartTime.setError("Select Start Time");
            etStartTime.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(expiryTime)) {
            etExpiryTime.setError("Select Expiry Time");
            etExpiryTime.requestFocus();
            return;
        }

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            java.util.Date startD = sdf.parse(startDate);
            java.util.Date endD = sdf.parse(expiryDate);

            if (endD.before(startD)) {
                Toast.makeText(getContext(), "Expiry date must be after Start date", Toast.LENGTH_SHORT).show();
                return;
            }

            // If same day, check time
            if (startD.equals(endD)) {
                java.text.SimpleDateFormat stf = new java.text.SimpleDateFormat("HH:mm");
                java.util.Date startT = stf.parse(startTime);
                java.util.Date endT = stf.parse(expiryTime);

                if (endT.before(startT)) {
                    Toast.makeText(getContext(), "Expiry time must be after Start time", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Invalid date/time format", Toast.LENGTH_SHORT).show();
            return;
        }

        sendAnnouncement(className, startDate, expiryDate, startTime, expiryTime,title,message);
    }

    private void sendAnnouncement(String className, String startDate, String expiryDate,
                                  String startTime, String expiryTime,String title,String message) {
        String sendTo = rbAll.isChecked() ? "All Students" : "By Class";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response ->{
                    if (response.equals("success")) {
                        Toast.makeText(getContext(), "Announcement created successfully", Toast.LENGTH_SHORT).show();
                        etClass.setSelection(0);
                        etTitle.setText("");
                        etMessage.setText("");
                        etStartDate.setText("");
                        etExpiryDate.setText("");
                        etStartTime.setText("");
                        etExpiryTime.setText("");
                        rgSendTo.clearCheck();
                        etClass.setVisibility(View.GONE);
                        // Go back to home fragment
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, new HomeFragment())
                                .commit();

                    }

                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                String adminId = sp.getString("admin_id", "");


                params.put("admin_id", adminId);
                params.put("action", "ann_create");

                params.put("title", title);
                params.put("message", message);
                params.put("class", className);
                params.put("start_date", startDate);
                params.put("expiry_date", expiryDate);
                params.put("start_time", startTime);
                params.put("expiry_time", expiryTime);
                params.put("send_to", sendTo);
                return params;
            }
        };

        queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }
}
