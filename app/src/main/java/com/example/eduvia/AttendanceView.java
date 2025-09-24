package com.example.eduvia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AttendanceView extends Fragment {
    LinearLayout dateContainer;
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_attendance_view, container, false);

        // Initialize views
        dateContainer = view.findViewById(R.id.dateContainer);

        Calendar calendar = Calendar.getInstance();
// Generate today + next 6 days
        for (int i = 0; i < 7; i++) {
            Button dateBtn = new Button(getContext());

            // Button text
            if (i == 0) {
                dateBtn.setText("Today");
                dateBtn.didTouchFocusSelect();
            } else {
                dateBtn.setText(sdf.format(calendar.getTime()));
            }

            // Style
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            dateBtn.setLayoutParams(params);

            // Material style
            dateBtn.setBackgroundResource(R.drawable.selector_date_button);
            dateBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.tt_text));

            // Handle click
            int finalI = i;
            dateBtn.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Selected: " + dateBtn.getText(), Toast.LENGTH_SHORT).show();
                // TODO: filter attendance by selected date
            });
            // Add to container
            dateContainer.addView(dateBtn);
            // Move calendar to next day
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (getArguments() != null) {
            String selectedClass = getArguments().getString("selectedClass");
            Toast.makeText(getContext(), "Showing students of Class: " + selectedClass, Toast.LENGTH_SHORT).show();

            // 🔹 Call API here to fetch students by selectedClass
        }
        return view;
    }
}