package com.beakya.hellotalk.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.dialog.ChangePasswordDialog;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.permission.ImageCropCallBack;
import com.beakya.hellotalk.permission.ImageCropDialog;
import com.beakya.hellotalk.permission.PermissionGrantCallback;
import com.beakya.hellotalk.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Socket;

//import com.linchaolong.android.imagepicker.ImagePicker;
//import com.linchaolong.android.imagepicker.cropper.CropImage;
//import com.linchaolong.android.imagepicker.cropper.CropImageView;

public class MyInfoActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSON_REQUEST_CODE = 111;
    private static final int ACTIVITY_RESULT_FOR_CAMERA = 112;
    private static final int STORAGE_REQUEST_CODE = 222;
    private static final int ACTIVITY_RESULT_FOR_EXTERNER_STORAGE = 223;
    private static final int ACTIVITY_RESULT_FOR_CROP_IMAGE = 333;
    final int PICK_FROM_CAMERA = 444;
    final int PICK_FROM_GALLERY = 555;
    private Button changePasswordButton;
    private Button btnForChangeProfileImg;
    private Context mContext;
    private User myInfo;
    private CircleImageView myProfileImageView;
    private LinearLayout rootView;
    private Uri outputUri;

//    ImagePicker imagePicker;
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
//        imagePicker = new ImagePicker();
//        imagePicker.setTitle("이미지 선택");
//        imagePicker.setCropImage(true);

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
                ImageCropDialog dialog = new ImageCropDialog(mContext, new ImageCropCallBack() {
                    @Override
                    public PermissionGrantCallback camera() {
                        return new PermissionGrantCallback() {
                            @Override
                            public void call() {
                                File imagePath = new File(getFilesDir(), "images");
                                File file = new File(imagePath, "test.png");
                                if ( !imagePath.exists() ) {
                                    file.mkdirs();
                                }
                                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                outputUri = FileProvider.getUriForFile(mContext, "com.beakya.hellotalk.provider", file);
                                cameraIntent.putExtra("test", outputUri);
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                                startActivityForResult(cameraIntent, PICK_FROM_CAMERA);
                            }
                        };
                    }
                    @Override
                    public PermissionGrantCallback gallery() {
                        return new PermissionGrantCallback() {
                            @Override
                            public void call() {
                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                photoPickerIntent.setType("image/*");
                                photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(photoPickerIntent, PICK_FROM_GALLERY);
                            }
                        };
                    }
                });
                dialog.show();
            }
        });
        myProfileImageView.setImageBitmap(myInfo.getProfileImg(this));
        nameTextView.setText(myInfo.getName());
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK) {
            startCropImageActivity(outputUri);
        }
        if ( requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK ) {
            final Uri imageUri = data.getData();
            if ( imageUri != null ) {
                startCropImageActivity(imageUri);
            }
        }
        if ( requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK ) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri imgUrl = result.getUri();
            RequestOptions options = new RequestOptions();
            options.centerCrop().override(128, 128);
            Glide.with(MyInfoActivity.this).asBitmap().load(imgUrl).apply(options).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    myProfileImageView.setImageBitmap(resource);
                    Socket socket = ( (MyApp) getApplication() ).getSocket();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    socket.emit("change_new_profile_image", stream.toByteArray());
                    File imagePath = new File(getFilesDir(), "images");
                    File file = new File(imagePath, "test.png");
                    file.delete();
                    Utils.setMyProfileImage(mContext, resource);
                }
            });
        }
    }
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }
}
