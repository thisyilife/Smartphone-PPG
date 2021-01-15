package com.bcroix.sensoriacompanion.model;


import android.media.Image;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

public class BloodAnalysisSession {

    /**
     * The Default duration required to perform an analysis
     */
    public static final long DEFAULT_ANALYSIS_DURATION = (long)15e9;

    /**
     * Ordered collection of all frameInfo since the beginning of the session
     */
    ArrayList<FrameInfo> mFramesInfo;

    public BloodAnalysisSession() {
        //TODO : maybe complete constructor
        mFramesInfo = new ArrayList<FrameInfo>();
    }

    public ArrayList<FrameInfo> getFramesInfo() {
        return mFramesInfo;
    }

    /**
     * Processes the input image and save results for this frame
     *
     * @param image   the input image
     * @return true if all went well
     */
    public boolean process(Image image, long lastTimestamp) {
        // TODO : maybe complete method
        FrameInfo current = new FrameInfo();
        boolean res = current.fillInfo(image, lastTimestamp);
        mFramesInfo.add(current);
        return res;
    }

    /**
     * Computes the duration between first and last frames
     * @return Duration in nanoseconds
     */
    public long getDuration() {
        if(mFramesInfo.size() > 1){
            return mFramesInfo.get(mFramesInfo.size() -1).getTimestamp() - mFramesInfo.get(0).getTimestamp();
        }else{
            return 0;
        }
    }

    /**
     * Compute average heartbeat on the whole analysis session
     *
     * @return average heartbeat value in beats per minute
     */
    public double getHeartbeatAverage() {
        // TODO : put relevant code, following one is a dummy
        return new Random().nextInt(120 + 1);
    }

    /**
     * Compute heartbeat at the instant of the last frameInfo
     *
     * @return heartbeat value in beats per minute
     */
    public double getHeartbeatLast() {
        // TODO : put relevant code, following one is a dummy
        return new Random().nextInt(120 + 1);
    }

    /**
     * Compute heartbeat at a given instant
     *
     * @return heartbeat value in beats per minute
     */
    public double getHeartbeatAt(Instant instant) {
        // TODO : put relevant code, following one is a dummy
        return new Random().nextInt(120 + 1);
    }

    /**
     * Returns PPG value of FrameInfo at given index, filtered using previous frame infos
     * @param index the index of the frameInfo to process
     * @return the PPG value
     */
    public float getPPGFiltered(int index){
        // The PPG value is the average of the nb_frame last values
        final int nb_frames = 8;

        if(index < 0) return -1;
        // Make sure a good number of frames can be used
        int used_nb_frames = (index - nb_frames + 1 < 0)? index+1 : nb_frames;
        float result = 0;
        for(int i = 0; i<used_nb_frames; i++){
            result += mFramesInfo.get(index).getPPGValue();
        }
        return result / used_nb_frames;
    }
}