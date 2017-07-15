package com.beakya.hellotalk.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Switch;

import com.beakya.hellotalk.R;

public class GeneralSettingActivity extends ToolBarActivity {
    Switch cameraPermissionSwitch;
    public final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setToolbarContentView(R.layout.activity_general_setting);
        cameraPermissionSwitch = (Switch) findViewById(R.id.camera_permission_switch);
        cameraPermissionSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(GeneralSettingActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            }
        });
    }
    private void handleCameraPermission (Context context) {
        if (ContextCompat.checkSelfPermission( context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {

        }
    }
}
