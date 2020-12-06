package com.bcroix.sensoriacompanion.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bcroix.sensoriacompanion.R;

public class MainActivity extends AppCompatActivity {
    // View members
    private TextView mWelcomeMsg;
    private Button mBloodAnalysisMenuButton;
    private Button mStatisticsMenuButton;

    // Model members


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link view elements
        mWelcomeMsg = findViewById(R.id.activity_main_welcome_msg);
        mBloodAnalysisMenuButton = findViewById(R.id.activity_main_blood_analysis_menu_button);
        mStatisticsMenuButton = findViewById(R.id.activity_main_statistics_menu_button);

        // Add button listeners
        mBloodAnalysisMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BloodAnalysisActivity.class));
            }
        });

        mStatisticsMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
            }
        });
    }
}