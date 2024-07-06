package com.example.ipollusense;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ipollusense.R;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private Handler handler;
    private boolean scanning;
    private static final long SCAN_PERIOD = 10000; // 10 seconds

    private TextView statusTextView;
    private ListView deviceListView;
    private List<BluetoothDevice> devices;
    private ArrayAdapter<BluetoothDevice> deviceListAdapter;

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                // Handle connected state
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
                // Handle disconnected state
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Handle successful service discovery
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Handle successful characteristic read
                try {
                    String jsonString = new String(characteristic.getValue(), "UTF-8");
                    Log.d(TAG, "Received data: " + jsonString);
                    // Update UI with received data
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Error converting bytes to string: " + e.getMessage());
                }
            } else {
                Log.w(TAG, "onCharacteristicRead received: " + status);
            }
        }
    };

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (!devices.contains(device)) {
                devices.add(device);
                deviceListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan failed with error code: " + errorCode);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final BluetoothManager bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        } else {
            Log.e(TAG, "Unable to initialize BluetoothManager.");
        }
        handler = new Handler();
        devices = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, devices);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        statusTextView = view.findViewById(R.id.statusTextView);
        deviceListView = view.findViewById(R.id.listViewDevices);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = devices.get(position);
                connectToDevice(device);
            }
        });

        Button scanButton = view.findViewById(R.id.btn_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDevices();
            }
        });

        return view;
    }

    private void scanDevices() {
        if (!scanning) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, SCAN_PERIOD);
            devices.clear();
            deviceListAdapter.notifyDataSetChanged();
            bluetoothLeScanner.startScan(scanCallback);
            scanning = true;
            statusTextView.setText("Scanning...");
            Log.d(TAG, "Scanning started.");
        } else {
            Log.d(TAG, "Scan already in progress.");
        }
    }

    private void stopScan() {
        if (scanning) {
            bluetoothLeScanner.stopScan(scanCallback);
            scanning = false;
            statusTextView.setText("Scan stopped.");
            Log.d(TAG, "Scan stopped.");
        } else {
            Log.d(TAG, "No scan to stop.");
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        bluetoothGatt = device.connectGatt(requireContext(), false, gattCallback);
        Log.d(TAG, "Connecting to device: " + device.getName() + " - " + device.getAddress());
        statusTextView.setText("Connecting to " + device.getName() + "...");
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
}
