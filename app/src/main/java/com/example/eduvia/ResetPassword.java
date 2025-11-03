package com.example.eduvia;

import static com.example.eduvia.SignUp.BASE_URL;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResetPassword extends AppCompatActivity {

    private static final String url = BASE_URL + "otp.php";
    RequestQueue queue;
    private EditText etNewPassword, etConfirmPassword;
    private Button submitBtn;
    private TextView tvResponseMessage, tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);


        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        submitBtn = findViewById(R.id.submitBtn);
        tvResponseMessage = findViewById(R.id.tvResponseMessage);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvBackToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
        });

        queue = Volley.newRequestQueue(this);

        submitBtn.setOnClickListener(view -> {
            String email = getIntent().getStringExtra("email");
            resetPassword(email);
        });
    }

    private void resetPassword(String email) {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Enter new password");
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Enter confirm password");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Send to server
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean success = obj.getBoolean("success");
                        String message = obj.getString("message");

                        tvResponseMessage.setVisibility(View.VISIBLE);
                        if (success) {
                            tvResponseMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            tvResponseMessage.setText(message);
                            Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                tvResponseMessage.setVisibility(View.GONE);
                                finish();
                                Intent intent = new Intent(this, SignIn.class);
                                startActivity(intent);
                            }, 2000);
                        } else {
                            tvResponseMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parse error!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "reset_password_admin");
                params.put("input", email);
                params.put("password", newPassword);
                return params;
            }
        };

        Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }
}