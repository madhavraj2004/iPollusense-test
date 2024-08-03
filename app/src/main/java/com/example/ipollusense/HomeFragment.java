package com.example.ipollusense;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseReference databaseReference;
    private LineChart lineChart;
    private AnyChartView anyChartView;
    private com.anychart.charts.Cartesian anyChart;

    private List<Entry> temperatureEntries = new ArrayList<>();
    private List<Entry> humidityEntries = new ArrayList<>();
    private List<Entry> no2Entries = new ArrayList<>();
    private List<Entry> c2h5ohEntries = new ArrayList<>();
    private List<Entry> vocEntries = new ArrayList<>();
    private List<Entry> coEntries = new ArrayList<>();
    private List<Entry> pm1Entries = new ArrayList<>();
    private List<Entry> pm2_5Entries = new ArrayList<>();
    private List<Entry> pm10Entries = new ArrayList<>();

    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        databaseReference = FirebaseDatabase.getInstance().getReference("sensorData");

        lineChart = binding.lineChart;
        setupLineChart();

        anyChartView = binding.anyChartView;
        anyChart = AnyChart.line();
        anyChartView.setChart(anyChart);

        // Observe LiveData from ViewModel
        sharedViewModel.getSensorData().observe(getViewLifecycleOwner(), sensorData -> {
            if (sensorData != null && sensorData instanceof SensorData) {
                SensorData data = (SensorData) sensorData;
                updateCharts(data);
                updateCardViews(data);
                databaseReference.push().setValue(data);
            }
        });
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
        leftAxis.setGranularity(1f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    }

    private void updateCharts(SensorData sensorData) {
        // Add new entries to the lists
        temperatureEntries.add(new Entry(temperatureEntries.size(), (float) sensorData.getTemperature()));
        humidityEntries.add(new Entry(humidityEntries.size(), (float) sensorData.getHumidity()));
        no2Entries.add(new Entry(no2Entries.size(), (float) sensorData.getNo2()));
        c2h5ohEntries.add(new Entry(c2h5ohEntries.size(), (float) sensorData.getC2h5oh()));
        vocEntries.add(new Entry(vocEntries.size(), (float) sensorData.getVoc()));
        coEntries.add(new Entry(coEntries.size(), (float) sensorData.getCo()));
        pm1Entries.add(new Entry(pm1Entries.size(), (float) sensorData.getPm1()));
        pm2_5Entries.add(new Entry(pm2_5Entries.size(), (float) sensorData.getPm2_5()));
        pm10Entries.add(new Entry(pm10Entries.size(), (float) sensorData.getPm10()));

        // Update LineChart
        updateLineChart();

        // Update AnyChart
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Temperature", sensorData.getTemperature()));
        data.add(new ValueDataEntry("Humidity", sensorData.getHumidity()));
        data.add(new ValueDataEntry("NO2", sensorData.getNo2()));
        data.add(new ValueDataEntry("C2H5OH", sensorData.getC2h5oh()));
        data.add(new ValueDataEntry("VOC", sensorData.getVoc()));
        data.add(new ValueDataEntry("CO", sensorData.getCo()));
        data.add(new ValueDataEntry("PM1", sensorData.getPm1()));
        data.add(new ValueDataEntry("PM2.5", sensorData.getPm2_5()));
        data.add(new ValueDataEntry("PM10", sensorData.getPm10()));

        anyChart.data(data);
        anyChartView.setChart(anyChart);
    }

    private void updateLineChart() {
        // Define datasets
        LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Temperature");
        temperatureDataSet.setColor(getResources().getColor(R.color.red));
        temperatureDataSet.setLineWidth(2f);

        LineDataSet humidityDataSet = new LineDataSet(humidityEntries, "Humidity");
        humidityDataSet.setColor(getResources().getColor(R.color.blue));
        humidityDataSet.setLineWidth(2f);

        LineDataSet no2DataSet = new LineDataSet(no2Entries, "NO2");
        no2DataSet.setColor(getResources().getColor(R.color.orange));
        no2DataSet.setLineWidth(2f);

        LineDataSet c2h5ohDataSet = new LineDataSet(c2h5ohEntries, "C2H5OH");
        c2h5ohDataSet.setColor(getResources().getColor(R.color.green));
        c2h5ohDataSet.setLineWidth(2f);

        LineDataSet vocDataSet = new LineDataSet(vocEntries, "VOC");
        vocDataSet.setColor(getResources().getColor(R.color.purple));
        vocDataSet.setLineWidth(2f);

        LineDataSet coDataSet = new LineDataSet(coEntries, "CO");
        coDataSet.setColor(getResources().getColor(R.color.yellow));
        coDataSet.setLineWidth(2f);

        LineDataSet pm1DataSet = new LineDataSet(pm1Entries, "PM1");
        pm1DataSet.setColor(getResources().getColor(R.color.brown));
        pm1DataSet.setLineWidth(2f);

        LineDataSet pm2_5DataSet = new LineDataSet(pm2_5Entries, "PM2.5");
        pm2_5DataSet.setColor(getResources().getColor(R.color.cyan));
        pm2_5DataSet.setLineWidth(2f);

        LineDataSet pm10DataSet = new LineDataSet(pm10Entries, "PM10");
        pm10DataSet.setColor(getResources().getColor(R.color.magenta));
        pm10DataSet.setLineWidth(2f);

        // Combine all datasets
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
        lineChart.invalidate(); // Refresh the chart
    }

    private void updateCardViews(SensorData sensorData) {
        // Ensure that the data types match the format specifiers
        binding.textTemperature.setText(String.format("Temperature: %.2f°C", (float) sensorData.getTemperature()));
        binding.textHumidity.setText(String.format("Humidity: %.2f%%", (float) sensorData.getHumidity()));
        binding.textNO2.setText(String.format("NO2: %.2f µg/m³", (float) sensorData.getNo2()));
        binding.textC2H5OH.setText(String.format("C2H5OH: %.2f µg/m³", (float) sensorData.getC2h5oh()));
        binding.textVOC.setText(String.format("VOC: %.2f µg/m³", (float) sensorData.getVoc()));
        binding.textCO.setText(String.format("CO: %.2f µg/m³", (float) sensorData.getCo()));
        binding.textPM1.setText(String.format("PM1: %.2f µg/m³", (float) sensorData.getPm1()));
        binding.textPM2.setText(String.format("PM2.5: %.2f µg/m³", (float) sensorData.getPm2_5()));
        binding.textPM10.setText(String.format("PM10: %.2f µg/m³", (float) sensorData.getPm10()));
    }
}
