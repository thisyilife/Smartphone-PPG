package com.bcroix.sensoriacompanion.model;


import android.media.Image;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BloodAnalysisSession {

    /**
     * The Default duration required to perform an analysis
     */
    static final Duration DEFAULT_ANALYSIS_DURATION = Duration.ofSeconds(15);

    /**
     * Ordered collection of all frameInfo since the beginning of the session
     */
    ArrayList<FrameInfo> mFramesInfo;

    public BloodAnalysisSession(){
        //TODO : maybe complete constructor
        mFramesInfo = new ArrayList<FrameInfo>();
    }

    public ArrayList<FrameInfo> getFramesInfo() {
        return mFramesInfo;
    }

    /**
     * Processes the input image and save results for this frame
     * @param image the input image
     * @param instant the instant at which the image was captured
     * @return true if all went well
     */
    public boolean process(Image image, Instant instant) {
        // TODO : maybe complete method
        FrameInfo current = new FrameInfo(instant);
        boolean res = current.fillInfo(image);
        mFramesInfo.add(current);
        return res;
    }

    /**
     * Compute average heartbeat on the whole analysis session
     * @return average heartbeat value in beats per minute
     */
    public double getHeartbeatAverage(){
        // TODO : put relevant code, following one is a dummy
        return new Random().nextInt(120 + 1);
    }

    /**
     * Compute heartbeat at the instant of the last frameInfo
     * @return heartbeat value in beats per minute
     */
    public double getHeartbeatLast(){
        // TODO : put relevant code, following one is a dummy
        return new Random().nextInt(120 + 1);
    }

    /**
     * Compute heartbeat at a given instant
     * @return heartbeat value in beats per minute
     */
    public double getHeartbeatAt(Instant instant){
        // TODO : put relevant code, following one is a dummy
        return new Random().nextInt(120 + 1);
    }
}
