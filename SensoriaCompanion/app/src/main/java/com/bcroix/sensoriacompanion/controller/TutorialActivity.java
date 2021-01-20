package com.bcroix.sensoriacompanion.controller;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bcroix.sensoriacompanion.R;
import com.bcroix.sensoriacompanion.model.BloodAnalysisSession;

public class TutorialActivity extends AppCompatActivity {

    private TextView mTitle;
    private TextView mExplanations;
    private Button mGoToAnalysisBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        mTitle = findViewById(R.id.activity_tutorial_title_text);
        mExplanations = findViewById(R.id.activity_tutorial_explanations);

        mGoToAnalysisBtn = findViewById(R.id.activity_tutorial_go_to_analysis_btn);

        mGoToAnalysisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TutorialActivity.this, BloodAnalysisActivity.class));
            }
        });



        }
}