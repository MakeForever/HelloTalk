package com.beakya.hellotalk.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.beakya.hellotalk.R;

/**
 * Created by goodlife on 2017. 7. 13..
 */

public class ToolBarActivity extends AppCompatActivity {
    Toolbar toolbar;

    protected void setToolbarContentView ( @Nullable int activityResId ) {
        setContentView(activityResId);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item.getItemId() == android.R.id.home ) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
