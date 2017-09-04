package com.beakya.hellotalk.permission;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goodlife on 2017. 8. 29..
 */

public class PermissionActivity extends Activity {
    private static String TAG = PermissionActivity.class.getSimpleName();
    private static Permission permission;
    private static int PERMISSION_REQUEST_CODE = 123;
    public static void setCallBack( Permission permission ) {
        PermissionActivity.permission = permission;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if ( permission != null && permission.getPermissions() != null ) {
            String[] permissionList = checkPermissions(permission.getPermissions());
            if ( permissionList.length != 0  ) {
                requestPermission(permissionList);
            } else {
                Log.d(TAG, "onCreate: ");
                permission.getCallback().call();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (permission != null) {
            permission = null;
        }
    }
    private String[] checkPermissions(String[] permissions) {
        List<String> list = new ArrayList<>();
        for ( String permission : permissions ) {
            if ( checkPermission(permission) ) {
                list.add(permission);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private boolean isExplanationForPermission(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
    }
    private boolean checkPermission(String permission) {
        return (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED);
    }
    private void requestPermission(String[] permissions) {
        ActivityCompat.requestPermissions(this,
                permissions,
                PERMISSION_REQUEST_CODE);
    }
    private AlertDialog getAlertDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("경고");
        builder.setMessage("해당 기능을 사용할려면 허가가 필요합니다.");
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        return builder.create();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isSuccess = true;
        if ( requestCode == PERMISSION_REQUEST_CODE) {
            for ( int result : grantResults ) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    isSuccess = false;
                }
            }
            if ( isSuccess ) {
                Log.d(TAG, "onRequestPermissionsResult: ");
                permission.getCallback().call();
                finish();
            } else {
                getAlertDialog().show();
            }
        }
    }

}
