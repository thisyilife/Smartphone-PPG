package com.bcroix.sensoriacompanion.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.Image;
import android.telephony.ims.ImsManager;
import android.util.Log;
import android.renderscript.ScriptIntrinsicYuvToRGB;
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

        ByteBuffer y = image.getPlanes()[0].getBuffer();
        ByteBuffer u = image.getPlanes()[1].getBuffer();
        ByteBuffer v = image.getPlanes()[2].getBuffer();

        // Use android import to convert much faster than hand-approach
        // Get YUV channel to buffer
        convertYUVToBitmap(image);

        // compute threshold of the red channel
        //setThreshold(bitImage);

        // if its greater than min expected threshold then its a valid capture
        /*if(mThreshold < minExpectedThreshold){
            return false;
        }*/
        // compute the sum of the intensities greater than the defined threshold
        //computeSumIntensities(bitImage);

        return true;
    }

    /**
     * Convert image into bitmap following format and fill mRed, mBlue, mGreen
     * @param image input to be converted
     * @return bitimage, the converted bitmap image
     */
    public void convertYUVToBitmap(Image image) {
        // retrieve the number of ByteBuffers needed to represent the image
        int format = image.getFormat();
        float R,G,B;
        R = 0; G = 0; B = 0;
        float yValue, vValue, uValue;
        ByteBuffer y = image.getPlanes()[0].getBuffer();
        ByteBuffer u = image.getPlanes()[1].getBuffer();
        ByteBuffer v = image.getPlanes()[2].getBuffer();

        // other format can be added
        switch (format) {
            case ImageFormat.YUV_420_888:
                Log.d("Debug","case YUV");
                int pixelStride = image.getPlanes()[1].getRowStride(); // Pixel stride to get to next line
                int uvIndex = 0; // Corresponding to u and v format index, smaller array
                boolean loopback = true; // Variable to get back to the first element to complete 2x2 blocs

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

                    yValue = y.get(i) ;
                    uValue = u.get(uvIndex);
                    vValue = v.get(uvIndex);
                    // Clamp to get value between [0:255]

                    R += clamp(yValue + (1.370705 * (vValue-128)), 0, 255);
                    G += clamp(yValue - (0.698001 * (vValue-128)) - (0.337633 * (uValue-128)),0, 255);
                    B += clamp(yValue + (1.732446 * (uValue-128)),0,255);
                }

                mRedMean = (int) R/(mHeight*mWidth);
                mGreenMean = (int) G/(mHeight*mWidth);
                mBlueMean = (int) B/(mHeight*mWidth);

                Log.d("DEBUG", "Red pixel : " + mRedMean);
                Log.d("DEBUG", "Green pixel : " + mGreenMean);
                Log.d("DEBUG", "Blue pixel : " + mBlueMean);
                break;

            default:
                break;
        }
       // return null;
    }



}