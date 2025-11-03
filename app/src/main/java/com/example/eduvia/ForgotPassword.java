package com.example.eduvia;

import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPassword extends AppCompatActivity {

    EditText email;
    TextView go_back;
    Button sendOtp;
    SharedPreferences sp;
    RequestQueue queue;
    String url = BASE_URL + "otp.php";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        email = findViewById(R.id.etemail);
        sendOtp = findViewById(R.id.otp_btn);
        go_back = findViewById(R.id.go_back);

        queue = Volley.newRequestQueue(this);
        sp = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);

        go_back.setOnClickListener(view -> onBackPressed());

        sendOtp.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            if (emailText.isEmpty()) {
                email.setError("Email cannot be empty");
                email.requestFocus();
                return;
            }

            String admin_id = sp.getString("admin_id", "");
            OtpGenerate(admin_id, emailText);
        });
    }
    private void OtpGenerate(String adminId, String emailText) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);
                        Toast.makeText(ForgotPassword.this, obj.getString("message"), Toast.LENGTH_SHORT).show();

                        if (obj.getBoolean("success")) {
                            // OTP sent successfully, navigate to OTP screen
                            Intent intent = new Intent(ForgotPassword.this, OTP.class);
                            intent.putExtra("email", emailText); // send email to OTP screen
                            startActivity(intent);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ForgotPassword.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }

                }, error -> {
            progressDialog.dismiss();
            // Volley error may have null message, show network error instead
            Toast.makeText(ForgotPassword.this, "Network error! Please try again.", Toast.LENGTH_SHORT).show();

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "send_otp_admin");
                params.put("admin_id", adminId);
                params.put("input", emailText);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(sr);

        sr.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


    }
}
