package com.bcroix.sensoriacompanion.controller;

import com.bcroix.sensoriacompanion.model.FrameInfo;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class GraphTools {
    static List<Entry> FrameInfoArrayToListEntry(ArrayList<FrameInfo> frameInfoArray){
        // Recover number of milliseconds of first FrameInfo
        long minMillis = frameInfoArray.get(0).getInstant().toEpochMilli();
        // Fill Array of entries
        List<Entry> entries = new ArrayList<Entry>();
        for (FrameInfo f : frameInfoArray) {
            // turn your data into Entry objects
            entries.add(new Entry(f.getInstant().toEpochMilli()-minMillis, f.getRedMean()));
        }
        return entries;
    }
}
