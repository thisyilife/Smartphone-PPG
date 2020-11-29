package com.bcroix.sensoriacompanion.controller;

import android.graphics.Color;
import android.os.Bundle;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_analysis);

        // Bind view elements
        mPreviewView = findViewById(R.id.activity_blood_analysis_preview);
        mStartButton = findViewById(R.id.activity_blood_analysis_start_button);
        mStartButton.setBackgroundColor(Color.GREEN);
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

        // Configure image analysis object
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        // Non blocking mode : if the computation takes longer than the framerate, then frames will be dropped
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                // TODO : insert code here : the following line is an example
                mInfoText.setText(String.format(getString(R.string.activity_blood_analysis_info_txt), new Random().nextInt((image.getWidth() + 1))));

                // Close image to allow to take other frames
                image.close();
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
                bindPreviewAndAnalysis(cameraProvider, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreviewAndAnalysis(@NonNull ProcessCameraProvider cameraProvider, ImageAnalysis imageAnalysis) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);

        // Turn on flashlight
        camera.getCameraControl().enableTorch(true);
    }
}