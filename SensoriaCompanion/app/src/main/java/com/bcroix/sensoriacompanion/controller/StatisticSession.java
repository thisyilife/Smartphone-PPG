package com.bcroix.sensoriacompanion.controller;

import androidx.appcompat.app.AppCompatActivity;
import com.bcroix.sensoriacompanion.R;
import com.bcroix.sensoriacompanion.model.BloodAnalysisSession;
import com.bcroix.sensoriacompanion.controller.GraphTools;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class StatisticSession extends AppCompatActivity {

    private LineChart mLineChart;
    private TextView mInfoText;
    private int mKeyPosition;
    private List<String> mKeyList;
    private SharedPreferences mPreferences;
    //private BloodAnalysisSession mAnalysisSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_session);

        mLineChart = findViewById(R.id.activity_statistic_session_graph);
        mLineChart.getDescription().setEnabled(false);

        //Get previous analysis List
        mPreferences = getDefaultSharedPreferences(getApplicationContext());
        mKeyList = new ArrayList<>(mPreferences.getAll().keySet());

        //Get position of the analysis in the list
        Intent intent = getIntent();
        mKeyPosition = intent.getIntExtra("position",0);

        //Display analysis name
        mInfoText = findViewById(R.id.activity_statistic_session_info_txt);
        mInfoText.setText(String.format(getString(R.string.activity_statistic_session_info_txt), mKeyList.get(mKeyPosition)));

        plotMemoryAnalysis();

    }

    void plotMemoryAnalysis(){

        //Recover the BloodAnalysisSession
        Gson gson = new Gson();
        String json = mPreferences.getString(mKeyList.get(mKeyPosition), "");
        BloodAnalysisSession mAnalysisSession = gson.fromJson(json,BloodAnalysisSession.class);

        //Log.d("DEBUG :", "float value" + String.valueOf(mAnalysisSession.getFramesInfo().get(0).getPPGValue()));

        // Enable all options on graph
        mLineChart.setTouchEnabled(true);
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleXEnabled(true);
        mLineChart.setPinchZoom(true);

        //Plot BloodAnalysisSession data on the graph
        LineDataSet dataSet = new LineDataSet(GraphTools.FrameInfoArrayToListEntry(mAnalysisSession.getFramesInfo()), "PPG Value");
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.RED);
        dataSet.setDrawCircles(false);
        mLineChart.setData(new LineData(dataSet));
        mLineChart.invalidate();
    }
}