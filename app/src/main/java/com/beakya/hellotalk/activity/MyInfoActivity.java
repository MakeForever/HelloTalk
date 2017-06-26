package com.beakya.hellotalk.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;

import io.socket.client.Socket;

public class MyInfoActivity extends AppCompatActivity {
    Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        socket = ((MyApp)getApplicationContext()).getSocket();
        TextView test = (TextView) findViewById(R.id.invite_chat_button);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.emit("if_login");
            }
        });
    }
}
