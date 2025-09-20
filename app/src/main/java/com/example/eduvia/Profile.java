package com.example.eduvia;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Profile extends Fragment {
    String url = BASE_URL + "fetchData.php";
    TextView edit_profile, total_subjects, total_students, announcement, fee_view, change_password, logout_btn,role;
    LinearLayout subject_view, student_view;
     ImageView profileImage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        edit_profile = view.findViewById(R.id.edit_profile);
        total_subjects = view.findViewById(R.id.total_subjects);
        total_students = view.findViewById(R.id.total_students);
        announcement = view.findViewById(R.id.announcement);
        fee_view = view.findViewById(R.id.fee_view);
        change_password = view.findViewById(R.id.change_password);
        logout_btn = view.findViewById(R.id.logout_btn);
        subject_view = view.findViewById(R.id.subject_view);
        student_view = view.findViewById(R.id.student_view);
        profileImage = view.findViewById(R.id.profile_image);
        role = view.findViewById(R.id.role);
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedRole = sp.getString("role", "");
        role.setText(savedRole);

        edit_profile.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new EditProfile())
                    .addToBackStack(null)
                    .commit();

        });

        change_password.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new ChangePassword())
                    .addToBackStack(null)
                    .commit();
        });

        logout_btn.setOnClickListener(v -> {
            SharedPreferences sharedPreferences =
                    requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.isLoggedIn = false;
            mainActivity.hideBars();

            Intent intent = new Intent(requireActivity(), SignIn.class);
            startActivity(intent);
            requireActivity().finish();
        });
        String adminId = sp.getString("admin_id", "");
        fetchDetails(adminId);
        fetchTotal();
        subject_view.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new SubjectsFragment())
                    .addToBackStack(null)
                    .commit();
        });
        student_view.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new StudentFragment())
                    .addToBackStack(null)
                    .commit();
        });

        announcement.setOnClickListener(view1 -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Announcement())
                    .addToBackStack(null)
                    .commit();
        });
        return view;
    }

    private void fetchTotal() {
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");


        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            int totalSubjects = jsonObject.getInt("total_subjects");
                            int totalStudents = jsonObject.getInt("total_students");

                            // Example: update UI
                            total_subjects.setText(String.valueOf(totalSubjects));
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

    private void fetchDetails(String adminId) {
        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONObject data = json.getJSONObject("data");

                            // Load profile image
                            String profileImg = data.optString("profile_img", "");
                            if (!profileImg.isEmpty()) {
                                String fullUrl = profileImg.startsWith("http") ?
                                        profileImg : BASE_URL + profileImg;

                                Glide.with(this)
                                        .load(fullUrl)
                                        .placeholder(R.drawable.user_profile)
                                        .into(profileImage);
                            }
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

}
