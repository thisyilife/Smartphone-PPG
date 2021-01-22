package com.bcroix.sensoriacompanion.controller;

import android.graphics.Color;

import com.bcroix.sensoriacompanion.model.BloodAnalysisSession;
import com.bcroix.sensoriacompanion.model.FrameInfo;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class GraphTools {
    static List<Entry> FrameInfoArrayToListEntry(ArrayList<FrameInfo> frameInfoArray){
        // Recover number of nanoseconds of first FrameInfo
        long minTimestamp = frameInfoArray.get(0).getTimestamp();
        // Fill Array of entries
        List<Entry> entries = new ArrayList<>();
        for (FrameInfo f : frameInfoArray) {
            // turn data into Entry objects
            entries.add(new Entry((float)(f.getTimestamp()-minTimestamp)/(float)1e9, f.getPPGValue()));
        }
        return entries;
    }

    static LineData BloodAnalysisSessionToLineData(BloodAnalysisSession b){
        // Each dataset is a line to plot
        List<ILineDataSet> dataSets = new ArrayList<>();

        /* Compute values of each line to plot ****************************************************/

        List<Entry> ppg_entries = new ArrayList<>();
        List<Entry> hb_entries = new ArrayList<>();

        // Recover number of nanoseconds of first FrameInfo
        long minTimestamp = b.getFramesInfo().get(0).getTimestamp();
        // Fill Array of entries
        for (int i = 0; i<b.getFramesInfo().size(); i++) {
            // turn data into Entry objects
            float time = (float)(b.getFramesInfo().get(i).getTimestamp()-minTimestamp)/1e9f;
            float ppg_value = b.getFramesInfo().get(i).getPPGValue();
            // For PPG plot
            ppg_entries.add(new Entry(time, ppg_value));
            // For heartbeat plot
            hb_entries.add(new Entry(time, b.isHeartbeat(i)? ppg_value : 0));
        }

        /* Set characteristics of each plot *******************************************************/

        LineDataSet ppg_line = new LineDataSet(ppg_entries, "PPG value");
        ppg_line.setColor(Color.RED);
        ppg_line.setValueTextColor(Color.RED);
        ppg_line.setDrawCircles(false);
        ppg_line.setDrawValues(false);
        dataSets.add(ppg_line);

        LineDataSet hb_line = new LineDataSet(hb_entries, "Heartbeats");
        // This will make only the dots visible
        hb_line.enableDashedLine(0, 1, 0);
        hb_line.setCircleColor(Color.BLACK);
        hb_line.setColor(Color.BLACK);
        hb_line.setCircleRadius(2f);
        hb_line.setValueTextColor(Color.BLACK);
        hb_line.setDrawCircles(true);
        hb_line.setDrawValues(false);
        dataSets.add(hb_line);

        return new LineData(dataSets);
    }
}
