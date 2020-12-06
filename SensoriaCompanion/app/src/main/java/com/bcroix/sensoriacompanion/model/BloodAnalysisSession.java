package com.bcroix.sensoriacompanion.model;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;


public class BloodAnalysisSession {
    /**
     * Ordered collection of all frameInfo since the beginning of the session
     */
    List<FrameInfo> mFramesInfo;

    private int mCardiacCycle;

    public BloodAnalysisSession(){
        //TODO : maybe complete constructor
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

    public void computeCycle()
    {
        for (FrameInfo frame : mFramesInfo)
        {
            //TODO: compute the number of frames per cycle
            // the difference between two minima will define the cardiac cycle
        }
    }
}
