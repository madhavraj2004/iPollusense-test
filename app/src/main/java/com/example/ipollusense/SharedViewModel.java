package com.example.ipollusense;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<SensorData> sensorData = new MutableLiveData<>();

    public LiveData<SensorData> getSensorData() {
        return sensorData;
    }

    public void setSensorData(SensorData data) {
        sensorData.setValue(data);
    }
}
