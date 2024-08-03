package com.example.ipollusense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String PREF_KEY_USER_TOKEN = "user_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FirebaseApp.initializeApp(this);

        // Check login status
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userToken = sharedPreferences.getString(PREF_KEY_USER_TOKEN, null);

        // Set the next activity based on login status
        Class<?> nextActivity = (userToken != null) ? MainActivity.class : FeaturesActivity.class;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, nextActivity);
                startActivity(intent);
                finish();  // Close SplashActivity
            }
        }, 3000);  // 3-second delay
    }
}
