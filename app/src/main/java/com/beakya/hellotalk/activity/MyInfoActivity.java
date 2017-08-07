package com.beakya.hellotalk.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.dialog.ChangePasswordDialog;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Socket;

public class MyInfoActivity extends AppCompatActivity {
    private Button changePasswordButton;
    private Context mContext;
    private User myInfo;
    private CircleImageView myProfileImageView;
    Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        mContext = this;
        socket = ((MyApp)getApplicationContext()).getSocket();
        myInfo = Utils.getMyInfo(this);
        TextView test = (TextView) findViewById(R.id.invite_chat_button);
        myProfileImageView = (CircleImageView) findViewById(R.id.user_profile_image_view);
        changePasswordButton = (Button) findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordDialog dialog = new ChangePasswordDialog(mContext);
                dialog.show();
            }
        });
        myProfileImageView.setImageBitmap(myInfo.getProfileImg(this));
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.deleteChatRoom(mContext, "b12a08a870ad2d282a1a073fa396c49bd4a7acfc1e8fa974013b2d91d7c782da");
            }
        });
    }
    private class test extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }
}
