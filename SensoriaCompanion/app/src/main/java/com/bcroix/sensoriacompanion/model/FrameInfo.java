


package com.bcroix.sensoriacompanion.model;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;


public class FrameInfo {
    /**
     * The instant to which the information is relevant, in nanoseconds.
     * Each timestamp is only coherent with the ones taken during an analysis.
     */
    private long mTimestamp;

    /**
     * The frame rate for this image.
     */
    private float mFps;

    /**
     * The width of the camera.
     */
    private int mWidth;

    /**
     * The height of the camera.
     */
    private int mHeight;

    /**
     * The mean of the frame's red channel.
     */
    private int mRedMean;

    /**
     * The mean of the frame's green channel.
     */
    private int mGreenMean;

    /**
     * The mean of the frame's blue channel.
     */
    private int mBlueMean;

    /**
     * The threshold used to compute the PPG value of this frame.
     */
    private float mThreshold;

    /**
     * Minimum expected threshold of any frame.
     * Not set to an accurate value yet.
     */
    static final int minExpectedThreshold = 0;

    /**
     * sum of pixels with intensity greater than the computed threshold.
     */
    private int mSumRedIntensity;

    /*********** Getters **********/
    public long getTimestamp(){
        return mTimestamp;
    }

    public float getFps() {
        return mFps;
    }

    public int getSumRedIntensity(){
        return mSumRedIntensity;
    }

    public int getRedMean(){
        return mRedMean;
    }

    public int getBlueMean(){
        return mBlueMean;
    }

    public int getGreenMean(){
        return mGreenMean;
    }

    /**
     * Function to clamp pixel value
     * @param val to be clamped
     * @param min the lower bound
     * @param max the upper bound
     * @return the clamped value
     */
    public static double clamp(double val, double min, double max){
        return Math.max(min, Math.min(max,val));
    }

    /**
     * Computes the red channel bounds.
     * @return the min and max intensity of the red channel in an array of size 2.
     */
    public int[] getMinMaxIntensity(Bitmap bitImage){
        int[] pixels = new int[mWidth * mHeight];
        bitImage.getPixels(pixels, 0, mWidth,0,0, mWidth, mHeight);
        int redPixel;
        int maxRedIntensity=0;
        int minRedIntensity=255;
        int[] minMaxRedIntensity = new int[2];

        for(int i : pixels){
            redPixel = Color.red(i);
            if(redPixel > maxRedIntensity)
                maxRedIntensity = redPixel;
            if(redPixel < minRedIntensity)
                minRedIntensity = redPixel;
        }

        minMaxRedIntensity[0]=minRedIntensity;
        minMaxRedIntensity[1]=maxRedIntensity;
        return minMaxRedIntensity;
    }

    /**
     * Sets mThreshold according to given Bitmap image.
     * @param bitImage the frame used to compute threshold.
     */
    public void setThreshold(Bitmap bitImage)
    {
        mThreshold = 0.99f*(getMinMaxIntensity(bitImage)[1] - getMinMaxIntensity(bitImage)[0]);
    }

    /**
     * Return PPG value, used to compute heartbeat.
     * @return PPG value.
     */
    public float getPPGValue()
    {
        return mSumRedIntensity;
    }

    /**
     * Compute sum of red pixels above mThreshold.
     * @param bitImage the bitmap image used for computation.
     */
    public void computeSumIntensities(Bitmap bitImage){
        mSumRedIntensity = 0;

        int[] pixels = new int[mWidth * mHeight];
        bitImage.getPixels(pixels, 0, mWidth,0,0, mWidth, mHeight);

        for(int i : pixels){
            if(Color.red(i) >= mThreshold)
                mSumRedIntensity += Color.red(i);
        }
    }

