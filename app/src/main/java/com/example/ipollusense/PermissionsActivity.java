package com.example.ipollusense;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class PermissionsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private SwitchMaterial bluetoothSwitch;
    private SwitchMaterial locationSwitch;
    private SwitchMaterial internetSwitch;
    private MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        bluetoothSwitch = findViewById(R.id.bluetooth_permission_switch);
        locationSwitch = findViewById(R.id.location_permission_switch);
        internetSwitch = findViewById(R.id.internet_permission_switch);
        nextButton = findViewById(R.id.nextButton);

        setupSwitches();
        checkPermissions();
        setupNextButton();
    }

    private void setupSwitches() {
        bluetoothSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestBluetoothPermissions();
            } else {
                updateNextButtonState();
            }
        });

        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestLocationPermissions();
            } else {
                updateNextButtonState();
            }
        });

        internetSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestInternetPermission();
            } else {
                updateNextButtonState();
            }
        });
    }

    private void setupNextButton() {
        nextButton.setOnClickListener(v -> {
            if (bluetoothSwitch.isChecked() && locationSwitch.isChecked() && internetSwitch.isChecked()) {
                // All permissions and switches are enabled, navigate to MainActivity
                startActivity(new Intent(PermissionsActivity.this, MainActivity.class));
                finish();// Optional: Finish PermissionsActivity
            } else {
                Snackbar.make(nextButton, "Please allow all permissions and enable all switches.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }


    private void checkPermissions() {
        bluetoothSwitch.setChecked(hasBluetoothPermissions());
        locationSwitch.setChecked(hasLocationPermissions());
        internetSwitch.setChecked(hasInternetPermission());

        updateNextButtonState();
    }

    private boolean hasBluetoothPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasInternetPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                PERMISSION_REQUEST_CODE);
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    private void requestInternetPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INTERNET},
                PERMISSION_REQUEST_CODE);
    }

    private void updateNextButtonState() {
        boolean allPermissionsGranted = hasBluetoothPermissions() && hasLocationPermissions() && hasInternetPermission();
        nextButton.setEnabled(allPermissionsGranted);

        if (allPermissionsGranted) {
            nextButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
        } else {
            nextButton.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if all permissions are granted
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            // Update switches and next button state
            if (allGranted) {
                checkPermissions(); // Refresh switches
            } else {
                Snackbar.make(nextButton, "Permissions denied. Please allow all permissions to proceed.", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
