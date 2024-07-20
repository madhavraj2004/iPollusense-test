package com.example.ipollusense;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.example.ipollusense.databinding.FragmentHomeBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
    private DatabaseReference databaseReference;
    private Handler handler = new Handler();
    private Runnable dataUpdater;
    private Random random = new Random();

    // MPAndroidChart variables
    private LineChart lineChart;

    // AnyChart variables
    private AnyChartView anyChartView;
    private com.anychart.charts.Cartesian anyChart;

    // Data lists for MPAndroidChart and AnyChart
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

        // Initialize MPAndroidChart
        lineChart = binding.lineChart;
        setupLineChart();

        // Initialize AnyChart
        anyChartView = binding.anyChartView;
        anyChart = AnyChart.line();
        anyChartView.setChart(anyChart);

        // Retrieve data from Firebase and plot on both charts
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear previous entries
                clearEntries();

                // Populate entries from Firebase data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SensorData sensorData = dataSnapshot.getValue(SensorData.class);
                    if (sensorData != null) {
                        long currentTime = System.currentTimeMillis();
                        float timeInHours = (currentTime % (24 * 3600 * 1000L)) / (3600 * 1000F);

                        // Add entries for MPAndroidChart
                        addMPAndroidChartEntries(timeInHours, sensorData);

                        // Add entries for AnyChart
                        addAnyChartEntries(timeInHours, sensorData);
                    }
                }

                // Update both charts
                updateLineChart();
                updateAnyChart();
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

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
    }

    private void updateLineChart() {
        LineDataSet temperatureDataSet = createLineDataSet(temperatureEntries, "Temperature", getResources().getColor(R.color.chart_temperature));
        LineDataSet humidityDataSet = createLineDataSet(humidityEntries, "Humidity", getResources().getColor(R.color.chart_humidity));
        LineDataSet no2DataSet = createLineDataSet(no2Entries, "NO2", getResources().getColor(R.color.chart_no2));
        LineDataSet c2h5ohDataSet = createLineDataSet(c2h5ohEntries, "C2H5OH", getResources().getColor(R.color.chart_c2h5oh));
        LineDataSet vocDataSet = createLineDataSet(vocEntries, "VOC", getResources().getColor(R.color.chart_voc));
        LineDataSet coDataSet = createLineDataSet(coEntries, "CO", getResources().getColor(R.color.chart_co));
        LineDataSet pm1DataSet = createLineDataSet(pm1Entries, "PM1", getResources().getColor(R.color.chart_pm1));
        LineDataSet pm2_5DataSet = createLineDataSet(pm2_5Entries, "PM2.5", getResources().getColor(R.color.chart_pm2_5));
        LineDataSet pm10DataSet = createLineDataSet(pm10Entries, "PM10", getResources().getColor(R.color.chart_pm10));

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(temperatureDataSet);
        dataSets.add(humidityDataSet);
        dataSets.add(no2DataSet);
        dataSets.add(c2h5ohDataSet);
        dataSets.add(vocDataSet);
        dataSets.add(coDataSet);
        dataSets.add(pm1DataSet);
        dataSets.add(pm2_5DataSet);
        dataSets.add(pm10DataSet);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private LineDataSet createLineDataSet(List<Entry> entries, String label, int color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);
        return dataSet;
    }

    private void updateAnyChart() {
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Temperature", getLastEntry(temperatureEntries).getY()));
        data.add(new ValueDataEntry("Humidity", getLastEntry(humidityEntries).getY()));
        data.add(new ValueDataEntry("NO2", getLastEntry(no2Entries).getY()));
        data.add(new ValueDataEntry("C2H5OH", getLastEntry(c2h5ohEntries).getY()));
        data.add(new ValueDataEntry("VOC", getLastEntry(vocEntries).getY()));
        data.add(new ValueDataEntry("CO", getLastEntry(coEntries).getY()));
        data.add(new ValueDataEntry("PM1", getLastEntry(pm1Entries).getY()));
        data.add(new ValueDataEntry("PM2.5", getLastEntry(pm2_5Entries).getY()));
        data.add(new ValueDataEntry("PM10", getLastEntry(pm10Entries).getY()));

        anyChart.line(data);
        anyChart.title("Sensor Data");
    }

    private Entry getLastEntry(List<Entry> entries) {
        if (entries.isEmpty()) {
            return new Entry(0f, 0f);
        } else {
            return entries.get(entries.size() - 1);
        }
    }

    private void addMPAndroidChartEntries(float timeInHours, SensorData sensorData) {
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

    private void addAnyChartEntries(float timeInHours, SensorData sensorData) {
        // Add entries as needed for AnyChart
        // Example: anyChartData.add(new ValueDataEntry(timeInHours, sensorData.temperature));
        // Repeat for other parameters
    }

    private void clearEntries() {
        temperatureEntries.clear();
        humidityEntries.clear();
        no2Entries.clear();
        c2h5ohEntries.clear();
        vocEntries.clear();
        coEntries.clear();
        pm1Entries.clear();
        pm2_5Entries.clear();
        pm10Entries.clear();
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
        updateUI(temperature, humidity, no2, c2h5oh, voc, co, pm1, pm2_5, pm10);

        // Get current time in hours
        long currentTime = System.currentTimeMillis();
        float timeInHours = (currentTime % (24 * 3600 * 1000L)) / (3600 * 1000F);

        // Add new entries to lists
        addMPAndroidChartEntries(timeInHours, sensorData);
        addAnyChartEntries(timeInHours, sensorData);

        // Update both charts
        updateLineChart();
        updateAnyChart();
    }

    private void updateUI(double temperature, double humidity, int no2, int c2h5oh, int voc, int co, int pm1, int pm2_5, int pm10) {
        binding.textTemperature.setText("Temperature: " + String.format("%.2f", temperature) + " °C");
        binding.textHumidity.setText("Humidity: " + String.format("%.2f", humidity) + " %");
        binding.textNO2.setText("NO2: " + no2 + " µg/m³");
        binding.textC2H5OH.setText("C2H5OH: " + c2h5oh + " ppm");
        binding.textVOC.setText("VOC: " + voc + " ppb");
        binding.textCO.setText("CO: " + co + " ppm");
        binding.textPM1.setText("PM1: " + pm1 + " µg/m³");
        binding.textPM2.setText("PM2.5: " + pm2_5 + " µg/m³");
        binding.textPM10.setText("PM10: " + pm10 + " µg/m³");
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
