package com.example.ipollusense;

import androidx.lifecycle.ViewModel;
import com.polidea.rxandroidble3.RxBleConnection;
import io.reactivex.rxjava3.disposables.Disposable;

public class BluetoothViewModel extends ViewModel {

    private RxBleConnection connection;
    private Disposable connectionDisposable;

    public RxBleConnection getConnection() {
        return connection;
    }

    public void setConnection(RxBleConnection connection) {
        this.connection = connection;
    }

    public Disposable getConnectionDisposable() {
        return connectionDisposable;
    }

    public void setConnectionDisposable(Disposable connectionDisposable) {
        this.connectionDisposable = connectionDisposable;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
    }
}