    /**
     * Fill all members with relevant information, according to the given image.
     * @param image the frame to process, directly from camera.
     * @param lastTimestamp the timestamp of the previous processed frame.
     * @return true if the frame is suitable for analysis.
     */
    public boolean fillInfo(Image image, long lastTimestamp){
        mRedMean = 0;
        mGreenMean = 0;
        mBlueMean = 0;
        // Get image info and set threshold for valid capture
        mWidth = image.getWidth();
        mHeight = image.getHeight();
        mTimestamp = image.getTimestamp();
        mFps = 1e9f/(mTimestamp-lastTimestamp);

        // Convert the image to Bitmap to allow pixel operation
        Bitmap bitImage = convertYUVToBitmap(image);

        // compute threshold of the red channel
        setThreshold(bitImage);

        // if its greater than min expected threshold then it is a valid capture
        if(mThreshold < FrameInfo.minExpectedThreshold){
            // Not implemented yet since minExpectedThreshold is not known
            return false;
        }

        // compute the sum of the intensities greater than the defined threshold
        computeSumIntensities(bitImage);

        return true;
    }

    /**
     * Convert image into bitmap following format and fill members mRed, mBlue, mGreen.
     * @param image input to be converted.
     * @return the converted bitmap image.
     */
    public Bitmap convertYUVToBitmap(Image image) {
        // retrieve the number of ByteBuffers needed to represent the image
        int format = image.getFormat();

        switch (format) {
            case ImageFormat.YUV_420_888:
                return yuv420ToBitmap(image);

            default:
                // Return a Bitmap with 0 value for every pixel if format not supported
                return Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        }
    }

    /**
     * Convert a yuv_420_888 image type to Bitmap type.
     * @param image to be converted.
     * @return The bitmap from the input image.
     */
     Bitmap yuv420ToBitmap(Image image){
        int pixelStride = image.getPlanes()[1].getRowStride(); // Pixel stride to get to next line
        int uvIndex = 0;          // Corresponding to u and v format index, smaller array
        boolean loopback = false; // Variable to get back to the first element to complete 2x2 blocs
        // Variable for visibility
        int R = 0, G = 0, B = 0, len;
        int yValue, vValue, uValue;

        ByteBuffer y = image.getPlanes()[0].getBuffer();
        ByteBuffer u = image.getPlanes()[1].getBuffer();
        ByteBuffer v = image.getPlanes()[2].getBuffer();

        len = y.capacity();
        // Int array to be converted to Bitmap
        int[] argbArray = new int[len];
        // Going through every elements of our array to compute RGB value from YUV

        for(int i = 0; i < len; i += 2){
            // Go back to the first element of our table if necessary
            if(uvIndex%(pixelStride) == 0){
                if (loopback){
                    uvIndex -= (pixelStride);
                    loopback = false;
                } else {
                    loopback = true;
                }
            }

            /*computation of index i */
            yValue = (y.get(i) & 0xff);
            uValue = (u.get(uvIndex) & 0xff);
            vValue = (v.get(uvIndex) & 0xff);

            // Clamp to get value between [0:255]
            // YUV used in camera 2 API is JFIF YUV colorspace, the correct formula is :
            R = (int) clamp(yValue + (1.402 * (vValue-128)), 0, 255);
            G = (int) clamp(yValue - (0.71414 * (vValue-128)) - (0.34414 * (uValue-128)),0, 255);
            B = (int) clamp(yValue + (1.772 * (uValue-128)),0,255);

            mRedMean += R;
            mGreenMean += G;
            mBlueMean += B;

            // Alpha channel set to 255, 8 bits for every other values
            argbArray[i] = (255 << 24) | (R & 255) << 16 | (G & 255) << 8 | (B & 255);

            /* computation of index i + 1 */
            yValue = (y.get(i + 1) & 0xff);

            R = (int) clamp(yValue + (1.402 * (vValue-128)), 0, 255);
            G = (int) clamp(yValue - (0.71414 * (vValue-128)) - (0.34414 * (uValue-128)),0, 255);
            B = (int) clamp(yValue + (1.772 * (uValue-128)),0,255);

            mRedMean += R;
            mGreenMean += G;
            mBlueMean += B;

            // Alpha channel set to 255, 8 bits for every other values
            argbArray[i+1] = (255 << 24) | (R & 255) << 16 | (G & 255) << 8 | (B & 255);

            uvIndex++;
        }
        return Bitmap.createBitmap(argbArray, mWidth, mHeight, Bitmap.Config.ARGB_8888);
    }

}