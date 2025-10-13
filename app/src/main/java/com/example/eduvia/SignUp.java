package com.example.eduvia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.android.volley.DefaultRetryPolicy;
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
    TextView login, showMessage, tvResendOtp;
    ProgressDialog progressDialog;
    EditText etName, etEmail, etPassword, etConfirmPassword;
    EditText etOtp1, etOtp2, etOtp3, etOtp4;
    Button register, btnVerifyEmail;
    ImageView ivVerified;
    LinearLayout otpLayout;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        queue = Volley.newRequestQueue(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);

        initViews();
        setupOtpInputs();
        initListeners();
    }

    private void initViews() {
        login = findViewById(R.id.login_btn);
        etName = findViewById(R.id.etname);
        etEmail = findViewById(R.id.etemail);
        etPassword = findViewById(R.id.etpassword);
        etConfirmPassword = findViewById(R.id.etconfirmpassword);
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        register = findViewById(R.id.register_btn);
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail);
        ivVerified = findViewById(R.id.ivVerified);
        otpLayout = findViewById(R.id.otp_layout);
        showMessage = findViewById(R.id.show_message);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        otpLayout.setVisibility(View.GONE);
        register.setEnabled(false);
        ivVerified.setVisibility(View.GONE);
    }

    private void initListeners() {
        login.setOnClickListener(v -> finish());

        btnVerifyEmail.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (name.isEmpty()) { etName.setError("Name is required"); etName.requestFocus(); return; }
            if (email.isEmpty()) { etEmail.setError("Email is required"); etEmail.requestFocus(); return; }

            sendOtp(name, email);
            otpLayout.setVisibility(View.VISIBLE);
            btnVerifyEmail.setText("Check");
        });

        register.setOnClickListener(v -> registerAdmin());

        tvResendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if(!email.isEmpty()) {
                resendOtp(etName.getText().toString().trim(), email);
                startResendCountdown();
            } else {
                etEmail.setError("Enter email first");
                etEmail.requestFocus();
            }
        });
    }

    private void setupOtpInputs() {
        etOtp1.addTextChangedListener(new OtpTextWatcher(etOtp1, etOtp2));
        etOtp2.addTextChangedListener(new OtpTextWatcher(etOtp2, etOtp3));
        etOtp3.addTextChangedListener(new OtpTextWatcher(etOtp3, etOtp4));
        etOtp4.addTextChangedListener(new OtpTextWatcher(etOtp4, null));
    }

    private class OtpTextWatcher implements TextWatcher {
        private final EditText current;
        private final EditText next;

        public OtpTextWatcher(EditText current, EditText next) {
            this.current = current;
            this.next = next;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1) {
                if (next != null) {
                    next.requestFocus();
                } else {
                    String otp = etOtp1.getText().toString() +
                            etOtp2.getText().toString() +
                            etOtp3.getText().toString() +
                            etOtp4.getText().toString();
                    verifyOtp(etEmail.getText().toString().trim(), otp);
                }
            }
        }
    }

    private void showMessage(String message, boolean success){
        showMessage.setText(message);
        showMessage.setTextColor(success ? Color.GREEN : Color.RED);
        showMessage.setVisibility(View.VISIBLE);
    }

    private void sendOtp(String name, String email) {
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "otp.php",
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        showMessage(jsonObject.getString("message"), jsonObject.getBoolean("success"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showMessage("Response parsing error!", false);
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    showMessage("Network Error: " + error, false);
                }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("action", "send_otpCode");
                params.put("name", name);
                params.put("input", email);
                return params;
            }
        };
        queue.add(request);
    }

    private void resendOtp(String name, String email) {
        sendOtp(name, email);
    }

    private void verifyOtp(String email, String otp) {
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "otp.php",
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        String message = jsonObject.getString("message");

                        showMessage(message, success);

                        if(success){
                            otpLayout.setVisibility(View.GONE);
                            btnVerifyEmail.setVisibility(View.GONE);
                            ivVerified.setVisibility(View.VISIBLE);
                            etPassword.setVisibility(View.VISIBLE);
                            etConfirmPassword.setVisibility(View.VISIBLE);
                            register.setVisibility(View.VISIBLE);
                            register.setEnabled(true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showMessage("Response parsing error!", false);
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    showMessage("Network Error: " + error, false);
                }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("action", "verify_otp_admin");
                params.put("input", email);
                params.put("otp", otp);
                return params;
            }
        };
        queue.add(request);

    }

    private void registerAdmin() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if(name.isEmpty()){ etName.setError("Name required"); etName.requestFocus(); return; }
        if(email.isEmpty()){ etEmail.setError("Email required"); etEmail.requestFocus(); return; }
        if(password.isEmpty()){ etPassword.setError("Password required"); etPassword.requestFocus(); return; }
        if(!password.equals(confirmPassword)){ etConfirmPassword.setError("Passwords do not match"); etConfirmPassword.requestFocus(); return; }

        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "otp.php",
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        String message = jsonObject.getString("message");

                        showMessage(message, success);

                        if(success){
                            new Handler().postDelayed(() -> {
                                startActivity(new Intent(SignUp.this, SignIn.class));
                                finish();
                            }, 1500);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showMessage("Response parsing error!", false);
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    showMessage("Network Error: " + error, false);
                }
        ){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("action","register_admin");
                params.put("name",name);
                params.put("email",email);
                params.put("password",password);
                return params;
            }
        };
        queue.add(request);

    }

    private void startResendCountdown() {
        tvResendOtp.setEnabled(false);
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvResendOtp.setText("Resend OTP in " + millisUntilFinished / 1000 + "s");
                tvResendOtp.setTextColor(Color.GRAY);
            }
            public void onFinish() {
                tvResendOtp.setText("Resend OTP");
                tvResendOtp.setTextColor(getResources().getColor(R.color.tt_primary));
                tvResendOtp.setEnabled(true);
            }
        }.start();
    }
}
