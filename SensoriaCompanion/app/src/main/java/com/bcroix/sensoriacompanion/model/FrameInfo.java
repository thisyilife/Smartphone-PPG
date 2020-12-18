


package com.bcroix.sensoriacompanion.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.Image;
import android.provider.MediaStore;
import android.telephony.ims.ImsManager;
import android.util.Log;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.widget.SimpleAdapter;

import androidx.camera.core.internal.utils.ImageUtil;

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

    /**
     * Return PPG value, used to compute heartbeat
     * @return PPG value
     */
    public float getPPGValue()
    {
        return mSumRedIntensity;
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
        mRedMean = 0;
        mGreenMean = 0;
        mBlueMean = 0;
        // Get image info and set threshold for valid capture
        mWidth = image.getWidth();
        mHeight = image.getHeight();
        int minExpectedThreshold = 50;

        // Convert the image to Bitmap to allow pixel operation
        Log.d("DEBUG", "image format :" + image.getFormat());

        // Use android import to convert much faster than hand-approach
        // Get YUV channel to buffer
        Bitmap bitImage = convertYUVToBitmap(image);

        //computeFrameMean(bitImage);


        // Example to print value in the console
        Log.d("DEBUG", "R pixel : " + mRedMean);
        Log.d("DEBUG", "G pixel : " + mGreenMean);
        Log.d("DEBUG", "B pixel : " + mBlueMean);


        // compute threshold of the red channel
        setThreshold(bitImage);

        // if its greater than min expected threshold then its a valid capture

        if(mThreshold < minExpectedThreshold){
            //return false;
        }

        // compute the sum of the intensities greater than the defined threshold
        computeSumIntensities(bitImage);

        return true;
    }

    /**
     * Convert image into bitmap following format and fill mRed, mBlue, mGreen
     * @param image input to be converted
     * @return bitimage, the converted bitmap image
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
     * Convert a yuv_420_888 image type to Bitmap type
     * @param image to be converted
     * @return The bitmap from the input image
     */
    Bitmap yuv420ToBitmap(Image image){
        int pixelStride = image.getPlanes()[1].getRowStride(); // Pixel stride to get to next line
        int uvIndex = 0;          // Corresponding to u and v format index, smaller array
        boolean loopback = true;  // Variable to get back to the first element to complete 2x2 blocs
        // Variable for visibility
        int R = 0, G = 0, B = 0;
        int yValue, vValue, uValue;

        ByteBuffer y = image.getPlanes()[0].getBuffer();
        ByteBuffer u = image.getPlanes()[1].getBuffer();
        ByteBuffer v = image.getPlanes()[2].getBuffer();


        // Int array to be converted to Bitmap
        int[] argbArray = new int[y.capacity()];
        // Going through every elements of our array to compute RGB value from YUV

        for(int i = 0; i < y.capacity(); i++){
            // Increment uvIndex every 2 steps
            if(i%2 == 1){
                uvIndex++;
            }
            // Go back to the first element of our table if necessary
            if(uvIndex == pixelStride){
                if (loopback){
                    uvIndex -= pixelStride - 1;
                    loopback = false;
                }
                else {
                    loopback = true;
                }
            }

            /* YUV is positive, & 0xff result in unsigned value */
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
        }

        mRedMean /= (mWidth * mHeight);
        mGreenMean /= (mWidth * mHeight);
        mBlueMean /= (mWidth * mHeight);
        return Bitmap.createBitmap(argbArray, mWidth, mHeight, Bitmap.Config.ARGB_8888);
    }

}