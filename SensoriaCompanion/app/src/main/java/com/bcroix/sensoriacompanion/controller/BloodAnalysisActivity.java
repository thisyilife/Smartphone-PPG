package com.bcroix.sensoriacompanion.controller;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.internal.annotation.CameraExecutor;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.bcroix.sensoriacompanion.R;
import com.bcroix.sensoriacompanion.model.ImageProcessing;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class BloodAnalysisActivity extends AppCompatActivity {
    // View members
    private PreviewView mPreviewView;
    private Button mStartButton;
    private boolean mAnalysisIsActive;
    private TextView mInfoText;

    // members relevant to cameraX
    private ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;
    // Handle for the image analysis thread
    private Handler mImageAnalysisHandler;
    static final int UI_ENABLE_ANALYSIS_BUTTON = 0;
    static final int UI_DISABLE_ANALYSIS_BUTTON = 1;

    // model members
    private ImageProcessing mImageProcessing;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_analysis);

        // Define model members
        mImageProcessing = new ImageProcessing();

        // Bind view elements
        mPreviewView = findViewById(R.id.activity_blood_analysis_preview);
        mStartButton = findViewById(R.id.activity_blood_analysis_start_button);
        mStartButton.setBackgroundColor(Color.GREEN);
        mStartButton.setEnabled(false);
        mAnalysisIsActive = false;
        mInfoText = findViewById(R.id.activity_blood_analysis_info_txt);
        mInfoText.setText(String.format(getString(R.string.activity_blood_analysis_info_txt), 0));

        // Add actions for button
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAnalysisIsActive){
                    mAnalysisIsActive = false;
                    mStartButton.setText(R.string.activity_blood_analysis_start_button);
                    mStartButton.setBackgroundColor(Color.GREEN);
                    // TODO : save results ?
                }else{
                    mAnalysisIsActive = true;
                    mStartButton.setText(R.string.activity_blood_analysis_stop_button);
                    mStartButton.setBackgroundColor(Color.RED);
                }
            }
        });

        // Provide a camera to recover images
        mCameraProviderFuture = ProcessCameraProvider.getInstance(this);
        mCameraProviderFuture.addListener(() -> {
            try {
                // Bind camera provider to preview and analysis
                ProcessCameraProvider cameraProvider = mCameraProviderFuture.get();
                // Unbind use cases before rebinding
                cameraProvider.unbindAll();
                bindPreviewAndAnalysis(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

        // Specify what the image analysis handler
        mImageAnalysisHandler = new Handler (Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UI_ENABLE_ANALYSIS_BUTTON:
                        mStartButton.setEnabled(true);
                        break;
                    case UI_DISABLE_ANALYSIS_BUTTON:
                        mStartButton.setEnabled(false);
                        break;
                }
            }
        };
    }

    void bindPreviewAndAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        // Configure preview settings
        Preview preview = new Preview.Builder()
                .build();
        // Configure image analysis settings
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        // Non blocking mode : if the computation takes longer than the framerate, then frames will be dropped
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
        // Define the analysis itself
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new ImageAnalysis.Analyzer() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image) {
                Image img = image.getImage();
                // If the analysis is not running yet
                if(!mAnalysisIsActive){
                    // Enable Analysis Button if the image is valid
                    if(ImageProcessing.isImageValid(img)){
                        mImageAnalysisHandler.obtainMessage(UI_ENABLE_ANALYSIS_BUTTON).sendToTarget();
                    }else{
                        mImageAnalysisHandler.obtainMessage(UI_DISABLE_ANALYSIS_BUTTON).sendToTarget();
                    }
                }else{
                    // The analysis is running :
                    mImageProcessing.process(img);
                }

                // Close image to allow to take other frames
                image.close();
            }
        });

        // Select the back camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Link preview from the UI layout
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        // Bind all use cases
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);

        // Turn on flashlight
        camera.getCameraControl().enableTorch(true);
    }
}