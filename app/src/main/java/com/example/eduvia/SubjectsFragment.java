package com.example.eduvia;

import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectsFragment extends Fragment {
    Loader loader;
    RecyclerView recyclerview;
    SubjectAdapter subjectAdapter;
    RequestQueue queue;
    EditText etSubjectName, etfees;
    Spinner sp_class_from, sp_class_to;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subjects, container, false);

        recyclerview = v.findViewById(R.id.rvSubjects);
        loader = new Loader(getContext());
        recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

        subjectAdapter = new SubjectAdapter(new SubjectAdapter.OnSubjectClickListener() {
            @Override
            public void onEditClick(Subject subject) {
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.add_subject, null);

                etSubjectName = dialogView.findViewById(R.id.et_subject_name);
                sp_class_from = dialogView.findViewById(R.id.sp_class_from);
                sp_class_to = dialogView.findViewById(R.id.sp_class_to);
                etfees = dialogView.findViewById(R.id.et_fees);

                etSubjectName.setText(subject.getTvSubjectName());
                etfees.setText(subject.getTvChargesValue());

                // Step 1: Prepare class list with hint
                List<String> classList = new ArrayList<>();
                classList.add("Select Class"); // hint
                for (int i = 1; i <= 12; i++) {
                    classList.add("" + i);
                }
                classList.add("Other");

                // Step 2: Adapter for spinners
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, classList) {
                    @Override
                    public boolean isEnabled(int position) {
                        return position != 0; // Disable first item
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;
                        if (position == 0) tv.setTextColor(Color.GRAY);
                        else tv.setTextColor(Color.BLACK);
                        return view;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_class_from.setAdapter(adapter);
                sp_class_to.setAdapter(adapter);

                // Pre-select spinner values
                String[] range = subject.getTvClassRangeValue().split("-");
                if (range.length == 2) {
                    String from = range[0].trim();
                    String to = range[1].trim();

                    if (classList.contains(from)) {
                        sp_class_from.setSelection(classList.indexOf(from));
                    }
                    if (classList.contains(to)) {
                        sp_class_to.setSelection(classList.indexOf(to));
                    }
                }

                // Step 3: Build dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                TextView  add_subject_title = dialogView.findViewById(R.id.add_subject_title);
                add_subject_title.setText("Update Subject");
                TextView tv_note = dialogView.findViewById(R.id.tv_note);
                tv_note.setVisibility(View.GONE);
                builder.setView(dialogView)
                        .setPositiveButton("Update", null)
                        .setNegativeButton("Cancel", null);

                AlertDialog dialog = builder.create();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(
                            ContextCompat.getDrawable(getContext(), R.drawable.alert_round_corner)
                    );
                }
                dialog.show();

                // Handle update click
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btnView -> {
                    String subjectName = etSubjectName.getText().toString().trim();
                    String fee = etfees.getText().toString().trim();
                    String classFrom = sp_class_from.getSelectedItem().toString();
                    String classTo = sp_class_to.getSelectedItem().toString();

                    if (subjectName.isEmpty() || fee.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (classFrom.equals("Select Class") || classTo.equals("Select Class")) {
                        Toast.makeText(getContext(), "Please select both class ranges", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Call Update API instead of Add
                    updateSubjectToServer(subject.getId(), subjectName, classFrom, classTo, fee);
                    fetchSubjects();
                    dialog.dismiss();
                });
            }


            private void updateSubjectToServer(int subjectId, String name, String classFrom, String classTo, String fee) {
                loader.show();
                String updateUrl = BASE_URL + "subject.php";

                StringRequest request = new StringRequest(Request.Method.POST, updateUrl,
                        response -> {
                            Log.d("UpdateSubject", response);
                            loader.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                boolean success = jsonObject.getBoolean("success");

                                if (success) {
                                    Toast.makeText(getContext(), "Subject updated successfully", Toast.LENGTH_SHORT).show();
                                    fetchSubjects();
                                } else {
                                    String msg = jsonObject.optString("message", "Update failed");
                                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Parse error", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }) {

                    @Override
                    protected Map<String, String> getParams() {
                        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                        String adminId = sp.getString("admin_id", "");

                        Map<String, String> params = new HashMap<>();
                        params.put("action","update_subject");
                        params.put("admin_id", adminId);
                        params.put("subject_id", String.valueOf(subjectId));
                        params.put("subject_name", name);
                        params.put("class_from", classFrom);
                        params.put("class_to", classTo);
                        params.put("fee", fee);
                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(requireContext());
                queue.add(request);
            }


            @Override
            public void onDeleteClick(Subject subject) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Delete Subject");
                builder.setIcon(R.drawable.logo_blue);
                builder.setMessage("Are you sure you want to delete this subject?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    String subjectId = String.valueOf(subject.getId());
                    // Delete the student
                    deleteSubj(subjectId);
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                    // Do nothing
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            private void deleteSubj(String subjectId) {
                SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String adminId = sp.getString("admin_id", "");

                String urlWithParams = BASE_URL + "subject.php";

                StringRequest sr = new StringRequest(Request.Method.POST, urlWithParams,
                        response -> {
                            // reload the page
                            fetchSubjects();
                            Toast.makeText(getContext(), "Subject deleted successfully", Toast.LENGTH_SHORT).show();
                        },
                        error -> {
                            error.printStackTrace();
                            Toast.makeText(getContext(),
                                    "Network error: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("action", "delete_subject");
                        params.put("admin_id", adminId);
                        params.put("subject_id", subjectId);
                        return params;
                    }
                };

                Volley.newRequestQueue(requireContext()).add(sr);
            }
        });

        recyclerview.setAdapter(subjectAdapter);

        queue = Volley.newRequestQueue(requireContext());

        fetchSubjects();

        return v;
    }

    private void fetchSubjects() {
        loader.show();
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        String urlWithParams = BASE_URL + "subject.php?action=get_subjects&admin_id=" + adminId;

        StringRequest sr = new StringRequest(Request.Method.GET, urlWithParams,
                response -> {
                    Log.d("responseSubject", response);
                    loader.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("subjects");

                            List<Subject> tempList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                // Build class range string first
                                String range = obj.getString("class_from") + " - " + obj.getString("class_to");
                                String rs = obj.getString("fee");
                                // Now create Subject
                                Subject sub = new Subject(
                                        obj.getInt("id"),
                                        obj.getString("subject_name"),
                                        range,
                                        rs
                                );

                                tempList.add(sub);
                            }

                            subjectAdapter.setData(tempList);
                        } else {
                            Toast.makeText(getContext(), "No subjects found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Parse error", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            error.printStackTrace();
            Toast.makeText(getContext(),
                    "Network error: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });

        queue.add(sr);
    }

}
