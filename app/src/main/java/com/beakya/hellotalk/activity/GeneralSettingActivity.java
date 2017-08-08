package com.beakya.hellotalk.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.beakya.hellotalk.R;

public class GeneralSettingActivity extends ToolBarActivity {
    Switch messageVibrateSwitch;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        super.setToolbarContentView(R.layout.activity_general_setting);
        messageVibrateSwitch = (Switch) findViewById(R.id.message_vibrate_switch);
        initialize();
    }
    void initialize() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isVibrate = sharedPreferences.getBoolean("is_vibrate", false);
        if ( isVibrate ) {
            messageVibrateSwitch.setChecked(true);
        }
        messageVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean("is_vibrate", isChecked).apply();
            }
        });
    }
}
