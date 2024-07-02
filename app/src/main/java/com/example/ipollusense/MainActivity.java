package com.example.ipollusense;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_PERMISSION_LOCATION = 2;
    private final int REQUEST_PERMISSION_BLUETOOTH = 3;

    private ArrayAdapter<String> deviceListAdapter;
    private List<String> deviceList = new ArrayList<>();
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();

    private final UUID SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb"); // Example service UUID
    private final UUID CHARACTERISTIC_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"); // Example characteristic UUID

    private TextView label1;
    private TextView temperatureValue;
    private TextView humidityValue;
    private TextView no2Value;
    private TextView c2h5ohValue;
    private TextView vocValue;
    private TextView coValue;
    private TextView pm1Value;
    private TextView pm2Value;
    private TextView pm10Value;
    private TextView testValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        ListView listViewDevices = findViewById(R.id.listViewDevices);
        Button btnScan = findViewById(R.id.btn_scan);
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listViewDevices.setAdapter(deviceListAdapter);

        label1 = findViewById(R.id.label1);
        temperatureValue = findViewById(R.id.temp_value);
        humidityValue = findViewById(R.id.hum_value);
        no2Value = findViewById(R.id.no2_value);
        c2h5ohValue = findViewById(R.id.c2h5oh_value);
        vocValue = findViewById(R.id.voc_value);
        coValue = findViewById(R.id.co_value);
        pm1Value = findViewById(R.id.pm1_value);
        pm2Value = findViewById(R.id.pm2_value);
        pm10Value = findViewById(R.id.pm10_value);
        testValue = findViewById(R.id.test_value);

        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = bluetoothDevices.get(position);
            connectToDevice(device);
        });

        // Check for necessary permissions and request them if not granted
        checkAndRequestPermissions();

        // Get BluetoothAdapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Request to enable Bluetooth if it is not enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Handle button click to start scanning
        btnScan.setOnClickListener(v -> {
            if (bluetoothAdapter.isEnabled()) {
                startBluetoothScanning();
            } else {
                Toast.makeText(MainActivity.this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH);
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]),
                    REQUEST_PERMISSION_BLUETOOTH);
        }
    }

    private void startBluetoothScanning() {
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    private final BluetoothAdapter.LeScanCallback leScanCallback = (device, rssi, scanRecord) -> runOnUiThread(() -> {
        if (!deviceList.contains(device.getName() + "\n" + device.getAddress())) {
            deviceList.add(device.getName() + "\n" + device.getAddress());
            bluetoothDevices.add(device);
            deviceListAdapter.notifyDataSetChanged();
        }
    });

    private void connectToDevice(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected to device", Toast.LENGTH_SHORT).show());
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected from device", Toast.LENGTH_SHORT).show());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    if (characteristic != null) {
                        gatt.readCharacteristic(characteristic);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                String dataString = new String(data);
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(dataString);
                        label1.setText(dataString);

                        temperatureValue.setText(String.valueOf(jsonObject.getDouble("temperature")));
                        humidityValue.setText(String.valueOf(jsonObject.getDouble("humidity")));
                        no2Value.setText(String.valueOf(jsonObject.getInt("no2")));
                        c2h5ohValue.setText(String.valueOf(jsonObject.getInt("c2h5oh")));
                        vocValue.setText(String.valueOf(jsonObject.getInt("voc")));
                        coValue.setText(String.valueOf(jsonObject.getInt("co")));
                        pm1Value.setText(String.valueOf(jsonObject.getInt("pm1")));
                        pm2Value.setText(String.valueOf(jsonObject.getInt("pm2_5")));
                        pm10Value.setText(String.valueOf(jsonObject.getInt("pm10")));
                        testValue.setText(String.valueOf(jsonObject.getInt("test")));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Failed to parse JSON", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_LOCATION || requestCode == REQUEST_PERMISSION_BLUETOOTH) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
