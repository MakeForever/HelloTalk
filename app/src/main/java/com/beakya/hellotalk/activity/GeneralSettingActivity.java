package com.beakya.hellotalk.activity;

import android.app.Activity;
import android.os.Bundle;

import com.beakya.hellotalk.R;

public class GeneralSettingActivity extends ToolBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setToolbarContentView(R.layout.activity_general_setting);
    }
}
