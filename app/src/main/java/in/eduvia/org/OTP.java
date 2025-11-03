package in.eduvia.org;

import static in.eduvia.org.SignUp.BASE_URL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OTP extends AppCompatActivity {
    EditText et1, et2, et3, et4;
    Button btnVerifyOtp;
    ProgressDialog progressDialog;
    RequestQueue queue;
    TextView tvResendOtp;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        et1 = findViewById(R.id.etOtp1);
        et2 = findViewById(R.id.etOtp2);
        et3 = findViewById(R.id.etOtp3);
        et4 = findViewById(R.id.etOtp4);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvResendOtp = findViewById(R.id.tvResendOtp); // moved inside onCreate

        queue = Volley.newRequestQueue(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.setCancelable(false);

        email = getIntent().getStringExtra("email");

        moveToNext(et1, et2);
        moveToNext(et2, et3);
        moveToNext(et3, et4);

        // Verify OTP
        btnVerifyOtp.setOnClickListener(v -> {
            String otp = et1.getText().toString() +
                    et2.getText().toString() +
                    et3.getText().toString() +
                    et4.getText().toString();

            if (otp.length() == 4) {
                verifyOtp(email, otp);
            } else {
                Toast.makeText(this, "Enter full 4-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });

        // Resend OTP
        tvResendOtp.setOnClickListener(v -> {
            resendOtp(email);
            startResendCountdown();
        });

        // Start countdown immediately on screen open
        startResendCountdown();
    }

    private void moveToNext(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) next.requestFocus();
            }
        });
    }

    private void verifyOtp(String email, String otp) {
        progressDialog.show();

        StringRequest sr = new StringRequest(Request.Method.POST, BASE_URL + "otp.php",
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);
                        Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();

                        if (obj.getBoolean("success")) {
                            // OTP verified, navigate to reset password screen
                            Intent intent = new Intent(OTP.this, ResetPassword.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("action","verify_otp_admin");
                params.put("input", email);
                params.put("otp", otp);
                return params;
            }
        };

        queue.add(sr);
    }

    // 🔹 Resend OTP API call
    private void resendOtp(String email) {
        StringRequest sr = new StringRequest(Request.Method.POST, BASE_URL + "otp.php",
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("action","send_otp_admin"); // resend action
                params.put("input", email);
                return params;
            }
        };

        queue.add(sr);
    }

    // Function to start the countdown
    private void startResendCountdown() {
        tvResendOtp.setEnabled(false); // disable click
        new CountDownTimer(60000, 1000) { // 1 minute (60 sec)

            public void onTick(long millisUntilFinished) {
                tvResendOtp.setText("Resend OTP in " + millisUntilFinished / 1000 + "s");
                tvResendOtp.setTextColor(Color.GRAY); // show disabled color
            }

            public void onFinish() {
                tvResendOtp.setText("Resend OTP");
                tvResendOtp.setTextColor(getResources().getColor(R.color.tt_primary));
                tvResendOtp.setEnabled(true); // enable again
            }
        }.start();
    }
}
