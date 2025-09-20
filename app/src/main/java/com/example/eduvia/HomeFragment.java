package com.example.eduvia;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {

    String url = BASE_URL + "subject.php";
    TextView user_name, addSubject,tvNoAnnouncements, add_student, role, add_announcement, total_students;
    EditText etSubjectName, etfees;
    Spinner sp_class_from, sp_class_to;
    ChipGroup chipGroup;
    ImageView our_profile;
    RecyclerView rvAnnouncements;
    SharedPreferences sp;
    AnnouncementAdapter announcementAdapter;
    LinearLayout student_view;
    List<AnnouncementModel> annList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        user_name = view.findViewById(R.id.user_name);
        addSubject = view.findViewById(R.id.add_subject);
        chipGroup = view.findViewById(R.id.subject_chip_group);
        add_student = view.findViewById(R.id.add_student);
        add_announcement = view.findViewById(R.id.add_announcement);
        total_students = view.findViewById(R.id.total_students);
        tvNoAnnouncements = view.findViewById(R.id.tvNoAnnouncements);
        student_view = view.findViewById(R.id.student_view);

        student_view.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new StudentFragment())
                    .addToBackStack(null)
                    .commit();
        });
        add_announcement.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Announcement())
                    .commit();
        });
        role = view.findViewById(R.id.role);
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedRole = sp.getString("role", "");
        role.setText(savedRole);

        fetchTotal();



        add_student.setOnClickListener(view1 -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new AddStudent())
                    .commit();
        });

        fetchSubjectsFromServer();


        addSubject.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.add_subject, null);
            etSubjectName = dialogView.findViewById(R.id.et_subject_name);
            sp_class_from = dialogView.findViewById(R.id.sp_class_from);
            sp_class_to = dialogView.findViewById(R.id.sp_class_to);
            etfees = dialogView.findViewById(R.id.et_fees);

            // Step 1: Prepare class list with hint
            List<String> classList = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                classList.add("" + i);
            }
            classList.add("Other");

            // Step 2: Set adapter for both spinners
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, classList) {


                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    if (position == 0) {
                        tv.setTextColor(Color.BLACK);
                    } else {
                        tv.setTextColor(Color.BLACK);
                    }
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_class_from.setAdapter(adapter);
            sp_class_to.setAdapter(adapter);

            // Step 3: Build dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(dialogView)
                    .setPositiveButton("Add", null)
                    .setNegativeButton("Cancel", null);

            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        ContextCompat.getDrawable(getContext(), R.drawable.alert_round_corner)
                );
            }
            dialog.show();

            // Step 4: Handle validation AFTER showing
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btnView -> {
                String subject = etSubjectName.getText().toString().trim();
                String fee = etfees.getText().toString().trim();
                String classFrom = sp_class_from.getSelectedItem().toString();
                String classTo = sp_class_to.getSelectedItem().toString();

                // Validation
                if (subject.isEmpty() || fee.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (classFrom.equals("Select Class") || classTo.equals("Select Class")) {
                    Toast.makeText(getContext(), "Please select both class ranges", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!classFrom.equals("Other") && !classTo.equals("Other")) {
                    int from = Integer.parseInt(classFrom);
                    int to = Integer.parseInt(classTo);
                    if (from > to) {
                        Toast.makeText(getContext(), "Class From cannot be greater than Class To", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (from < 1 || to > 12) {
                        Toast.makeText(getContext(), "Invalid class range", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Passed validation → call API & close
                sendSubjectToServer(subject, classFrom, classTo, fee);
                dialog.dismiss();
            });
        });


        SharedPreferences sharedPreferences =
                requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedName = sharedPreferences.getString("name", "");
        user_name.setText(savedName);
        fetchDetails(sharedPreferences.getString("admin_id", ""));

        rvAnnouncements = view.findViewById(R.id.rvAnnouncements);
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(getContext()));
        announcementAdapter = new AnnouncementAdapter(getContext(), annList, position -> {
            // Handle delete click
            AnnouncementModel ann = annList.get(position);

            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Announcement")
                    .setIcon(R.drawable.logo_blue)
                    .setMessage("Are you sure you want to delete this announcement?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Call delete function if confirmed
                        deleteAnnouncementFromServer(ann.getId(), position);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss(); // Just close dialog
                    })
                    .create()
                    .show();
        });

        rvAnnouncements.setAdapter(announcementAdapter);

// Fetch announcements
        fetchAnnouncements();


        return view;
    }

    private void fetchSubjectsFromServer() {
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        String fetchUrl = url + "?action=get_subjects&admin_id=" + adminId;

        StringRequest request = new StringRequest(Request.Method.GET, fetchUrl,
                response -> {
                    Log.d("fetchSubjects", response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        if (success) {
                            JSONArray jsonArray = jsonObject.getJSONArray("subjects");

                            chipGroup.removeAllViews();
                            Set<String> uniqueSubject = new HashSet<>();


                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                String subjectName = obj.getString("subject_name");

                                // Normalize subject name to lower case for uniqueness check
                                String normalized = subjectName.trim().toLowerCase();

                                if (!uniqueSubject.contains(normalized)) {
                                    uniqueSubject.add(normalized);

                                    Chip chip = new Chip(getContext());
                                    chip.setText(subjectName); // keep original text
                                    chip.setChipBackgroundColorResource(R.color.tt_chip);
                                    chip.setTextColor(Color.BLACK);
                                    chip.setChipStrokeWidth(1f);
                                    chip.setChipStrokeColorResource(R.color.tt_primary);
                                    chip.setChipCornerRadius(25f);

                                    chip.setOnClickListener(v -> {
                                        requireActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.container, new SubjectsFragment())
                                                .addToBackStack(null)
                                                .commit();
                                        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);
                                        bottomNav.setSelectedItemId(R.id.nav_subject);
                                        return;
                                    });

                                    chipGroup.addView(chip);
                                }
                            }
                        } else {
                            String msg = jsonObject.optString("message", "No subjects found");
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private void sendSubjectToServer(String subject, String classFrom, String classTo, String fee) {
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("response", response);

                    String letter = subject;
                    String subjectName = letter.substring(0, 1).toUpperCase() + letter.substring(1).toLowerCase();
                    Chip chip = new Chip(getContext());
                    chip.setText(subjectName);
                    chip.setChipBackgroundColorResource(R.color.tt_chip);
                    chip.setTextColor(Color.BLACK);
                    chip.setChipStrokeWidth(1f);
                    chip.setChipStrokeColorResource(R.color.tt_primary);
                    chip.setChipCornerRadius(25f);
                    chipGroup.addView(chip);
                },
                error -> {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "add_subject");
                params.put("admin_id", adminId);
                String sub = subject;
                String s = sub.substring(0, 1).toUpperCase() + sub.substring(1);
                params.put("subject_name", s);
                params.put("class_from", classFrom);
                params.put("class_to", classTo);
                params.put("fee", fee);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private void fetchDetails(String adminId) {
        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONObject data = json.getJSONObject("data");
                            String name = data.optString("name", "");
                            user_name.setText(name);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e(TAG, "Volley Error: " + error.getMessage())) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "profileDetails");
                params.put("admin_id", adminId);
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(sr);
    }
    private void fetchTotal() {
        String url = BASE_URL + "fetchData.php";
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");


        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            int totalStudents = jsonObject.getInt("total_students");


                            total_students.setText(String.valueOf(totalStudents));
                        } else {
                            Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Volley error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "get_total");
                params.put("admin_id", adminId);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(sr);
    }
    private void fetchAnnouncements() {
        String annUrl = BASE_URL + "announcement.php";

        StringRequest request = new StringRequest(Request.Method.POST, annUrl,
                response -> {
                    Log.d("Announcementresponse", response);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("announcements");
                            annList.clear();

                            if (arr.length() == 0) {
                                tvNoAnnouncements.setVisibility(View.VISIBLE);
                                rvAnnouncements.setVisibility(View.GONE);
                            } else {
                                tvNoAnnouncements.setVisibility(View.GONE);
                                rvAnnouncements.setVisibility(View.VISIBLE);

                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject obj = arr.getJSONObject(i);
                                    annList.add(new AnnouncementModel(
                                            obj.optString("id", ""),
                                            obj.optString("title", ""),
                                            obj.optString("message", ""),
                                            obj.optString("start_date", ""),
                                            obj.optString("expiry_date", ""),
                                            obj.optString("start_time", ""),
                                            obj.optString("expiry_time", "")
                                    ));
                                }
                                announcementAdapter.notifyDataSetChanged();
                            }
                        } else {
                            tvNoAnnouncements.setVisibility(View.VISIBLE);
                            rvAnnouncements.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String adminId = sp.getString("admin_id", "");
                Map<String, String> params = new HashMap<>();
                params.put("admin_id", adminId);
                params.put("action", "ann_fetch");
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void deleteAnnouncementFromServer(String id, int position) {
        String annUrl = BASE_URL + "announcement.php";
        StringRequest request = new StringRequest(Request.Method.POST, annUrl,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            annList.remove(position);
                            announcementAdapter.notifyItemRemoved(position);
                            Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "delete_ann");
                params.put("id", id);
                return params;
            }
        };
        Volley.newRequestQueue(requireContext()).add(request);
    }


}
