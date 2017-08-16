package com.beakya.hellotalk.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.utils.PopUpNotificationManager;

/**
 * Created by goodlife on 2017. 7. 13..
 */

public class ToolBarActivity extends AppCompatActivity {
    Toolbar toolbar;
    MyApp myApp;
    MessageReceiveBroadCastReceiver receiver;
    private static PopupWindow window;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new MessageReceiveBroadCastReceiver();
        myApp = (MyApp) getApplicationContext();
        //in app toast setup

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.broad_cast_new_message_receive_action));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myApp.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        clearReferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearReferences();
    }

    protected void setToolbarContentView (int activityResId ) {
        setContentView(activityResId);
    }
    protected void setToolbar ( String title ) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle( title );
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    protected  void setToolbar () {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.drawer);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    private void clearReferences() {
        Activity currActivity = myApp.getCurrentActivity();
        if (this.equals(currActivity))
            myApp.setCurrentActivity(null);
    }
    class MessageReceiveBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = context.getString(R.string.broad_cast_new_message_receive_action);
            if ( intent.getAction().equals(action) ) {
                Message message = intent.getParcelableExtra("message");
                PopUpNotificationManager.getInstance().show(message);

            }
        }
    }

}
