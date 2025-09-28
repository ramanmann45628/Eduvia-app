package com.example.eduvia;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.android.volley.BuildConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
//    public static final String BASE_URL = "http://10.0.2.2/tuition_centre/";
    public static final String BASE_URL = "https://eduvia.org.in/";

    TextView login, showMessage;
    EditText etName, etEmail, etPassword, etConfirmPassword;
    Button register, btnVerifyEmail;
    String url = BASE_URL + "auth.php";
    Handler handler = new Handler();
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        queue = Volley.newRequestQueue(this);

        login = findViewById(R.id.login_btn);
        etName = findViewById(R.id.etname);
        etEmail = findViewById(R.id.etemail);
        etPassword = findViewById(R.id.etpassword);
        etConfirmPassword = findViewById(R.id.etconfirmpassword);
        register = findViewById(R.id.register_btn);
        showMessage = findViewById(R.id.show_message);
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail);

        btnVerifyEmail.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            verifyEmailAddress(name,email);
        });

        register.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Name is required");
                etName.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }
            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            // Volley Request
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            String message = jsonObject.getString("message");

                            // Show only our custom message
                            if (message.equals("Admin registered successfully")) {
                                showMessage.setText(message);
                                showMessage.setTextColor(Color.GREEN);
                                showMessage.setVisibility(TextView.VISIBLE);
                                handler.postDelayed(() -> {
                                    showMessage.setVisibility(TextView.INVISIBLE);
                                    etEmail.setText("");
                                    etEmail.requestFocus();
                                    etPassword.setText("");
                                    etConfirmPassword.setText("");
                                    Intent i = new Intent(SignUp.this, SignIn.class);
                                    startActivity(i);
                                    finish();
                                }, 2000);
                            } else {
                                showMessage.setText(message);
                                showMessage.setTextColor(Color.RED);
                                showMessage.setVisibility(TextView.VISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            showMessage.setText("Response parsing error!");
                            showMessage.setTextColor(Color.RED);
                            showMessage.setVisibility(TextView.VISIBLE);
                        }
                        handler.postDelayed(() -> {
                            showMessage.setVisibility(TextView.INVISIBLE);
                            etEmail.setText("");
                            etEmail.requestFocus();
                            etPassword.setText("");
                            etConfirmPassword.setText("");
                        }, 3000);

                    },
                    error -> {
                        Toast.makeText(SignUp.this, "Network Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("action", "register_admin");
                    params.put("name", name);
                    params.put("email", email);
                    params.put("password", password);
                    return params;
                }
            };

//            request.setRetryPolicy(new DefaultRetryPolicy(
//                    15000,
//                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//            ));

            Volley.newRequestQueue(SignUp.this).add(request);
        });

        login.setOnClickListener(v -> {
            finish();
        });
    }

    private void verifyEmailAddress(String name, String email) {
        String url = BASE_URL + "email_verify.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("Response", response);
                },
                error -> {
                    Log.e("VolleyError", "Error: " + error.toString());
                }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}
