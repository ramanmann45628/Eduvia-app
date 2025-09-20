package com.example.eduvia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash_Screen extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds
    ImageView splashLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Delay then move to LoginActivity
        new Handler().postDelayed(() -> {
            // start main activity
            Intent intent = new Intent(Splash_Screen.this, MainActivity.class);
           // Intent intent = new Intent(Splash_Screen.this, Login.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME_OUT);
    }
}