package com.bcroix.sensoriacompanion.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
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
     * The mean of the frame's red channel
     */
    private int mGreenMean;

    /**
     * The mean of the frame's red channel
     */
    private int mBlueMean;

    /**
     * The threshold of the frame's red channel
     */
    private int mThreshold;

    /**
     * sum of pixels with intensity greater than the computed threshold
     */
    private int mSumRedIntensity;

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
     *  Getter for mSumRedIntensity
     * @return the current frame's sum red intensity
     */
    public int getSumRedIntensity(){
        return mSumRedIntensity;
    }

    /**
     * Getter for mRedMean
     * @return the red level of a frame
     */
    public int getRedMean(){
        return mRedMean;
    }

    /**
     * Getter for mRedMean
     * @return the red level of a frame
     */
    public int getBlueMean(){
        return mBlueMean;
    }

    /**
     * Getter for mRedMean
     * @return the red level of a frame
     */
    public int getGreenMean(){
        return mGreenMean;
    }

    /**
     * Getter for the red channel intensity
     * @return the min and max intensity of the red channel in an array
     */
    public int[] getMinMaxIntensity(Bitmap bitImage){
        int pixel = 0;
        int maxRedIntensity=0;
        int minRedIntensity=255;
        int[] minMaxRedIntensity = new int[2];
        for(int y = 0; y < mHeight; y++){
            for(int x = 0; x < mWidth; x++){
                pixel = bitImage.getPixel(x,y);
                if(Color.red(pixel) >maxRedIntensity)
                    maxRedIntensity = Color.red(pixel);
                if(Color.red(pixel) <minRedIntensity)
                    minRedIntensity = Color.red(pixel);
            }
        }
        minMaxRedIntensity[0]=minRedIntensity;
        minMaxRedIntensity[1]=maxRedIntensity;
        return minMaxRedIntensity;
    }
    /**
     * Fill all members with relevant information, according to the given image
     * @param bitImage of the frame
     * @return the threshold of the intensity of the red channel
     */
    public void setThreshold(Bitmap bitImage)
    {
        mThreshold = (int)0.99*(getMinMaxIntensity(bitImage)[1] -getMinMaxIntensity(bitImage)[0]);
    }

    public void computeSumIntensities(Bitmap bitImage)
    {
        mSumRedIntensity=0;
        int pixel =0;
        for(int y = 0; y < mHeight; y++){
            for(int x = 0; x < mWidth; x++){
                pixel = bitImage.getPixel(x,y);
                if (Color.red(pixel) >= mThreshold)
                    mSumRedIntensity += Color.red(pixel);
            }
        }
    }

    /**
     * Function to compute the mean of every pixel
     * @param bitImage, image captured by the camera
     */
    public void computeFrameMean(Bitmap bitImage){
        int pixel = 0;
        mRedMean = 0;
        mGreenMean = 0;
        mBlueMean = 0;
        // Get R,G,B pixel value
        for(int y = 0; y < mHeight; y++){
            for(int x = 0; x < mWidth; x++){
                pixel = bitImage.getPixel(x,y);
                mRedMean += Color.red(pixel);
                mGreenMean += Color.green(pixel);
                mBlueMean += Color.blue(pixel);
            }
        }
        // Divide every value by the size of the frame to get the mean
        mRedMean /= (mWidth * mHeight);
        mGreenMean /= (mWidth * mHeight);
        mBlueMean /= (mWidth * mHeight);
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
        int minExpectedThreshold = 50;
        // Convert the image to Bitmap to allow pixel operation
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

        // compute threshold of the red channel
        setThreshold(bitImage);

        // if its greater than min expected threshold then its a valid capture
        if(mThreshold < minExpectedThreshold){
            return false;
        }
        // compute the sum of the intensities greater than the defined threshold
        computeSumIntensities(bitImage);

        return true;
    }
}
