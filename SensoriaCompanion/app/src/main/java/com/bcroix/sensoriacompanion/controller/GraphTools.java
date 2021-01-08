package com.bcroix.sensoriacompanion.controller;

import com.bcroix.sensoriacompanion.model.FrameInfo;
import com.github.mikephil.charting.data.Entry;

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
}
