package com.bcroix.sensoriacompanion.model;


import android.media.Image;

public class ImageProcessing {

    /**
     * Returns whether or not the image is suitable for analysis
     * @param image the input image
     * @return true if the image is suitable for analysis
     */
    static public boolean isImageValid(Image image) {
        // TODO : the following code is a dummy
        return image.getPlanes()[0].getBuffer().getInt(0) > 128;
    }

    /**
     * Processes the input image and save results for this frame
     * @param image the input image
     * @return true if all went well
     */
    public boolean process(Image image) {
        // TODO
        return true;
    }
}
