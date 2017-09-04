package com.beakya.hellotalk.permission;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.LinearLayout;

import com.beakya.hellotalk.R;

/**
 * Created by goodlife on 2017. 8. 29..
 */

public class ImageCropDialog extends Dialog {
    private LinearLayout cameraLayout;
    private LinearLayout galleryLayout;
    private int requestCode;
    private Uri outputUri;
    private Context mContext;
    ImageCropCallBack callBack;

    public ImageCropDialog(@NonNull Context context, ImageCropCallBack callBack ) {
        super(context);
        this.callBack = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_image_crop);
        cameraLayout = (LinearLayout) findViewById(R.id.camera_layout);
        galleryLayout = (LinearLayout) findViewById(R.id.gallery_layout);
        mContext = getContext();
        initialize();
    }

    private void initialize() {
        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionBuilder.get()
                        .permissions(Manifest.permission.CAMERA)
                        .setCallBack(callBack.camera()).request(mContext);
                dismiss();
            }
        });
        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                PermissionBuilder.get()
                        .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .setCallBack(callBack.gallery()).request(mContext);
                dismiss();
            }
        });
    }

}
