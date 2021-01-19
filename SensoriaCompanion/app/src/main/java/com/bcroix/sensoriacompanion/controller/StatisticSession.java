package com.bcroix.sensoriacompanion.controller;

import androidx.appcompat.app.AppCompatActivity;
import com.bcroix.sensoriacompanion.R;
import com.bcroix.sensoriacompanion.model.BloodAnalysisSession;
import com.bcroix.sensoriacompanion.controller.GraphTools;
import com.bcroix.sensoriacompanion.model.FrameInfo;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class StatisticSession extends AppCompatActivity {

    private LineChart mLineChart;
    private TextView mInfoText;
    private int mKeyPosition;
    private List<String> mKeyList;
    private Button mDeleteButton;
    private SharedPreferences mPreferences;
    //private BloodAnalysisSession mAnalysisSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_session);

        mLineChart = findViewById(R.id.activity_statistic_session_graph);
        mLineChart.getDescription().setEnabled(false);
        mDeleteButton = findViewById(R.id.activity_statistic_session_delete);

        //Get previous analysis List
        mPreferences = getDefaultSharedPreferences(getApplicationContext());
        mKeyList = new ArrayList<>(mPreferences.getAll().keySet());

        //Get position of the analysis in the list
        Intent intent = getIntent();
        mKeyPosition = intent.getIntExtra("position",0);

        //Display analysis name
        mInfoText = findViewById(R.id.activity_statistic_session_info_txt);
        mInfoText.setText(String.format(getString(R.string.activity_statistic_session_info_txt), mKeyList.get(mKeyPosition)));

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.remove(mKeyList.get(mKeyPosition));
                editor.commit();

                finish();
            }
        });

        plotMemoryAnalysis();

    }

    void plotMemoryAnalysis(){

        //Recover the BloodAnalysisSession
        Gson gson = new Gson();
        String json = mPreferences.getString(mKeyList.get(mKeyPosition), "");
        BloodAnalysisSession mAnalysisSession = gson.fromJson(json,BloodAnalysisSession.class);

        //ArrayList<FrameInfo> frameInfoArray = mAnalysisSession.getFramesInfo();

        //Add timestamps to FrameInfo values

        /*for(int i=0;i<frameInfoArray.size();++i){
            if(i==0){
            Instant inst = Instant.EPOCH;
            frameInfoArray.get(i).setmInstant(inst);
            } else {
                frameInfoArray.get(i).setmInstant(Instant.ofEpochMilli(i));
            }

        }*/

        //Log.d("DEBUG :", "float value" + String.valueOf(mAnalysisSession.getFramesInfo().get(0).getPPGValue()));

        // Enable all options on graph
        mLineChart.setScaleEnabled(false);
        mLineChart.setTouchEnabled(true);
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleXEnabled(true);
        mLineChart.setPinchZoom(true);

        // Convert blood analysis session to plots
        mLineChart.setData(GraphTools.BloodAnalysisSessionToLineData(mAnalysisSession));
        // Refresh
        mLineChart.invalidate();
    }
}