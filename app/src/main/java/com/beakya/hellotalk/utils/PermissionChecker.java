package com.beakya.hellotalk.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by goodlife on 2017. 8. 7..
 */

public class PermissionChecker {

    public static final int PHOTO_REQUEST_CODE = 222;
    private Context mContext;
    public PermissionChecker(Context context) {
        this.mContext = context;
    }
//    public void getPermission(final Activity activity ) {
//
//    }


    public boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED;
    }
    public void requestPermission( String permission, explanationCallBack callBack, int requestCode ) {
        if (ContextCompat.checkSelfPermission(mContext,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                    permission)) {
                callBack.explanation();
            } else {
                ActivityCompat.requestPermissions((Activity) mContext,
                        new String[]{ permission },
                        requestCode);
            }
        }
    }
    public interface explanationCallBack {
        public void explanation();
    }
}
