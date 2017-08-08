package com.beakya.hellotalk.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.dialog.ChangePasswordDialog;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.linchaolong.android.imagepicker.ImagePicker;
import com.linchaolong.android.imagepicker.cropper.CropImage;
import com.linchaolong.android.imagepicker.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Socket;

public class MyInfoActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSON_REQUEST_CODE = 111;
    private static final int ACTIVITY_RESULT_FOR_CAMERA = 112;
    private static final int STORAGE_REQUEST_CODE = 222;
    private static final int ACTIVITY_RESULT_FOR_EXTERNER_STORAGE = 223;
    private static final int ACTIVITY_RESULT_FOR_CROP_IMAGE = 333;
    private Button changePasswordButton;
    private Button btnForChangeProfileImg;
    private Context mContext;
    private User myInfo;
    private CircleImageView myProfileImageView;
    private LinearLayout rootView;
    ImagePicker imagePicker;
    private static final String PACKAGE_URL_SCHEME = "package:";
    Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        mContext = this;
        socket = ((MyApp)getApplicationContext()).getSocket();
        myInfo = Utils.getMyInfo(this);
        //
        imagePicker = new ImagePicker();
        imagePicker.setTitle("이미지 선택");
        imagePicker.setCropImage(true);

        rootView = (LinearLayout) findViewById(R.id.root_view);
        TextView nameTextView = (TextView) findViewById(R.id.name_text_view);
        myProfileImageView = (CircleImageView) findViewById(R.id.user_profile_image_view);
        changePasswordButton = (Button) findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordDialog dialog = new ChangePasswordDialog(mContext);
                dialog.show();
            }
        });
        btnForChangeProfileImg = (Button) findViewById(R.id.btn_for_change_image_profile);
        btnForChangeProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChooser();
            }
        });
        myProfileImageView.setImageBitmap(myInfo.getProfileImg(this));
        nameTextView.setText(myInfo.getName());
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
    void startChooser() {
        imagePicker.startChooser(this, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {

            }
            @Override
            public void onCropImage(Uri imageUri) {
                RequestOptions options = new RequestOptions();
                options.centerCrop().override(128, 128);
                Glide.with(MyInfoActivity.this).asBitmap().load(imageUri).apply(options).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        myProfileImageView.setImageBitmap(resource);
                        Socket socket = ( (MyApp) getApplication() ).getSocket();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        socket.emit("change_new_profile_image", stream.toByteArray());
                        Utils.setMyProfileImage(mContext, resource);
                    }
                });
            }
            @Override
            public void cropConfig(CropImage.ActivityBuilder builder) {
                builder.setMultiTouchEnabled(false)
                        .setMinCropResultSize(128, 128)
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(480, 480)
                        .setAspectRatio(1, 1);
            }
            @Override
            public void onPermissionDenied(int requestCode, String[] permissions, int[] grantResults) {
                super.onPermissionDenied(requestCode, permissions, grantResults);
            }
        });
    }
}
