package com.example.ipollusense;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.polidea.rxandroidble3.RxBleClient;
import com.polidea.rxandroidble3.RxBleConnection;
import com.polidea.rxandroidble3.RxBleDevice;
import com.polidea.rxandroidble3.RxBleScanResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BluetoothService extends Service {

    private static final String CHARACTERISTIC_UUID = "0000fef4-0000-1000-8000-00805f9b34fb";
    private static final String TARGET_MAC_ADDRESS = "7C:DF:A1:EE:D4:96";
    private static final long READ_INTERVAL_MS = 5000; // 5 seconds
    private static final long RETRY_INTERVAL_MS = 3000; // 3 seconds

    private RxBleClient rxBleClient;
    private RxBleDevice selectedDevice;
    private RxBleConnection connection;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable scanDisposable;
    private Disposable connectDisposable;
    private Disposable readDisposable;
    private Disposable retryConnectDisposable;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize RxBleClient
        rxBleClient = RxBleClient.create(this);

        // Check permissions and start scanning if granted
        if (checkPermissions()) {
            startScan();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            startScan();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScan();
        disposeIfNotNull(readDisposable);
        disposeIfNotNull(retryConnectDisposable);
        compositeDisposable.clear();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean checkPermissions() {
        boolean bluetoothScanPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        boolean bluetoothConnectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (bluetoothScanPermission && bluetoothConnectPermission && locationPermission) {
            return true;
        } else {
            Log.e("BluetoothService", "Permissions not granted.");
            return false;
        }
    }

    private void startScan() {
        stopScan(); // Stop any ongoing scan

        Log.d("BluetoothService", "Scanning...");
        scanDisposable = rxBleClient.scanBleDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onScanResult, this::onScanError);

        compositeDisposable.add(scanDisposable);
    }

    private void onScanResult(RxBleScanResult scanResult) {
        RxBleDevice device = scanResult.getBleDevice();

        if (TARGET_MAC_ADDRESS.equals(device.getMacAddress())) {
            selectedDevice = device;
            connectToDevice();
        }
    }

    private void onScanError(Throwable throwable) {
        Log.e("BluetoothService", "Scan failed: " + throwable.toString());
    }

    private void stopScan() {
        if (scanDisposable != null && !scanDisposable.isDisposed()) {
            scanDisposable.dispose();
        }
    }

    private void connectToDevice() {
        if (selectedDevice == null) {
            Log.e("BluetoothService", "No device selected.");
            return;
        }

        Log.d("BluetoothService", "Connecting to " + selectedDevice.getName() + "...");
        connectDisposable = selectedDevice.establishConnection(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionEstablished, this::onConnectionFailed);

        compositeDisposable.add(connectDisposable);
    }

    private void onConnectionEstablished(RxBleConnection rxBleConnection) {
        connection = rxBleConnection;
        Log.d("BluetoothService", "Connected to " + selectedDevice.getName());

        // Start periodic reading of characteristic
        readDisposable = Schedulers.io().createWorker().schedulePeriodically(() -> {
            if (connection != null) {
                connection.readCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onCharacteristicRead, this::onCharacteristicReadFailed);
            }
        }, 0, READ_INTERVAL_MS, TimeUnit.MILLISECONDS);

        compositeDisposable.add(readDisposable);

        // Stop retry connection attempt when connected successfully
        disposeIfNotNull(retryConnectDisposable);
    }

    private void onConnectionFailed(Throwable throwable) {
        Log.e("BluetoothService", "Connection failed: " + throwable.toString());

        // Retry connection after a delay
        retryConnectDisposable = Schedulers.io().createWorker().schedulePeriodically(() -> {
            if (selectedDevice != null) {
                connectToDevice();
            }
        }, RETRY_INTERVAL_MS, RETRY_INTERVAL_MS, TimeUnit.MILLISECONDS);

        compositeDisposable.add(retryConnectDisposable);
    }

    private void onCharacteristicRead(byte[] bytes) {
        String jsonString = new String(bytes);
        Log.d("BluetoothService", "Data received: " + jsonString);
        updateUIWithData(jsonString);
    }

    private void onCharacteristicReadFailed(Throwable throwable) {
        Log.e("BluetoothService", "Failed to read characteristic: " + throwable.toString());
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

            SensorData sensorData = new SensorData(temperature, humidity, no2, c2h5oh, voc, co, pm1, pm2_5, pm10);

            // Notify HomeFragment or other components about the new sensor data
            // Implement your logic here to update the UI or database
        } catch (JSONException e) {
            Log.e("BluetoothService", "JSON Parsing error: " + e.toString());
        }
    }

    private void disposeIfNotNull(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
