package com.example.eduvia;


import static com.example.eduvia.SignUp.BASE_URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignIn extends AppCompatActivity {

    public static final String PREF_NAME = "login_pref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    Button login_btn;
    TextView register_btn, forgot_password;
    EditText etEmail, etPassword;
    String url = BASE_URL + "auth.php";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        login_btn = findViewById(R.id.login_btn);
        register_btn = findViewById(R.id.register_btn);
        etEmail = findViewById(R.id.etemail);
        etPassword = findViewById(R.id.etpassword);
        forgot_password = findViewById(R.id.forgot_password);
        forgot_password.setOnClickListener(v -> {
            startActivity(new Intent(SignIn.this, ForgotPassword.class));
        });

        // init SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 🔹 Auto login check
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            navigateToMain();
            return;
        }

        login_btn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required!");
                etEmail.requestFocus();
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required!");
                etPassword.requestFocus();
            } else {
                loginUser(email, password);
            }
        });

        register_btn.setOnClickListener(v -> {
            startActivity(new Intent(SignIn.this, SignUp.class));
        });
    }

    private void loginUser(String email, String password) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("response", response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            JSONObject userObj = obj.getJSONObject("user");

                            // Save login state + user details
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(KEY_IS_LOGGED_IN, true);
                            editor.putString("admin_id", userObj.getString("id"));
                            editor.putString("name", userObj.getString("name"));
                            editor.putString("email", userObj.getString("email"));
                            editor.putString("role", userObj.getString("qualification"));
                            editor.apply();

                            navigateToMain();
                        } else {
                            Toast.makeText(SignIn.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SignIn.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    Toast.makeText(SignIn.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "login_admin");
                params.put("email", email);
                params.put("password", password);
                Log.d("param", "POST Params: " + params);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(SignIn.this);
        requestQueue.add(stringRequest);
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignIn.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
