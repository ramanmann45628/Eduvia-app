package com.example.eduvia;

import static android.content.Context.MODE_PRIVATE;
import static com.example.eduvia.EditProfile.PICK_IMAGE;
import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AddStudent extends Fragment {
    EditText etFullName, etEmail, etContact, etDOB, etParentName, etParentContact, etCustomSubjects;
    LinearLayout layoutSubjects;
    ChipGroup chipGroupSubjects;
    Bitmap selectedProfileBitmap;
    ImageView imgProfile;
    Button btnCancel, btnSubmit,uploadImage;
    String url = BASE_URL + "student.php";
    RequestQueue queue;
    RadioButton rbMale, rbFemale, rbOther;
    RadioGroup rgGender;
    Spinner etClass;
    String gender;
    String subjects = "";
    private String lastFetchedClass = "";
    SharedPreferences sp;
    private List<String> preSelectedSubjects = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_student, container, false);

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        etContact = view.findViewById(R.id.etContact);
        etDOB = view.findViewById(R.id.etDOB);
        rgGender = view.findViewById(R.id.rgGender);
        rbMale = view.findViewById(R.id.rbMale);
        rbFemale = view.findViewById(R.id.rbFemale);
        rbOther = view.findViewById(R.id.rbOther);
        layoutSubjects = view.findViewById(R.id.layoutSubjects);
        chipGroupSubjects = view.findViewById(R.id.chipGroupSubjects);
        uploadImage =  view.findViewById(R.id.btnChangeImage);

        uploadImage.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });




        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMale) {
                gender = "Male";
            } else if (checkedId == R.id.rbFemale) {
                gender = "Female";
            } else if (checkedId == R.id.rbOther) {
                gender = "Other";
            }
        });

        etDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view1, year1, month1, dayOfMonth) -> {
                etDOB.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1);
            }, year, month, day);
            datePickerDialog.show();
        });

        etParentName = view.findViewById(R.id.etParentName);
        etParentContact = view.findViewById(R.id.etParentContact);
        etClass = view.findViewById(R.id.select_class);

        // Step 1: Prepare class list with hint
        List<String> classList = new ArrayList<>();
        classList.add("Select Class"); // <-- hint
        for (int i = 1; i <= 12; i++) {
            classList.add("" + i);
        }
        classList.add("Other");

        // Step 2: Set adapter spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, classList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Disable first item (hint)
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY); // hint color
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etClass.setAdapter(adapter);

        // Listen for class selection
        etClass.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedClass = etClass.getSelectedItem().toString();

                if (!selectedClass.equals("Select Class")) {
                    // Avoid multiple same requests
                    if (!selectedClass.equals(lastFetchedClass)) {
                        lastFetchedClass = selectedClass;
                        fetchSubjects(selectedClass);
                    }
                } else {
                    layoutSubjects.setVisibility(View.GONE); // hide subjects if no class selected
                    chipGroupSubjects.removeAllViews();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });

        String studentId;
        if (getArguments() != null) {
            studentId = getArguments().getString("student_id");
        } else {
            studentId = null;
        }

        etCustomSubjects = view.findViewById(R.id.etCustomSubjects);

        imgProfile = view.findViewById(R.id.imgProfile);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(view1 -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);
            bottomNav.setSelectedItemId(R.id.nav_home);
        });
        if (studentId != null) {
            fetchStudentDetails(studentId);
        }

        btnSubmit.setOnClickListener(view1 -> {
            if (studentId != null) {
                updateStudentDetail(studentId); // if editing
            } else {
                saveStudentDetail(); // if adding new
            }
        });

        return view;
    }

    private void updateStudentDetail(String studentId) {
        sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        // Collect subjects like in saveStudentDetail()
        if (etCustomSubjects.getVisibility() == View.VISIBLE) {
            subjects = etCustomSubjects.getText().toString().trim();
        } else {
            List<String> selectedSubjects = new ArrayList<>();
            for (int i = 0; i < chipGroupSubjects.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupSubjects.getChildAt(i);
                if (chip.isChecked()) {
                    selectedSubjects.add(chip.getText().toString());
                }
            }
            subjects = android.text.TextUtils.join(",", selectedSubjects);
        }

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                if (obj.getBoolean("success")) {
                    Toast.makeText(getContext(), "Student updated successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new HomeFragment())
                            .commit();
                } else {
                    Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e("VolleyError", error.toString());
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "update_student");
                params.put("admin_id", adminId);
                params.put("student_id", studentId);
                params.put("full_name", etFullName.getText().toString().trim());
                params.put("email", etEmail.getText().toString().trim());
                params.put("contact", etContact.getText().toString().trim());
                params.put("dob", etDOB.getText().toString().trim());
                params.put("gender", gender);
                params.put("parent_name", etParentName.getText().toString().trim());
                params.put("parent_contact", etParentContact.getText().toString().trim());
                params.put("class", etClass.getSelectedItem().toString());
                params.put("subjects", subjects);


                if (selectedProfileBitmap != null) {
                    params.put("image", encodeImageToBase64(selectedProfileBitmap));
                } else {
                    params.put("image", "");
                }

                return params;
            }

        };

        if (queue == null) {
            queue = Volley.newRequestQueue(requireContext());
        }
        queue.add(request);
    }


    private void fetchStudentDetails(String studentId) {
        sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                if (obj.getBoolean("success")) {
                    JSONObject student = obj.getJSONObject("student");

                    // Prefill fields
                    etFullName.setText(student.getString("name"));
                    etEmail.setText(student.getString("email"));
                    etContact.setText(student.getString("phone"));
                    etDOB.setText(student.getString("dob"));
                    etParentName.setText(student.getString("parent_name"));
                    etParentContact.setText(student.getString("parent_phone"));

                    // Gender
                    String genderVal = student.getString("gender");
                    if (genderVal.equalsIgnoreCase("Male")) rbMale.setChecked(true);
                    else if (genderVal.equalsIgnoreCase("Female")) rbFemale.setChecked(true);
                    else rbOther.setChecked(true);

                    // Class
                    String classVal = student.getString("class");
                    for (int i = 0; i < etClass.getCount(); i++) {
                        if (etClass.getItemAtPosition(i).toString().equals(classVal)) {
                            etClass.setSelection(i);
                            break;
                        }
                    }

                    // Subjects (comma separated)
                    String subjectStr = student.getString("subjects");
                    preSelectedSubjects.clear();
                    if (subjectStr != null && !subjectStr.isEmpty()) {
                        String[] subjectArr = subjectStr.split(",");
                        for (String s : subjectArr) {
                            preSelectedSubjects.add(s.trim());
                        }
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e("VolleyError", error.toString());
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "get_student_details");
                params.put("student_id", studentId);
                params.put("admin_id", adminId);
                Log.d("param", "POST Params: " + params);
                return params;
            }
        };

        if (queue == null) {
            queue = Volley.newRequestQueue(requireContext());
        }
        queue.add(request);
    }

    private void saveStudentDetail() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String parentName = etParentName.getText().toString().trim();
        String parentContact = etParentContact.getText().toString().trim();

