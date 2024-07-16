package com.example.ipollusense;

import android.graphics.Color;
import android.os.Bundle;
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
import com.anychart.charts.Cartesian;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.example.ipollusense.databinding.FragmentHomeBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private LineChart lineChart;
    private AnyChartView anyChartView;
    private List<Entry> temperatureEntries;
    private List<Entry> humidityEntries;
    private List<Entry> no2Entries;
    private List<Entry> c2h5ohEntries;
    private List<Entry> vocEntries;
    private List<Entry> coEntries;
    private List<Entry> pm1Entries;
    private List<Entry> pm2_5Entries;
    private List<Entry> pm10Entries;
    private LineData lineData;
    private Random random;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views and data structures
        lineChart = binding.lineChart;
        anyChartView = binding.anyChartView;
        random = new Random();

        temperatureEntries = new ArrayList<>();
        humidityEntries = new ArrayList<>();
        no2Entries = new ArrayList<>();
        c2h5ohEntries = new ArrayList<>();
        vocEntries = new ArrayList<>();
        coEntries = new ArrayList<>();
        pm1Entries = new ArrayList<>();
        pm2_5Entries = new ArrayList<>();
        pm10Entries = new ArrayList<>();

        setupLineChart();
        setupAnyChart();
        updateSensorValues();
        updateLineChart();

        // Start periodic sensor value updates
        startSensorValueUpdater();
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(24);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int hours = (int) value / 6;
                int minutes = (int) ((value % 6) * 10);
                return String.format("%02d:%02d", hours, minutes);
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setupAnyChart() {
        Cartesian barChart = AnyChart.column();
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("NO2", random.nextInt(100)));
        data.add(new ValueDataEntry("C2H5OH", random.nextInt(100)));
        data.add(new ValueDataEntry("VOC", random.nextInt(100)));
        data.add(new ValueDataEntry("CO", random.nextInt(100)));
        data.add(new ValueDataEntry("PM2.5", random.nextInt(100)));

        barChart.data(data);

        barChart.title("Pollutant Comparison");
        barChart.labels().enabled(true);
        barChart.labels().position("right");

        barChart.yScale().minimum(0);
        barChart.yScale().maximum(100);

        barChart.animation(true);
        barChart.tooltip()
                .position(Position.RIGHT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d)
                .format("{%Value}%");

        barChart.interactivity().hoverMode(HoverMode.BY_X);

        anyChartView.setChart(barChart);
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

        // Update line chart with new data
        updateLineChart();
    }

    private void updateLineChart() {
        List<ILineDataSet> dataSets = new ArrayList<>();

        LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Temperature");
        temperatureDataSet.setColor(Color.RED);
        temperatureDataSet.setCircleColor(Color.RED);
        temperatureDataSet.setLineWidth(2f);
        temperatureDataSet.setCircleRadius(4f);
        temperatureDataSet.setDrawCircleHole(false);
        temperatureDataSet.setValueTextSize(10f);
        temperatureDataSet.setValueTextColor(Color.RED);
        dataSets.add(temperatureDataSet);

        LineDataSet humidityDataSet = new LineDataSet(humidityEntries, "Humidity");
        humidityDataSet.setColor(Color.BLUE);
        humidityDataSet.setCircleColor(Color.BLUE);
        humidityDataSet.setLineWidth(2f);
        humidityDataSet.setCircleRadius(4f);
        humidityDataSet.setDrawCircleHole(false);
        humidityDataSet.setValueTextSize(10f);
        humidityDataSet.setValueTextColor(Color.BLUE);
        dataSets.add(humidityDataSet);

        LineDataSet no2DataSet = new LineDataSet(no2Entries, "NO2");
        no2DataSet.setColor(Color.GREEN);
        no2DataSet.setCircleColor(Color.GREEN);
        no2DataSet.setLineWidth(2f);
        no2DataSet.setCircleRadius(4f);
        no2DataSet.setDrawCircleHole(false);
        no2DataSet.setValueTextSize(10f);
        no2DataSet.setValueTextColor(Color.GREEN);
        dataSets.add(no2DataSet);

        LineDataSet c2h5ohDataSet = new LineDataSet(c2h5ohEntries, "C2H5OH");
        c2h5ohDataSet.setColor(Color.MAGENTA);
        c2h5ohDataSet.setCircleColor(Color.MAGENTA);
        c2h5ohDataSet.setLineWidth(2f);
        c2h5ohDataSet.setCircleRadius(4f);
        c2h5ohDataSet.setDrawCircleHole(false);
        c2h5ohDataSet.setValueTextSize(10f);
        c2h5ohDataSet.setValueTextColor(Color.MAGENTA);
        dataSets.add(c2h5ohDataSet);

        LineDataSet vocDataSet = new LineDataSet(vocEntries, "VOC");
        vocDataSet.setColor(Color.CYAN);
        vocDataSet.setCircleColor(Color.CYAN);
        vocDataSet.setLineWidth(2f);
        vocDataSet.setCircleRadius(4f);
        vocDataSet.setDrawCircleHole(false);
        vocDataSet.setValueTextSize(10f);
        vocDataSet.setValueTextColor(Color.CYAN);
        dataSets.add(vocDataSet);

        LineDataSet coDataSet = new LineDataSet(coEntries, "CO");
        coDataSet.setColor(Color.YELLOW);
        coDataSet.setCircleColor(Color.YELLOW);
        coDataSet.setLineWidth(2f);
        coDataSet.setCircleRadius(4f);
        coDataSet.setDrawCircleHole(false);
        coDataSet.setValueTextSize(10f);
        coDataSet.setValueTextColor(Color.YELLOW);
        dataSets.add(coDataSet);

        LineDataSet pm1DataSet = new LineDataSet(pm1Entries, "PM1");
        pm1DataSet.setColor(Color.GRAY);
        pm1DataSet.setCircleColor(Color.GRAY);
        pm1DataSet.setLineWidth(2f);
        pm1DataSet.setCircleRadius(4f);
        pm1DataSet.setDrawCircleHole(false);
        pm1DataSet.setValueTextSize(10f);
        pm1DataSet.setValueTextColor(Color.GRAY);
        dataSets.add(pm1DataSet);

        LineDataSet pm2_5DataSet = new LineDataSet(pm2_5Entries, "PM2.5");
        pm2_5DataSet.setColor(Color.LTGRAY);
        pm2_5DataSet.setCircleColor(Color.LTGRAY);
        pm2_5DataSet.setLineWidth(2f);
        pm2_5DataSet.setCircleRadius(4f);
        pm2_5DataSet.setDrawCircleHole(false);
        pm2_5DataSet.setValueTextSize(10f);
        pm2_5DataSet.setValueTextColor(Color.LTGRAY);
        dataSets.add(pm2_5DataSet);

        LineDataSet pm10DataSet = new LineDataSet(pm10Entries, "PM10");
        pm10DataSet.setColor(Color.DKGRAY);
        pm10DataSet.setCircleColor(Color.DKGRAY);
        pm10DataSet.setLineWidth(2f);
        pm10DataSet.setCircleRadius(4f);
        pm10DataSet.setDrawCircleHole(false);
        pm10DataSet.setValueTextSize(10f);
        pm10DataSet.setValueTextColor(Color.DKGRAY);
        dataSets.add(pm10DataSet);

        lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh the chart with new data
    }

    private void startSensorValueUpdater() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null && binding != null) { // Check if binding isvalid
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateSensorValues();
                        }
                    });
                }
            }
        }, 0, 5000);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
