package com.beakya.hellotalk.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.beakya.hellotalk.R;

public class GeneralSettingActivity extends ToolBarActivity {
    Switch messageVibrateSwitch;
    public final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setToolbarContentView(R.layout.activity_general_setting);
        messageVibrateSwitch = (Switch) findViewById(R.id.message_vibrate_switch);

        setupSharedPreferences();
    }
    void setupSharedPreferences() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isVibrate = sharedPreferences.getBoolean("is_vibrate", false);
        if ( isVibrate )
            messageVibrateSwitch.setChecked(true);
        messageVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean("is_vibrate", isChecked).apply();
            }
        });
    }
}
