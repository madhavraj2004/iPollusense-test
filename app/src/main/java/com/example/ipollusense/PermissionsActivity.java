package com.example.ipollusense;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private Switch locationPermissionSwitch;
    private Switch bluetoothPermissionSwitch;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        locationPermissionSwitch = findViewById(R.id.locationPermissionSwitch);
        bluetoothPermissionSwitch = findViewById(R.id.bluetoothPermissionSwitch);
        nextButton = findViewById(R.id.nextButton);

        checkAndRequestPermissions();

        nextButton.setOnClickListener(v -> {
            if (arePermissionsGranted()) {
                proceedToMainActivity();
            }
        });
    }

    private void checkAndRequestPermissions() {
        if (!arePermissionsGranted()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                    },
                    PERMISSION_REQUEST_CODE);
        } else {
            updateUIForPermissionsGranted();
        }
    }

    private boolean arePermissionsGranted() {
        return isLocationPermissionGranted() && isBluetoothPermissionGranted();
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isBluetoothPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateUIForPermissionsGranted() {
        locationPermissionSwitch.setChecked(isLocationPermissionGranted());
        bluetoothPermissionSwitch.setChecked(isBluetoothPermissionGranted());
        nextButton.setEnabled(arePermissionsGranted());
    }

    private void proceedToMainActivity() {
        Intent intent = new Intent(PermissionsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            updateUIForPermissionsGranted();
        }
    }
}