// ===== VALIDATIONS =====
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

// Validate mobile number format (Indian pattern)
        String mobilePattern = "^[6-9]\\d{9}$";

        if (!contact.matches(mobilePattern)) {
            etContact.setError("Enter a valid 10-digit student number");
            etContact.requestFocus();
            return;
        }

        if (!parentContact.matches(mobilePattern)) {
            etParentContact.setError("Enter a valid 10-digit parent number");
            etParentContact.requestFocus();
            return;
        }

// Check student & parent numbers are not the same
        if (contact.equals(parentContact)) {
            Toast.makeText(getContext(), "Student and Parent numbers cannot be the same", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dob.isEmpty()) {
            etDOB.setError("Select Date of Birth");
            etDOB.requestFocus();
            return;
        }

        if (parentName.isEmpty()) {
            etParentName.setError("Parent name is required");
            etParentName.requestFocus();
            return;
        }

        if (etClass.getSelectedItem().toString().equals("Select Class")) {
            Toast.makeText(getContext(), "Please select class", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gender == null) {
            Toast.makeText(getContext(), "Select Gender", Toast.LENGTH_SHORT).show();
            return;
        }


        // If all validations pass, then send request
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject obj = null;
            try {
                obj = new JSONObject(response);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            try {
                if (obj.getBoolean("success")) {
                    Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
                }
                if (!obj.getBoolean("success")) {
                    Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, volleyError -> {
            Log.e("VolleyError", volleyError.toString());

        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "add_student");

                SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String adminId = sp.getString("admin_id", "");
                params.put("admin_id", adminId);
                params.put("gender", gender);
                params.put("full_name", fullName);
                params.put("email", email);
                params.put("contact", contact);
                params.put("dob", dob);
                params.put("parent_name", parentName);
                params.put("parent_contact", parentContact);
                params.put("subjects", subjects);
                params.put("class", etClass.getSelectedItem().toString());

                // add encoded image
                if (selectedProfileBitmap != null) {
                    params.put("image", encodeImageToBase64(selectedProfileBitmap));
                } else {
                    params.put("image", ""); // empty if not selected
                }
                return params;
        }
        };
        if (queue == null) {
            queue = Volley.newRequestQueue(requireContext());
        }
        queue.add(request);
    }

    private void fetchSubjects(String studentClass) {
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                if (obj.getBoolean("success")) {
                    JSONArray arr = obj.getJSONArray("subjects");

                    // Clear old chips
                    chipGroupSubjects.removeAllViews();

                    for (int i = 0; i < arr.length(); i++) {
                        String subject = arr.getString(i);

                        Chip chip = new Chip(requireContext());
                        chip.setText(subject);
                        chip.setChipBackgroundColorResource(R.color.tt_chip);
                        chip.setTextColor(Color.BLACK);
                        chip.setChipStrokeWidth(1f);
                        chip.setChipStrokeColorResource(R.color.tt_primary);
                        chip.setChipCornerRadius(25f);
                        chip.setCheckable(true);
                        chip.setClickable(true);

                        // Pre-select if it was previously selected
                        if (preSelectedSubjects.contains(subject)) {
                            chip.setChecked(true);
                        }

                        chipGroupSubjects.addView(chip);
                    }


                    //Show subjects layout
                    layoutSubjects.setVisibility(View.VISIBLE);

                } else {
                    Toast.makeText(getContext(), "No subjects found", Toast.LENGTH_SHORT).show();
                    layoutSubjects.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e("VolleyError", error.toString());
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "get_subjects");
                params.put("admin_id", adminId);
                params.put("class", studentClass);
                return params;
            }
        };

        if (queue == null) {
            queue = Volley.newRequestQueue(requireContext());
        }
        queue.add(request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            try {
                Uri imageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int newSize = Math.min(width, height);

                int xOffset = (width - newSize) / 2;
                int yOffset = (height - newSize) / 2;

                Bitmap squareBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, newSize, newSize);

                selectedProfileBitmap = squareBitmap; // save for upload
                imgProfile.setImageBitmap(squareBitmap); // preview
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private String encodeImageToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

}