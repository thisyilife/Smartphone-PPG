package com.bcroix.sensoriacompanion.controller;

import com.bcroix.sensoriacompanion.model.FrameInfo;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Date;

public class GraphTools {
    static LineGraphSeries<DataPoint> FrameInfoArrayToLineGraph(ArrayList<FrameInfo> frameInfoArray){
        DataPoint[] tab = new DataPoint[frameInfoArray.size()];

        for(int i = 0; i<frameInfoArray.size(); i++){
            tab[i] = new DataPoint(frameInfoArray.get(i).getInstant().toEpochMilli(), frameInfoArray.get(i).getRedMean());
        }

        return new LineGraphSeries<>(tab);
    }
}
