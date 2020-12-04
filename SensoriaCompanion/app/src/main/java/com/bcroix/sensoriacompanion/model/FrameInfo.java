package com.bcroix.sensoriacompanion.model;

import android.media.Image;

import java.time.Instant;
import java.util.Random;

public class FrameInfo {
    /**
     * The instant to which the information is relevant, at which the frame was taken
     */
    private Instant mInstant;

    /**
     * The mean of the frame's red channel
     */
    private int mRedMean;

    public FrameInfo(Instant instant){
        mInstant = instant;
    }

    public Instant getInstant() {
        return mInstant;
    }

    public int getRedMean() {
        return mRedMean;
    }

    /**
     * Fill all members with relevant information, according to the given image
     * @param image the frame to process
     * @return true if the frame if suitable for analysis
     */
    public boolean fillInfo(Image image){
        //TODO : following code is a dummy
        mRedMean = new Random().nextInt(255 + 1);;
        return true;
    }
}
