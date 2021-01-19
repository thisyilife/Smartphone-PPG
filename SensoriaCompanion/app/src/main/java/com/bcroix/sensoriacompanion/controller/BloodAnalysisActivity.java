package com.bcroix.sensoriacompanion.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
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
import android.widget.Toast;

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

import com.bcroix.sensoriacompanion.R;
import com.bcroix.sensoriacompanion.model.BloodAnalysisSession;
import com.bcroix.sensoriacompanion.model.FrameInfo;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class BloodAnalysisActivity extends AppCompatActivity {
    // View members
    private PreviewView mPreviewView;
    private Button mStartButton;
    private boolean mAnalysisIsActive;
    private LineChart mRedLineChart;
    private TextView mAnalysisParamText;
    private TextView mInfoText;

    // Permission code
    private static final int CAMERA_PERMISSION_CODE = 100;

    // members relevant to cameraX
    private ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;
    // Handle for the image analysis thread
    private Handler mImageAnalysisHandler;
    static final int UI_ENABLE_ANALYSIS_BUTTON = 0;
    static final int UI_DISABLE_ANALYSIS_BUTTON = 1;
    static final int UI_UPDATE_GRAPH = 2;
    static final int UI_UPDATE_FPS_VALUE = 3;
    static final String UI_UPDATE_FPS_VALUE_KEY = "fps";

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
        mRedLineChart = findViewById(R.id.activity_blood_analysis_graph);
        mRedLineChart.getDescription().setEnabled(false);
        mRedLineChart.setNoDataText(getString(R.string.activity_blood_analysis_no_data_graph_txt));
        mInfoText = findViewById(R.id.activity_blood_analysis_info_txt);
        mInfoText.setText(String.format(getString(R.string.activity_blood_analysis_info_txt), 0));
        mAnalysisParamText = findViewById(R.id.activity_blood_analysis_param_txt);
        mAnalysisParamText.setText(String.format(getString(R.string.activity_blood_analysis_param_txt), 0f));

        // Add actions for button
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAnalysisIsActive){
                    // Stop analysis
                    mAnalysisIsActive = false;
                    // Save results if analysis was long enough
                    saveAnalysisSession();

                    // Enable all options on graph while analysis is active
                    mRedLineChart.setTouchEnabled(true);
                    mRedLineChart.setDragEnabled(true);
                    mRedLineChart.setScaleXEnabled(true);
                    mRedLineChart.setPinchZoom(true);
                    // Modify appearance of button
                    mStartButton.setText(R.string.activity_blood_analysis_start_button);
                    mStartButton.setBackgroundColor(Color.GREEN);
                }else{
                    // Define new image processing item
                    mBloodAnalysisSession = new BloodAnalysisSession();
                    mAnalysisIsActive = true;
                    // Disable all options on graph while analysis is active
                    mRedLineChart.setTouchEnabled(false);
                    mRedLineChart.setDragEnabled(false);
                    mRedLineChart.setScaleEnabled(false);
                    mRedLineChart.setPinchZoom(false);
                    // Modify appearance of button
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

        // Specify the image analysis handler to modify GUI from analyze function
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
                    case UI_UPDATE_GRAPH:
                        // Convert blood analysis session to plots
                        mRedLineChart.setData(GraphTools.BloodAnalysisSessionToLineData(mBloodAnalysisSession));
                        // Refresh
                        mRedLineChart.invalidate();
                        break;
                    case UI_UPDATE_FPS_VALUE:
                        mAnalysisParamText.setText(String.format(getString(R.string.activity_blood_analysis_param_txt), Float.parseFloat(msg.getData().getString(UI_UPDATE_FPS_VALUE_KEY))));
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
            private long mLastTimestamp = 0;

            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image) {
                // Recover image
                Image img = image.getImage();

                // A bundle to update fps in GUI in main thread
                Bundle bundleFps = new Bundle();

                // If the analysis is not running yet
                if(!mAnalysisIsActive){
                    FrameInfo currentFrameInfo = new FrameInfo();
                    // Enable Analysis Button if the image is valid
                    if(currentFrameInfo.fillInfo(img, mLastTimestamp)){
                        mImageAnalysisHandler.obtainMessage(UI_ENABLE_ANALYSIS_BUTTON).sendToTarget();
                    }else{
                        mImageAnalysisHandler.obtainMessage(UI_DISABLE_ANALYSIS_BUTTON).sendToTarget();
                    }
                    bundleFps.putString(UI_UPDATE_FPS_VALUE_KEY, Float.toString(currentFrameInfo.getFps()));
                }else{
                    // The analysis is running :
                    mBloodAnalysisSession.process(img, mLastTimestamp);
                    // Update GUI
                    mImageAnalysisHandler.obtainMessage(UI_UPDATE_GRAPH).sendToTarget();
                    bundleFps.putString(UI_UPDATE_FPS_VALUE_KEY, Float.toString(mBloodAnalysisSession.getFramesInfo().get(mBloodAnalysisSession.getFramesInfo().size()-1).getFps()));
                }
                // Save last timestamp
                mLastTimestamp = img.getTimestamp();
                // Send message to update fps
                Message msgFps = mImageAnalysisHandler.obtainMessage(UI_UPDATE_FPS_VALUE);
                msgFps.setData(bundleFps);
                mImageAnalysisHandler.sendMessage(msgFps);

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
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);

        // Turn on flashlight
        camera.getCameraControl().enableTorch(true);
    }

    /**
     * Stores in the app preferences the member BloodAnalysisSession if its duration is long enough
     */
    void saveAnalysisSession(){
        if(mBloodAnalysisSession.getDuration() >= BloodAnalysisSession.DEFAULT_ANALYSIS_DURATION){
            SharedPreferences  pref = getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor prefsEditor = pref.edit();
            Gson gson = new Gson();
            String json = gson.toJson(mBloodAnalysisSession, BloodAnalysisSession.class);
            String key = Instant.now().toString();
            prefsEditor.putString(key, json);
            prefsEditor.apply();
            // Send message to user
            Toast.makeText(BloodAnalysisActivity.this, String.format(getString(R.string.activity_blood_analysis_save_success_toast), key), Toast.LENGTH_SHORT).show();
        }else{
            // Send message to user
            Toast.makeText(BloodAnalysisActivity.this, getString(R.string.activity_blood_analysis_save_failure_toast), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAnalysisIsActive) {
            // Send message to user
            Toast.makeText(BloodAnalysisActivity.this, getString(R.string.activity_blood_analysis_save_failure_toast), Toast.LENGTH_SHORT).show();
        }
    }
}