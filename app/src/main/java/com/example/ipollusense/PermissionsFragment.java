package com.example.ipollusense;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionsFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private Switch bluetoothSwitch;
    private Switch locationSwitch;
    private Switch internetSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);

        bluetoothSwitch = view.findViewById(R.id.bluetooth_permission_switch);
        locationSwitch = view.findViewById(R.id.location_permission_switch);
        internetSwitch = view.findViewById(R.id.internet_permission_switch);

        checkPermissions();

        bluetoothSwitch.setOnClickListener(v -> {
            if (!bluetoothSwitch.isChecked()) {
                requestPermission(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN);
            }
        });

        locationSwitch.setOnClickListener(v -> {
            if (!locationSwitch.isChecked()) {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
            }
        });

        internetSwitch.setOnClickListener(v -> {
            if (!internetSwitch.isChecked()) {
                requestPermission(Manifest.permission.INTERNET);
            }
        });

        return view;
    }

    private void checkPermissions() {
        bluetoothSwitch.setChecked(
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        );
        updateSwitchColor(bluetoothSwitch);

        locationSwitch.setChecked(
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        );
        updateSwitchColor(locationSwitch);

        internetSwitch.setChecked(
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
        );
        updateSwitchColor(internetSwitch);
    }

    private void updateSwitchColor(Switch permissionSwitch) {
        if (permissionSwitch.isChecked()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionSwitch.setThumbTintList(ContextCompat.getColorStateList(getContext(), android.R.color.holo_blue_light));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionSwitch.setTrackTintList(ContextCompat.getColorStateList(getContext(), android.R.color.holo_blue_light));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionSwitch.setThumbTintList(ContextCompat.getColorStateList(getContext(), android.R.color.darker_gray));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionSwitch.setTrackTintList(ContextCompat.getColorStateList(getContext(), android.R.color.darker_gray));
            }
        }
    }

    private void requestPermission(String... permissions) {
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(getContext(), "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permissions denied", Toast.LENGTH_SHORT).show();
            }
            checkPermissions();
        }
    }
}
