package com.example.ipollusense;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    // MPAndroidChart variables
    private LineChart lineChart;

    // AnyChart variables
    private AnyChartView anyChartView;
    private com.anychart.charts.Cartesian anyChart;

    // Data lists for MPAndroidChart and AnyChart
    private List<SensorData> sensorDataList = new ArrayList<>();

    // TextViews for pollution parameters
    private TextView textTemperature, textHumidity, textNO2, textC2H5OH, textVOC, textCO, textPM1, textPM2, textPM10;

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

        // Initialize ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("sensorData");

        // Initialize MPAndroidChart
        lineChart = binding.lineChart;
        setupLineChart();

        // Initialize AnyChart
        anyChartView = binding.anyChartView;
        anyChart = AnyChart.line();
        anyChartView.setChart(anyChart);

        // Initialize TextViews for pollution parameters
        textTemperature = binding.textTemperature;
        textHumidity = binding.textHumidity;
        textNO2 = binding.textNO2;
        textC2H5OH = binding.textC2H5OH;
        textVOC = binding.textVOC;
        textCO = binding.textCO;
        textPM1 = binding.textPM1;
        textPM2 = binding.textPM2;
        textPM10 = binding.textPM10;

        // Observe sensor data from SharedViewModel
        sharedViewModel.getSensorData().observe(getViewLifecycleOwner(), sensorData -> {
            if (sensorData != null) {
                // Add new data to the list
                sensorDataList.add(sensorData);

                // Update charts with new data
                updateCharts();

                // Update UI with new sensor data
                updateCardViews(sensorData);

                // Store data in Firebase
                databaseReference.push().setValue(sensorData);
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

    private void updateCharts() {
        // Create lists for each parameter
        List<Entry> temperatureEntries = new ArrayList<>();
        List<Entry> humidityEntries = new ArrayList<>();
        List<Entry> no2Entries = new ArrayList<>();
        List<Entry> c2h5ohEntries = new ArrayList<>();
        List<Entry> vocEntries = new ArrayList<>();
        List<Entry> coEntries = new ArrayList<>();
        List<Entry> pm1Entries = new ArrayList<>();
        List<Entry> pm2_5Entries = new ArrayList<>();
        List<Entry> pm10Entries = new ArrayList<>();

        // Fill the lists with sensor data
        for (int i = 0; i < sensorDataList.size(); i++) {
            SensorData data = sensorDataList.get(i);
            temperatureEntries.add(new Entry(i, (float) data.getTemperature()));
            humidityEntries.add(new Entry(i, (float) data.getHumidity()));
            no2Entries.add(new Entry(i, (float) data.getNo2()));
            c2h5ohEntries.add(new Entry(i, (float) data.getC2h5oh()));
            vocEntries.add(new Entry(i, (float) data.getVoc()));
            coEntries.add(new Entry(i, (float) data.getCo()));
            pm1Entries.add(new Entry(i, (float) data.getPm1()));
            pm2_5Entries.add(new Entry(i, (float) data.getPm2_5()));
            pm10Entries.add(new Entry(i, (float) data.getPm10()));
        }

        // Update MPAndroidChart data
        updateLineChart(temperatureEntries, humidityEntries, no2Entries, c2h5ohEntries, vocEntries, coEntries, pm1Entries, pm2_5Entries, pm10Entries);

        // Update AnyChart data
        List<DataEntry> anyChartData = new ArrayList<>();
        for (SensorData data : sensorDataList) {
            anyChartData.add(new ValueDataEntry("Temperature", data.getTemperature()));
            anyChartData.add(new ValueDataEntry("Humidity", data.getHumidity()));
            anyChartData.add(new ValueDataEntry("NO2", data.getNo2()));
            anyChartData.add(new ValueDataEntry("C2H5OH", data.getC2h5oh()));
            anyChartData.add(new ValueDataEntry("VOC", data.getVoc()));
            anyChartData.add(new ValueDataEntry("CO", data.getCo()));
            anyChartData.add(new ValueDataEntry("PM1", data.getPm1()));
            anyChartData.add(new ValueDataEntry("PM2.5", data.getPm2_5()));
            anyChartData.add(new ValueDataEntry("PM10", data.getPm10()));
        }

        anyChart.data(anyChartData); // Use data() instead of line() for updating data
        anyChart.title("Sensor Data");
    }

    private void updateLineChart(List<Entry> temperatureEntries, List<Entry> humidityEntries,
                                 List<Entry> no2Entries, List<Entry> c2h5ohEntries, List<Entry> vocEntries,
                                 List<Entry> coEntries, List<Entry> pm1Entries, List<Entry> pm2_5Entries, List<Entry> pm10Entries) {
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

    private void updateCardViews(SensorData data) {
        textTemperature.setText("Temp: " + data.getTemperature() + "°C");
        textHumidity.setText("Humidity: " + data.getHumidity() + "%");
        textNO2.setText("NO2: " + data.getNo2());
        textC2H5OH.setText("C2H5OH: " + data.getC2h5oh());
        textVOC.setText("VOC: " + data.getVoc());
        textCO.setText("CO: " + data.getCo());
        textPM1.setText("PM1: " + data.getPm1());
        textPM2.setText("PM2.5: " + data.getPm2_5());
        textPM10.setText("PM10: " + data.getPm10());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}
