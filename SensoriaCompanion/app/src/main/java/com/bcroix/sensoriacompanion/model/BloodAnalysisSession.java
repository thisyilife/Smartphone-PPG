package com.bcroix.sensoriacompanion.model;


import android.media.Image;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

public class BloodAnalysisSession {

    /**
     * The Default duration required to perform an analysis (in nanoseconds).
     */
    public static final long DEFAULT_ANALYSIS_DURATION = (long)15e9;

    /**
     * The duration used to know if a frame is a heartbeat or not (in nanoseconds).
     */
    public static final long LOOK_FOR_HEARTBEAT_DURATION = (long)2e9;

    /**
     * The number of heart beats used to compute heart pulse value.
     */
    public static final long HEARTBEAT_NUMBER_FOR_PULSE = 10;

    /**
     * Ordered collection of all frameInfo since the beginning of the session.
     */
    ArrayList<FrameInfo> mFramesInfo;

    public BloodAnalysisSession() {
        mFramesInfo = new ArrayList<>();
    }

    public ArrayList<FrameInfo> getFramesInfo() {
        return mFramesInfo;
    }

    /**
     * Processes the input image and save results for this frame.
     * @param image the input image.
     * @return true if all went well.
     */
    public boolean process(Image image, long lastTimestamp){
        // Create FrameInfo and add it to collection
        FrameInfo current = new FrameInfo();
        boolean res = current.fillInfo(image, lastTimestamp);
        mFramesInfo.add(current);
        return res;
    }

    /**
     * Computes the duration between first and last frames.
     * @return Duration in nanoseconds.
     */
    public long getDuration() {
        if(mFramesInfo.size() > 1){
            return mFramesInfo.get(mFramesInfo.size() -1).getTimestamp() - mFramesInfo.get(0).getTimestamp();
        }else{
            return 0;
        }
    }

    /**
     * Compute heartbeat at a given moment.
     * @param index the index of the frameInfo at which heartbeat is to be computed.
     * @return heartbeat value in beats per minute.
     */
    public double getHeartbeatAt(int index) {
        // Search for a certain amount of heartbeats in previous indices
        int nb_heartbeats = 0;
        int index_to_look = index + 1;
        while(nb_heartbeats < BloodAnalysisSession.HEARTBEAT_NUMBER_FOR_PULSE && index_to_look > 0){
            index_to_look -= 1;
            if(isHeartbeat(index_to_look)) nb_heartbeats += 1;
        }
        // Compute total amount of time (in ns) between index_to_look and index
        long duration = mFramesInfo.get(index).getTimestamp() - mFramesInfo.get(index_to_look).getTimestamp();
        // Compute average duration of a heartbeat (in ns)
        double hb_duration = (double)duration / BloodAnalysisSession.HEARTBEAT_NUMBER_FOR_PULSE;
        // Convert to bpm
        return 60e9d / hb_duration;
    }

    /**
     * Returns PPG value of FrameInfo at given index, using previous FrameInfo.
     * @param index the index of the frameInfo to process.
     * @return the PPG value.
     */
    public float getPPGAvg(int index, int nb_frames){
        if(index < 0) return -1;
        // Make sure a good number of frames can be used
        int used_nb_frames = (index - nb_frames + 1 < 0)? index+1 : nb_frames;
        float result = 0;
        for(int i = 0; i<used_nb_frames; i++){
            result += mFramesInfo.get(index).getPPGValue();
        }
        return result / used_nb_frames;
    }

    /**
     * Tells whether a FrameInfo corresponds to a heartbeat or not.
     * The processed FrameInfo is actually the previous one, at index-1.
     * @param index the index of the FrameInfo to process.
     * @return true if it is a heartbeat.
     */
    public boolean isHeartbeat(int index){
        // A heartbeat is a local minimum whose value is less than the average of the last 2-3 seconds frames
        if(index < 2) return false;
        // Look at the previous index because it needs following value
        float previous = mFramesInfo.get(index-2).getPPGValue();
        float current = mFramesInfo.get(index-1).getPPGValue();
        float next = mFramesInfo.get(index).getPPGValue();
        boolean is_local_min = previous > current && current < next;
        if(is_local_min){
            // Compute number of frames needed to average last LOOK_FOR_HEARTBEAT_DURATION
            int nb_frames_for_avg = 0;
            float duration = 0;
            while(duration < LOOK_FOR_HEARTBEAT_DURATION){
                duration += mFramesInfo.get(index-1-nb_frames_for_avg).getTimestamp();
                nb_frames_for_avg++;
                if(index-1-nb_frames_for_avg == 0) break;
            }
            // Return true if the value is less than the average of the last nb_frames_for_avg
            return current < getPPGAvg(index, nb_frames_for_avg);
        }
        return false;
    }
}