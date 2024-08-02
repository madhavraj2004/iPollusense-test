package com.example.ipollusense;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.polidea.rxandroidble3.RxBleClient;
import com.polidea.rxandroidble3.RxBleConnection;
import com.polidea.rxandroidble3.RxBleDevice;
import com.polidea.rxandroidble3.RxBleScanResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BluetoothFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String CHARACTERISTIC_UUID = "0000fef4-0000-1000-8000-00805f9b34fb";
    private static final String TARGET_MAC_ADDRESS = "7C:DF:A1:EE:D4:96";
    private static final long READ_INTERVAL_MS = 5000; // 5 seconds

    private TextView statusTextView;
    private TextView dataTextView;
    private RecyclerView devicesRecyclerView;
    private DeviceAdapter deviceAdapter;
    private final List<RxBleDevice> deviceList = new ArrayList<>();

    private RxBleClient rxBleClient;
    private RxBleDevice selectedDevice;
    private RxBleConnection connection;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private SharedViewModel sharedViewModel;
    private Disposable readDisposable;
    private Disposable connectDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        statusTextView = view.findViewById(R.id.statusTextView);
        dataTextView = view.findViewById(R.id.label1);
        devicesRecyclerView = view.findViewById(R.id.recyclerView);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceAdapter = new DeviceAdapter(deviceList, this::onDeviceSelected);
        devicesRecyclerView.setAdapter(deviceAdapter);

        Button scanButton = view.findViewById(R.id.scanButton);
        Button connectButton = view.findViewById(R.id.connectButton);

        scanButton.setOnClickListener(v -> startScan());
        connectButton.setOnClickListener(v -> connectToDevice());

        // Initialize RxBleClient and ViewModel
        rxBleClient = RxBleClient.create(requireContext());
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Check and request permissions
        checkPermissions();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startScan();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
        if (readDisposable != null && !readDisposable.isDisposed()) {
            readDisposable.dispose();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear(); // Clear all disposables
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        } else {
            startScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                statusTextView.setText("Permissions denied.");
            }
        }
    }

    private void startScan() {
        stopScan(); // Stop any ongoing scan

        statusTextView.setText("Scanning...");
        Disposable scanDisposable = rxBleClient.scanBleDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onScanResult, this::onScanError);

        compositeDisposable.add(scanDisposable);
    }

    private void onScanResult(RxBleScanResult scanResult) {
        RxBleDevice device = scanResult.getBleDevice();

        if (TARGET_MAC_ADDRESS.equals(device.getMacAddress())) {
            selectedDevice = device;
            connectToDevice();
        } else if (!deviceList.contains(device)) {
            deviceList.add(device);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private void onScanError(Throwable throwable) {
        statusTextView.setText("Scan failed.");
        Log.e("BLE", "Scan failed: " + throwable.toString());
    }

    private void stopScan() {
        compositeDisposable.clear(); // Dispose of any previous scan subscription
    }

    private void connectToDevice() {
        if (selectedDevice == null) {
            statusTextView.setText("No device selected.");
            return;
        }

        statusTextView.setText("Connecting to " + selectedDevice.getName() + "...");
        connectDisposable = selectedDevice.establishConnection(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionEstablished, this::onConnectionFailed);

        compositeDisposable.add(connectDisposable);
    }

    private void onConnectionEstablished(RxBleConnection rxBleConnection) {
        connection = rxBleConnection;
        statusTextView.setText("Connected to " + selectedDevice.getName());

        // Start periodic reading of characteristic
        readDisposable = Schedulers.io().createWorker().schedulePeriodically(() -> {
            if (connection != null) {
                connection.readCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onCharacteristicRead, this::onCharacteristicReadFailed);
            }
        }, 0, READ_INTERVAL_MS, java.util.concurrent.TimeUnit.MILLISECONDS);

        compositeDisposable.add(readDisposable);
    }

    private void onConnectionFailed(Throwable throwable) {
        statusTextView.setText("Connection failed.");
        Log.e("BLE", "Connection failed: " + throwable.toString());
    }

    private void onCharacteristicRead(byte[] bytes) {
        String jsonString = new String(bytes);
        Log.d("BLE", "Data received: " + jsonString);
        updateUIWithData(jsonString);
    }

    private void onCharacteristicReadFailed(Throwable throwable) {
        statusTextView.setText("Failed to read characteristic.");
        Log.e("BLE", "Failed to read characteristic: " + throwable.toString());
    }

    private void updateUIWithData(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            double temperature = jsonObject.getDouble("temperature");
            double humidity = jsonObject.getDouble("humidity");
            int no2 = jsonObject.getInt("no2");
            int c2h5oh = jsonObject.getInt("c2h5oh");
            int voc = jsonObject.getInt("voc");
            int co = jsonObject.getInt("co");
            int pm1 = jsonObject.getInt("pm1");
            int pm2_5 = jsonObject.getInt("pm2_5");
            int pm10 = jsonObject.getInt("pm10");

            // Update dataTextView with received data
            dataTextView.setText(
                    "Temperature: " + temperature + "\n" +
                            "Humidity: " + humidity + "\n" +
                            "NO2: " + no2 + "\n" +
                            "C2H5OH: " + c2h5oh + "\n" +
                            "VOC: " + voc + "\n" +
                            "CO: " + co + "\n" +
                            "PM1: " + pm1 + "\n" +
                            "PM2.5: " + pm2_5 + "\n" +
                            "PM10: " + pm10
            );

            // Notify ViewModel of new sensor data
            SensorData sensorData = new SensorData(temperature, humidity, no2, c2h5oh, voc, co, pm1, pm2_5, pm10);
            sharedViewModel.setSensorData(sensorData);
        } catch (JSONException e) {
            statusTextView.setText("Failed to parse data.");
            Log.e("BLE", "JSON parsing error: " + e.toString());
        }
    }

    // Method to handle device selection
    private void onDeviceSelected(RxBleDevice device) {
        selectedDevice = device;
        statusTextView.setText("Selected device: " + device.getName());
        connectToDevice();
    }
}
