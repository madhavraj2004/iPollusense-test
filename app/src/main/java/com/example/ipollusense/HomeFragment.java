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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private LineChart lineChart;
    private AnyChartView anyChartView;
    private ArrayList<Entry> entries;
    private LineDataSet dataSet;
    private LineData lineData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = binding.chart;
        anyChartView = binding.anyChartView;
        entries = new ArrayList<>();

        setupLineChart();
        updateSensorValues();
        updateLineChart();
        setupAnyChart(); // Setup AnyChart

        startSensorValueUpdater();
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
    }

    private void setupAnyChart() {
        Cartesian barChart = AnyChart.column();
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("NO2", 25));
        data.add(new ValueDataEntry("C2H5OH", 35));
        data.add(new ValueDataEntry("VOC", 20));
        data.add(new ValueDataEntry("CO", 40));
        data.add(new ValueDataEntry("PM2.5", 50));

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
        double temperature = 20.0 + (30.0 - 20.0) * Math.random();
        double humidity = 30.0 + (50.0 - 30.0) * Math.random();
        int no2 = (int) (10 + (20 - 10) * Math.random());
        int c2h5oh = (int) (100 + (150 - 100) * Math.random());
        int voc = (int) (5 + (15 - 5) * Math.random());
        int co = (int) (40 + (70 - 40) * Math.random());
        int pm1 = (int) (30 + (60 - 30) * Math.random());
        int pm2_5 = (int) (50 + (80 - 50) * Math.random());
        int pm10 = (int) (70 + (110 - 70) * Math.random());

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

    private void updateLineChart() {
        entries.clear();
        for (int i = 0; i < 24; i++) {
            float value = (float) (50 + 20 * Math.sin(i * Math.PI / 12));
            entries.add(new Entry(i, value));
        }

        dataSet = new LineDataSet(entries, "Pollutant Levels");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);

        lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private void startSensorValueUpdater() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(() -> {
                    updateSensorValues();
                    updateLineChart();
                });
            }
        }, 0, 60000);
    }
}
