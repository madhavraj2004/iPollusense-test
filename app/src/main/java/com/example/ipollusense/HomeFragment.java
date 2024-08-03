package com.example.ipollusense;

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

    private SharedPreferencesHelper sharedPreferencesHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferencesHelper = new SharedPreferencesHelper(requireContext());
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(sharedPreferencesHelper.getUserId()).child("data");

        lineChart = binding.lineChart;
        setupLineChart();

        anyChartView = binding.anyChartView;
        anyChart = AnyChart.line();
        anyChartView.setChart(anyChart);

        fetchDataForLast24Hours();
    }

    private void fetchDataForLast24Hours() {
        long oneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000; // 24 hours in milliseconds

        databaseReference.orderByKey().startAt(String.valueOf(oneDayAgo)).endAt(String.valueOf(System.currentTimeMillis()))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        temperatureEntries.clear();
                        humidityEntries.clear();
                        no2Entries.clear();
                        c2h5ohEntries.clear();
                        vocEntries.clear();
                        coEntries.clear();
                        pm1Entries.clear();
                        pm2_5Entries.clear();
                        pm10Entries.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            long timestamp = Long.parseLong(snapshot.getKey());
                            SensorData data = snapshot.getValue(SensorData.class);

                            if (data != null) {
                                temperatureEntries.add(new Entry(timestamp, (float) data.getTemperature()));
                                humidityEntries.add(new Entry(timestamp, (float) data.getHumidity()));
                                no2Entries.add(new Entry(timestamp, (float) data.getNo2()));
                                c2h5ohEntries.add(new Entry(timestamp, (float) data.getC2h5oh()));
                                vocEntries.add(new Entry(timestamp, (float) data.getVoc()));
                                coEntries.add(new Entry(timestamp, (float) data.getCo()));
                                pm1Entries.add(new Entry(timestamp, (float) data.getPm1()));
                                pm2_5Entries.add(new Entry(timestamp, (float) data.getPm2_5()));
                                pm10Entries.add(new Entry(timestamp, (float) data.getPm10()));
                            }
                        }
                        updateCharts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors.
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
        // Update LineChart
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

        // Update AnyChart
        List<DataEntry> dataEntries = new ArrayList<>();
        dataEntries.add(new ValueDataEntry("Temperature", temperatureEntries.size() > 0 ? temperatureEntries.get(temperatureEntries.size() - 1).getY() : 0));
        dataEntries.add(new ValueDataEntry("Humidity", humidityEntries.size() > 0 ? humidityEntries.get(humidityEntries.size() - 1).getY() : 0));
        dataEntries.add(new ValueDataEntry("NO2", no2Entries.size() > 0 ? no2Entries.get(no2Entries.size() - 1).getY() : 0));
        dataEntries.add(new ValueDataEntry("C2H5OH", c2h5ohEntries.size() > 0 ? c2h5ohEntries.get(c2h5ohEntries.size() - 1).getY() : 0));
        dataEntries.add(new ValueDataEntry("VOC", vocEntries.size() > 0 ? vocEntries.get(vocEntries.size() - 1).getY() : 0));
        dataEntries.add(new ValueDataEntry("CO", coEntries.size() > 0 ? coEntries.get(coEntries.size() - 1).getY() : 0));
        dataEntries.add(new ValueDataEntry("PM1", pm1Entries.size() > 0 ? pm1Entries.get(pm1Entries.size() - 1).getY() : 0));
        dataEntries.add(new ValueDataEntry("PM2.5", pm2_5Entries.size() > 0 ? pm2_5Entries.get(pm2_5Entries.size() - 1).getY() : 0));
        dataEntries.add(new ValueDataEntry("PM10", pm10Entries.size() > 0 ? pm10Entries.get(pm10Entries.size() - 1).getY() : 0));

        anyChart.data(dataEntries);
    }
}
