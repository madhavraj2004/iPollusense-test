package com.example.ipollusense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        TextView profileName = findViewById(R.id.profile_name);
        TextView profileEmail = findViewById(R.id.profile_email);
        Button logoutButton = findViewById(R.id.logout_button);

        // Set profile details (retrieve from SharedPreferences or other source)
        profileName.setText("User Name"); // Replace with actual data
        profileEmail.setText("user@example.com"); // Replace with actual data

        // Handle logout button click
        logoutButton.setOnClickListener(v -> {
            // Clear user data and navigate to login
            MyApplication.getInstance().clearUserData(); // Ensure this method is implemented in MyApplication
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Close ProfileActivity
        });
    }
}
