package com.example.ipollusense;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ipollusense.databinding.FragmentHomeBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private LineChart lineChart;
    private DatabaseReference databaseReference;
    private Handler handler = new Handler();
    private Runnable dataUpdater;
    private Random random = new Random();

    private List<Entry> temperatureEntries = new ArrayList<>();
    private List<Entry> humidityEntries = new ArrayList<>();
    private List<Entry> no2Entries = new ArrayList<>();
    private List<Entry> c2h5ohEntries = new ArrayList<>();
    private List<Entry> vocEntries = new ArrayList<>();
    private List<Entry> coEntries = new ArrayList<>();
    private List<Entry> pm1Entries = new ArrayList<>();
    private List<Entry> pm2_5Entries = new ArrayList<>();
    private List<Entry> pm10Entries = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("sensorData");

        // Initialize line chart
        lineChart = binding.lineChart;
        setupLineChart();

        // Retrieve data from Firebase and plot on line chart
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                temperatureEntries.clear();
                humidityEntries.clear();
                no2Entries.clear();
                c2h5ohEntries.clear();
                vocEntries.clear();
                coEntries.clear();
                pm1Entries.clear();
                pm2_5Entries.clear();
                pm10Entries.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SensorData sensorData = dataSnapshot.getValue(SensorData.class);
                    if (sensorData != null) {
                        long currentTime = System.currentTimeMillis();
                        float timeInHours = (currentTime % (24 * 3600 * 1000L)) / (3600 * 1000F);

                        temperatureEntries.add(new Entry(timeInHours, (float) sensorData.temperature));
                        humidityEntries.add(new Entry(timeInHours, (float) sensorData.humidity));
                        no2Entries.add(new Entry(timeInHours, sensorData.no2));
                        c2h5ohEntries.add(new Entry(timeInHours, sensorData.c2h5oh));
                        vocEntries.add(new Entry(timeInHours, sensorData.voc));
                        coEntries.add(new Entry(timeInHours, sensorData.co));
                        pm1Entries.add(new Entry(timeInHours, sensorData.pm1));
                        pm2_5Entries.add(new Entry(timeInHours, sensorData.pm2_5));
                        pm10Entries.add(new Entry(timeInHours, sensorData.pm10));
                    }
                }

                updateLineChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });

        // Schedule data updates every 10 seconds
        dataUpdater = new Runnable() {
            @Override
            public void run() {
                updateSensorValues();
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(dataUpdater);
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getLegend().setEnabled(true);
    }

    private void updateLineChart() {
        LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Temperature");
        LineDataSet humidityDataSet = new LineDataSet(humidityEntries, "Humidity");
        LineDataSet no2DataSet = new LineDataSet(no2Entries, "NO2");
        LineDataSet c2h5ohDataSet = new LineDataSet(c2h5ohEntries, "C2H5OH");
        LineDataSet vocDataSet = new LineDataSet(vocEntries, "VOC");
        LineDataSet coDataSet = new LineDataSet(coEntries, "CO");
        LineDataSet pm1DataSet = new LineDataSet(pm1Entries, "PM1");
        LineDataSet pm2_5DataSet = new LineDataSet(pm2_5Entries, "PM2.5");
        LineDataSet pm10DataSet = new LineDataSet(pm10Entries, "PM10");

        LineData lineData = new LineData(temperatureDataSet, humidityDataSet, no2DataSet, c2h5ohDataSet, vocDataSet, coDataSet, pm1DataSet, pm2_5DataSet, pm10DataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh the chart
    }

    private void updateSensorValues() {
        // Generate random sensor values
        double temperature = 20.0 + (30.0 - 20.0) * random.nextDouble();
        double humidity = 30.0 + (50.0 - 30.0) * random.nextDouble();
        int no2 = 10 + random.nextInt(20 - 10);
        int c2h5oh = 100 + random.nextInt(150 - 100);
        int voc = 5 + random.nextInt(15 - 5);
        int co = 40 + random.nextInt(70 - 40);
        int pm1 = 30 + random.nextInt(60 - 30);
        int pm2_5 = 50 + random.nextInt(80 - 50);
        int pm10 = 70 + random.nextInt(110 - 70);

        // Push data to Firebase
        SensorData sensorData = new SensorData(temperature, humidity, no2, c2h5oh, voc, co, pm1, pm2_5, pm10);
        databaseReference.push().setValue(sensorData);

        // Update UI with sensor values
        binding.textTemperature.setText("Temperature: " + String.format("%.2f", temperature) + " °C");
        binding.textHumidity.setText("Humidity: " + String.format("%.2f", humidity) + " %");
        binding.textNO2.setText("NO2: " + no2 + " µg/m³");
        binding.textC2H5OH.setText("C2H5OH: " + c2h5oh + " ppm");
        binding.textVOC.setText("VOC: " + voc + " ppb");
        binding.textCO.setText("CO: " + co + " ppm");
        binding.textPM1.setText("PM1: " + pm1 + " µg/m³");
        binding.textPM2.setText("PM2.5: " + pm2_5 + " µg/m³");
        binding.textPM10.setText("PM10: " + pm10 + " µg/m³");

        // Get current time in hours
        long currentTime = System.currentTimeMillis();
        float timeInHours = (currentTime % (24 * 3600 * 1000L)) / (3600 * 1000F);

        // Add new entries to the lists
        temperatureEntries.add(new Entry(timeInHours, (float) temperature));
        humidityEntries.add(new Entry(timeInHours, (float) humidity));
        no2Entries.add(new Entry(timeInHours, no2));
        c2h5ohEntries.add(new Entry(timeInHours, c2h5oh));
        vocEntries.add(new Entry(timeInHours, voc));
        coEntries.add(new Entry(timeInHours, co));
        pm1Entries.add(new Entry(timeInHours, pm1));
        pm2_5Entries.add(new Entry(timeInHours, pm2_5));
        pm10Entries.add(new Entry(timeInHours, pm10));

        updateLineChart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(dataUpdater);
        binding = null;
    }

    public static class SensorData {
        public double temperature;
        public double humidity;
        public int no2;
        public int c2h5oh;
        public int voc;
        public int co;
        public int pm1;
        public int pm2_5;
        public int pm10;

        public SensorData() {
        }

        public SensorData(double temperature, double humidity, int no2, int c2h5oh, int voc, int co, int pm1, int pm2_5, int pm10) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.no2 = no2;
            this.c2h5oh = c2h5oh;
            this.voc = voc;
            this.co = co;
            this.pm1 = pm1;
            this.pm2_5 = pm2_5;
            this.pm10 = pm10;
        }
    }
}
