package com.bcroix.sensoriacompanion.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bcroix.sensoriacompanion.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class StatisticsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // View members
    private ListView mViewKeyList;
    // Permission Code
    private static final int STORAGE_PERMISSION_CODE = 101;

    // Access to shared memory Members
    private SharedPreferences mPreferences;
    private List<String> mKeyList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        statActivitySetUp();
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        statActivitySetUp();
    }

    private void statActivitySetUp(){
        mViewKeyList = (ListView) findViewById(R.id.mViewKeyList);
        mPreferences = getDefaultSharedPreferences(getApplicationContext());
        mKeyList = new ArrayList<>(mPreferences.getAll().keySet());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                mKeyList );

        mViewKeyList.setAdapter(arrayAdapter);
        mViewKeyList.setOnItemClickListener(this);
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(StatisticsActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(StatisticsActivity.this,
                    new String[] { permission },
                    requestCode);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent();
        intent.setClass(this, StatisticSession.class);
        intent.putExtra("position", position);
        // Or / And
        intent.putExtra("id", id);
        startActivity(intent);
    }
}