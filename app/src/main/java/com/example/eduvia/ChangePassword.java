package com.example.eduvia;

import static android.content.Context.MODE_PRIVATE;
import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangePassword extends Fragment {

EditText old_pass,new_pass,confirm_pass;
TextView forgot_pass,tvResponseMessage;
Button submit;
SharedPreferences sp;
String url = BASE_URL + "auth.php";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_change_password, container, false);
        // Initialize the views
        old_pass = v.findViewById(R.id.etOldPassword);
        new_pass = v.findViewById(R.id.etNewPassword);
        confirm_pass = v.findViewById(R.id.etConfirmPassword);
        forgot_pass = v.findViewById(R.id.forgot_password);
        submit = v.findViewById(R.id.submitBtn);
        tvResponseMessage = v.findViewById(R.id.tvResponseMessage);

        forgot_pass.setOnClickListener(view->{
            startActivity(new Intent(getContext(), ForgotPassword.class));
        });
        // Set click listeners for the buttons
        submit.setOnClickListener(view->{

            String old_pass_str = old_pass.getText().toString();
            String new_pass_str = new_pass.getText().toString();
            String confirm_pass_str = confirm_pass.getText().toString();

            if(old_pass.getText().toString().isEmpty()){
                old_pass.setError("Enter old password");
                return;
            }
            if(new_pass.getText().toString().isEmpty()){
                new_pass.setError("Enter new password");
                return;
                }
            if(confirm_pass.getText().toString().isEmpty()){
                confirm_pass.setError("Enter confirm password");
                return;
            }
            if(!new_pass.getText().toString().equals(confirm_pass.getText().toString())){
                confirm_pass.setError("Password does not match");
                return;
            }

                ChangePass(old_pass_str, new_pass_str, confirm_pass_str);

        });
        return v;
    }

    private void ChangePass(String oldPassStr, String newPassStr, String confirmPassStr) {
        sp = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String adminId = sp.getString("admin_id", "");
        String email = sp.getString("email", "");

        StringRequest sr = new StringRequest(Request.Method.POST,url,response->{
            try {
                JSONObject jo = new JSONObject(response);
                if(jo.getBoolean("success")){
                    Toast.makeText(requireActivity(), jo.getString("message"), Toast.LENGTH_LONG).show();
                    old_pass.setText("");
                    new_pass.setText("");
                    confirm_pass.setText("");
                   // Back pop
                    requireActivity().onBackPressed();


                }else{
                    Handler handler = new Handler();
                    tvResponseMessage.setTextColor(Color.RED);
                    tvResponseMessage.setVisibility(View.VISIBLE);
                    tvResponseMessage.setText(jo.getString("message"));
                    handler.postDelayed(() -> {
                        tvResponseMessage.setVisibility(View.GONE);
                    }, 3000);

                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
        ,error->{

        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("action","change_password");
                params.put("admin_id",adminId);
                params.put("email",email);
                params.put("old_pass",oldPassStr);
                params.put("new_pass",newPassStr);
                params.put("confirm_pass",confirmPassStr);
                return params;
            }
        };
            Volley.newRequestQueue(requireActivity()).add(sr);
    }
}