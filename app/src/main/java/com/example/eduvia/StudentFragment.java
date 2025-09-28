package com.example.eduvia;

import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StudentFragment extends Fragment {
    Loader loader;
    TextView add_student;
    RecyclerView rvStudents;
    StudentAdapter studentAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_student, container, false);
        add_student = v.findViewById(R.id.add_student);
        rvStudents = v.findViewById(R.id.rvStudents);
        loader = new Loader(getContext());
        rvStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        studentAdapter = new StudentAdapter(student -> {
            // Pass data to fragment
            StudentDetails fragment = new StudentDetails();
            Bundle args = new Bundle();
            args.putString("student_id", String.valueOf(student.getId()));
            fragment.setArguments(args);

            // Replace fragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment) // your container id
                    .addToBackStack(null)
                    .commit();
        });
        rvStudents.setAdapter(studentAdapter);

        fetchStudentsFromServer("All");

        ChipGroup classChipGroup = v.findViewById(R.id.class_chip_group);
        classChipGroup.setSingleSelection(true);
        classChipGroup.check(R.id.chip_all);

        classChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String filter = "All";
            if (!checkedIds.isEmpty()) {
                int chipId = checkedIds.get(0);
                Chip selectedChip = group.findViewById(chipId);
                filter = selectedChip.getText().toString();
            }
            fetchStudentsFromServer(filter);
        });

        add_student.setOnClickListener(view -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new AddStudent())
                    .commit();
        });
        return v;

    }

    private void fetchStudentsFromServer(String filter) {
        loader.show();
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        String urlWithParams = BASE_URL + "student.php?action=get_students&admin_id=" + adminId;

        if (!filter.equalsIgnoreCase("All")) {
            // Normalize dash type (convert en-dash or em-dash to hyphen)
            filter = filter.replace("–", "-").replace("—", "-");

            if (filter.contains("-")) {
                String[] parts = filter.split("-");
                if (parts.length == 2) {
                    String from = parts[0].trim();
                    String to = parts[1].trim();
                    urlWithParams += "&class_from=" + from + "&class_to=" + to;
                }
            } else {
                // single class, send same for from and to
                String single = filter.trim();
                urlWithParams += "&class_from=" + single + "&class_to=" + single;
            }
        }

        Log.d("urlWithParams", urlWithParams);

        StringRequest sr = new StringRequest(Request.Method.GET, urlWithParams,
                response -> {
                    loader.dismiss();
                    try {
                        Log.d("Studentresponse", response);
                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getBoolean("success")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("students");
                            List<Student> tempList = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                String subj = obj.getString("subjects");
                                String displaySubj = subj.length() > 25 ? subj.substring(0, 25) + "..." : subj;

                                // Create student object directly, no extra filtering needed
                                int status = obj.optInt("status", 0);
                                Student student = new Student(
                                        obj.getInt("id"),
                                        obj.getString("name"),
                                        obj.optString("profile_img", "user_profile"),
                                        displaySubj,
                                        obj.getString("class"),
                                        status
                                );

                                tempList.add(student);
                            }
                            studentAdapter.setItems(tempList);
                        } else {
                            Toast.makeText(getContext(),
                                    jsonObject.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(),
                                "Parsing error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(),
                            "Network error: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(requireContext()).add(sr);
    }
}