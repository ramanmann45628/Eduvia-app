package in.eduvia.org;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static in.eduvia.org.SignIn.PREF_NAME;
import static in.eduvia.org.SignUp.BASE_URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends Fragment {
    public static final int PICK_IMAGE = 1;

    EditText full_name, email, phone, role;
    Bitmap bitmap;
    Button save_btn, cancel_btn, upload_img, save_btn_img;
    ShapeableImageView profile_image;
    SharedPreferences sp;

    String url = BASE_URL + "admin_profile.php";

    String originalName, originalPhone, originalRole;
    Loader loader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Initialize views
        full_name = view1.findViewById(R.id.full_name);
        email = view1.findViewById(R.id.email);
        email.setEnabled(false);
        phone = view1.findViewById(R.id.phone);
        role = view1.findViewById(R.id.role);
        save_btn = view1.findViewById(R.id.save_button);
        cancel_btn = view1.findViewById(R.id.cancel_button);

        upload_img = view1.findViewById(R.id.upload_img);
        save_btn_img = view1.findViewById(R.id.save_btn_img);
        profile_image = view1.findViewById(R.id.profile_image); // ✅ initialize
        loader = new Loader(getContext());
        // Save button disabled by default
        save_btn.setEnabled(false);
        save_btn.setAlpha(0.5f);

        cancel_btn.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        // Add text change listeners
        addTextWatchers();

        save_btn.setOnClickListener(view -> {
            if (full_name.getText().toString().isEmpty()) {
                full_name.setError("Enter your full name");
                full_name.requestFocus();
                return;
            }
            if (phone.getText().toString().isEmpty()) {
                phone.setError("Enter your phone number");
                phone.requestFocus();
                return;
            }
            if (role.getText().toString().isEmpty()) {
                role.setError("Enter your role");
                role.requestFocus();
                return;
            }

            updateAdminProfile(
                    full_name.getText().toString(),
                    phone.getText().toString(),
                    role.getText().toString()
            );
        });

        upload_img.setOnClickListener(view -> {
            // Open gallery
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        save_btn_img.setOnClickListener(v -> {
            if (bitmap != null) {
                uploadProfileImage(bitmap);
            } else {
                Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });


        fetch_data();
        return view1;
    }

    private void uploadProfileImage(Bitmap bitmap) {
        loader.show();
        sp = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        // Convert bitmap to Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> {
                    loader.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Image Updated Successfully", Toast.LENGTH_SHORT).show();

                            String profileUrl = jsonObject.getJSONArray("profileImage").getString(0);
                            String fullUrl = BASE_URL + profileUrl;

                            // Save in SharedPreferences so MainActivity can access it
                            sp = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                            sp.edit().putString("profile_img", fullUrl).apply();

                            // Update current fragment image
                            Glide.with(requireContext())
                                    .load(fullUrl)
                                    .placeholder(R.drawable.user_profile)
                                    .into(profile_image);

                            // Notify MainActivity immediately
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).updateToolbarImage(fullUrl);
                            }
                        } else {
                            Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "update_admin_image");
                params.put("admin_id", adminId);
                params.put("image", encodedImage);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(sr);
    }


    private void addTextWatchers() {
        View.OnFocusChangeListener listener = (v, hasFocus) -> checkIfChanged();
        full_name.setOnFocusChangeListener(listener);
        phone.setOnFocusChangeListener(listener);
        role.setOnFocusChangeListener(listener);

        full_name.addTextChangedListener(new SimpleWatcher(this::checkIfChanged));
        phone.addTextChangedListener(new SimpleWatcher(this::checkIfChanged));
        role.addTextChangedListener(new SimpleWatcher(this::checkIfChanged));
    }

    private void checkIfChanged() {
        boolean changed =
                !full_name.getText().toString().equals(originalName) ||
                        !phone.getText().toString().equals(originalPhone) ||
                        !role.getText().toString().equals(originalRole);

        save_btn.setEnabled(changed);
        save_btn.setAlpha(changed ? 1f : 0.5f);
    }

    private void updateAdminProfile(String s, String s2, String s3) {
        loader.show();
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> {
                        loader.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();

                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("role", s3);
                            editor.apply();

                            originalName = s;
                            originalPhone = s2;
                            originalRole = s3;
                            checkIfChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "update_admin_profile");
                params.put("admin_id", adminId);
                params.put("name", s);
                params.put("phone", s2);
                params.put("qualification", s3); // role field maps to qualification
                return params;
            }
        };
        Volley.newRequestQueue(requireContext()).add(sr);
    }

    private void fetch_data() {
        loader.show();
        SharedPreferences sp = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");

        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> {

                    loader.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            originalName = data.getString("name");
                            originalPhone = data.getString("phone");
                            originalRole = data.getString("qualification");

                            full_name.setText(originalName);
                            email.setText(data.getString("email"));
                            phone.setText(originalPhone);
                            role.setText(originalRole);

                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("role", originalRole);
                            editor.apply();

                            // ✅ Load profile image if exists
                            if (data.has("profile_img") && !data.isNull("profile_img")) {
                                String profileImg = data.getString("profile_img");
                                if (!profileImg.isEmpty()) {
                                    Glide.with(requireContext())
                                            .load(BASE_URL + profileImg)
                                            .placeholder(R.drawable.user_profile)
                                            .into(profile_image);
                                }
                            }

                            checkIfChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "fetch_data");
                params.put("admin_id", adminId);
                return params;
            }
        };
        Volley.newRequestQueue(requireContext()).add(sr);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                profile_image.setImageBitmap(bitmap); // Preview selected image
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
