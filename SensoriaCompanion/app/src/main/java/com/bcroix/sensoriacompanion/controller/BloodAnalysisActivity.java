package com.bcroix.sensoriacompanion.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.bcroix.sensoriacompanion.R;
import com.bcroix.sensoriacompanion.model.BloodAnalysisSession;
import com.bcroix.sensoriacompanion.model.FrameInfo;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class BloodAnalysisActivity extends AppCompatActivity {
    // View members
    private PreviewView mPreviewView;
    private Button mStartButton;
    private boolean mAnalysisIsActive;
    private TextView mInfoText;

    // Permission code
    private static final int CAMERA_PERMISSION_CODE = 100;

    // members relevant to cameraX
    private ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;
    // Handle for the image analysis thread
    private Handler mImageAnalysisHandler;
    static final int UI_ENABLE_ANALYSIS_BUTTON = 0;
    static final int UI_DISABLE_ANALYSIS_BUTTON = 1;

    // model members
    private BloodAnalysisSession mBloodAnalysisSession;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_analysis);

        //Check permissions
        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);

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
                    // Define new image processing item
                    mBloodAnalysisSession = new BloodAnalysisSession();
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

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(BloodAnalysisActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(BloodAnalysisActivity.this,
                    new String[] { permission },
                    requestCode);
        }
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
                // Recover image
                Image img = image.getImage();
                // Recover instant
                Instant now = Instant.now();
                // If the analysis is not running yet
                if(!mAnalysisIsActive){
                    FrameInfo currentFrameInfo = new FrameInfo(Instant.now());
                    // Enable Analysis Button if the image is valid
                    if(currentFrameInfo.fillInfo(img)){
                        mImageAnalysisHandler.obtainMessage(UI_ENABLE_ANALYSIS_BUTTON).sendToTarget();
                    }else{
                        mImageAnalysisHandler.obtainMessage(UI_DISABLE_ANALYSIS_BUTTON).sendToTarget();
                    }
                }else{
                    // The analysis is running :
                    mBloodAnalysisSession.process(img, now);
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