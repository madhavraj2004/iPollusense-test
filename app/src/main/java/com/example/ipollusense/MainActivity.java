package com.example.ipollusense;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    // Define your navigation IDs as constants
    private static final int NAVIGATION_HOME = R.id.navigation_home;
    private static final int NAVIGATION_DASHBOARD = R.id.navigation_dashboard;
    private static final int NAVIGATION_NOTIFICATIONS = R.id.navigation_notifications;
    private static final int NAVIGATION_BLUETOOTH = R.id.navigation_bluetooth;
    private static final int NAVIGATION_MAP = R.id.navigation_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navigation_dashboard) {
                    selectedFragment = new DashboardFragment();
                } else if (itemId == R.id.navigation_notifications) {
                    selectedFragment = new NotificationsFragment();
                } else if (itemId == R.id.navigation_bluetooth) {
                    selectedFragment = new BluetoothFragment();
                } else if (itemId == R.id.navigation_map) {
                    selectedFragment = new MapsFragment();  // Example for MapsFragment
                }


                if (selectedFragment != null) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment, selectedFragment)
                            .commit();
                    return true;
                }

                return false;
            }
        });

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }
}
