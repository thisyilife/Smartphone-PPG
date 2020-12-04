package com.bcroix.sensoriacompanion.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;

import java.nio.ByteBuffer;
import java.time.Instant;


public class FrameInfo {
    /**
     * The instant to which the information is relevant, at which the frame was taken
     */
    private Instant mInstant;

    /**
     * The current camera fps
     */
    private int mFps;

    /**
     * The current camera width
     */
    private int mWidth;

    /**
     * The current camera height
     */
    private int mHeight;

    /**
     * The mean of the frame's red channel
     */
    private int mRedMean;


    /**
     * Function to get frame information
     * @param instant the instant when the frame is sent
     */
    public FrameInfo(Instant instant){
        mInstant = instant;
    }

    /**
     *  Getter for mInstant
     * @return the current frame's instant
     */
    public Instant getInstant(){
        return mInstant;
    }

    /**
     * Getter for mRedMean
     * @return the red level of a frame
     */
    public int getmRedMean(){
        return mRedMean;
    }


    /**
     * Fill all members with relevant information, according to the given image
     * @param image the frame to process
     * @return true if the frame is suitable for analysis
     */
    public boolean fillInfo(Image image){
        // Get image info and set threshold for valid capture
        mWidth = image.getWidth();
        mHeight = image.getHeight();
        int threshold = 128;
        int R = 0, G = 0, B = 0;
        int pixel = 0;
        // Convert the image to Bitmap to allow pixel operation
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

        // We can now go through pixel operation with bitImage
        for(int y = 0; y < mHeight; y++){
            for(int x = 0; x < mWidth; x++){
                pixel = bitImage.getPixel(x,y);
                R += Color.red(pixel);
                // G = Color.green(pixel);
                // B = Color.blue(pixel);
            }
        }

        // compute red mean of the image,
        // if its greater than threshold then its a valid capture
        mRedMean = R / (mWidth * mHeight);
        if(mRedMean > threshold){
            return true;
        }

        return false;
    }
}
