package com.example.eduvia;

import static com.example.eduvia.SignIn.PREF_NAME;
import static com.example.eduvia.SignUp.BASE_URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Loader loader;

    private MaterialToolbar toolbar;
    private ImageView profileImage;
    private BottomNavigationView bottomNav;

    public boolean isLoggedIn = false;
    private final String url = BASE_URL + "fetchData.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        // Init Views
        toolbar = findViewById(R.id.toolbar);
        profileImage = findViewById(R.id.profileImage);
        bottomNav = findViewById(R.id.bottomNav);
        loader = new Loader(this);
        setSupportActionBar(toolbar);

        profileImage.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Profile())
                    .addToBackStack(null)
                    .commit();
        });


        // Transparent status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Login check
        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean loggedIn = sp.getBoolean("isLoggedIn", false);
        String adminId = sp.getString("admin_id", "");

        if (!loggedIn) {
            startActivity(new Intent(this, SignIn.class));
            finish();
            return;
        }

        onLoginSuccess();
        fetchDetails(adminId);

        // BottomNav setup
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return show(new HomeFragment());
            if (id == R.id.nav_student) return show(new StudentFragment());
            if (id == R.id.nav_subject) return show(new SubjectsFragment());
            if (id == R.id.nav_fees) return show(new FeesFragment());
            if (id == R.id.nav_profile) return show(new Profile());
            return false;
        });

        // Backstack listener for toolbar navigation icon
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.container);
            if (current instanceof HomeFragment) {
                showBars();
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            } else {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        });
    }

    private void fetchDetails(String adminId) {
        loader.show();
        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> {
                    loader.dismiss();
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONObject data = json.getJSONObject("data");

                            // Load profile image
                            String profileImg = data.optString("profile_img", "");
                            if (!profileImg.isEmpty()) {
                                String fullUrl = profileImg.startsWith("http") ?
                                        profileImg : BASE_URL + profileImg;

                                Glide.with(this)
                                        .load(fullUrl)
                                        .placeholder(R.drawable.user_profile)
                                        .into(profileImage);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "profileDetails");
                params.put("admin_id", adminId);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(sr);
    }

    public void onLoginSuccess() {
        isLoggedIn = true;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new HomeFragment())
                .commit();
        showBars();
        bottomNav.setSelectedItemId(R.id.nav_home);
        invalidateOptionsMenu();
    }

    private boolean show(@NonNull Fragment fragment) {
        // For BottomNav, no backstack
        showFragment(fragment, false);
        return true;
    }

    private void showFragment(Fragment fragment, boolean addToBackStack) {
        var transaction = getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, fragment);

        transaction.commit();
    }

    void hideBars() {
        toolbar.setVisibility(View.GONE);
        bottomNav.setVisibility(View.GONE);
        profileImage.setVisibility(View.GONE);
    }

    private void showBars() {
        toolbar.setVisibility(View.VISIBLE);
        bottomNav.setVisibility(View.VISIBLE);
        profileImage.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }
    public void updateToolbarImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.user_profile)
                .into(profileImage);

        // Also save in SharedPreferences (for persistence)
        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        sp.edit().putString("profile_img", imageUrl).apply();
    }
}