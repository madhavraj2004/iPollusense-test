package com.example.ipollusense;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
public class MyApp extends Application {

    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:274963849995:android:81bc4b7f526d417882f80e")
                .setApiKey("AIzaSyCRPSKeDU4-rpGbGvlLCG61uCQuIuNgKwo")
                .setProjectId("ipollusense-e7c84")
                .build();
        FirebaseApp.initializeApp(this, options, "your_app_name"); // replace with your actual app details
    }
}
