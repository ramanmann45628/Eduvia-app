package com.example.eduvia;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Splash_Screen extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000;
    ImageView splashLogo;

    public boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if (!isInternetAvailable(this)) {
            // Show dialog and close app
            new AlertDialog.Builder(this)
                    .setTitle("No Internet Connection")
                    .setMessage("Please connect to the internet to use Eduvia app.")
                    .setCancelable(false)
                    .setPositiveButton("Exit", (dialog, which) -> finish())
                    .show();
        } else {

            // Initialize logo
            splashLogo = findViewById(R.id.splash_logo);

            // Optional fade-in animation for logo
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setDuration(1500);
            splashLogo.startAnimation(fadeIn);

            // Delay then move to MainActivity
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(Splash_Screen.this, MainActivity.class);
                startActivity(intent);
                finish(); // important to prevent splash reopening on back press
            }, SPLASH_TIME_OUT);
        }
    }
}
