package com.example.ipollusense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FeaturesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FeaturesAdapter adapter;
    private List<String> featureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        featureList = new ArrayList<>();
        featureList.add("Feature 1");
        featureList.add("Feature 2");
        featureList.add("Feature 3");

        adapter = new FeaturesAdapter(featureList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeaturesActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
