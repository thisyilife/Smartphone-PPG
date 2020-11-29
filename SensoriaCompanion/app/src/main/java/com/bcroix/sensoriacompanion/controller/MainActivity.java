package com.bcroix.sensoriacompanion.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bcroix.sensoriacompanion.R;

public class MainActivity extends AppCompatActivity {
    // View members
    private TextView mWelcomeMsg;
    private Button mBloodAnalysisButton;

    // Model members

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link view elements
        mWelcomeMsg = findViewById(R.id.activity_main_welcome_msg);
        mBloodAnalysisButton = findViewById(R.id.activity_main_blood_analysis_button);

        // Add button listeners
        mBloodAnalysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BloodAnalysisActivity.class));
            }
        });
    }
}