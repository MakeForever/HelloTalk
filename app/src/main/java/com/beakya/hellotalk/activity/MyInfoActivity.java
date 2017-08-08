package com.beakya.hellotalk.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.dialog.ChangePasswordDialog;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.PermissionChecker;
import com.beakya.hellotalk.utils.Utils;

import java.io.File;

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
    private PermissionChecker permissionChecker;
    private LinearLayout rootView;
    private Uri uri;
    private static final String PACKAGE_URL_SCHEME = "package:";
    Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        mContext = this;
        socket = ((MyApp)getApplicationContext()).getSocket();
        myInfo = Utils.getMyInfo(this);
        rootView = (LinearLayout) findViewById(R.id.root_view);
        TextView nameTextView = (TextView) findViewById(R.id.name_text_view);
        myProfileImageView = (CircleImageView) findViewById(R.id.user_profile_image_view);
        changePasswordButton = (Button) findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordDialog dialog = new ChangePasswordDialog(mContext);
                dialog.show();
//                MyApp myApp = (MyApp) getApplication();
//                Socket socket = myApp.getSocket();
//                Byte[] test = new Byte[1];
//                test[0] = 0x11;
//                socket.emit("test", test);
            }
        });
        btnForChangeProfileImg = (Button) findViewById(R.id.btn_for_change_image_profile);
        btnForChangeProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "profileImgTest", Toast.LENGTH_SHORT).show();
//                startActivityForResult(intent, 200);
                final PermissionChecker checker = new PermissionChecker(mContext);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("이미지 선택")
                        .setPositiveButton("앨범", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                   boolean permission = checker.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                                    if ( permission ) {
                                        startAlbum();
                                    }
                                    checker.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionChecker.explanationCallBack() {
                                        @Override
                                        public void explanation() {
                                            Snackbar snackbar = Snackbar.make(rootView, "사진을 가져 오기 위해 권한 허가가 필요합니다.", 2000);
                                            snackbar.setAction("허가하기", new View.OnClickListener() {
                                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                                @Override
                                                public void onClick(View v) {
                                                    ActivityCompat.requestPermissions((Activity) mContext,
                                                            new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                                                            STORAGE_REQUEST_CODE);
                                                }
                                            });
                                        }
                                    }, STORAGE_REQUEST_CODE);
                                } else {
                                    startAlbum();
                                }
                            }
                        })
                        .setNegativeButton("카메라", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean permission = checker.checkPermission(Manifest.permission.CAMERA);
                                if ( permission ) {
                                    startCamera();
                                } else {
                                    checker.requestPermission(Manifest.permission.CAMERA, new PermissionChecker.explanationCallBack() {
                                        @Override
                                        public void explanation() {
//                                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
//                                            dialogBuilder.setTitle("help");
//                                            dialogBuilder.setMessage("카메라 권한이 있어야 이 기능을 사용 할 수 있습니다.");
//                                            dialogBuilder.setPositiveButton("done", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    dialog.dismiss();
//                                                }
//                                            });
//                                            dialogBuilder.show();
                                            Snackbar snackbar = Snackbar.make(rootView, "카메라를 사용하기 위해 권한 허가가 필요합니다.", 2000);
                                            snackbar.setAction("허가하기", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    ActivityCompat.requestPermissions((Activity) mContext,
                                                            new String[]{ Manifest.permission.CAMERA },
                                                            CAMERA_PERMISSON_REQUEST_CODE);
                                                }
                                            });
                                            snackbar.show();
                                        }
                                    }, CAMERA_PERMISSON_REQUEST_CODE);
                                }
                            }
                        })
                        .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        myProfileImageView.setImageBitmap(myInfo.getProfileImg(this));
        nameTextView.setText(myInfo.getName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ( requestCode == CAMERA_PERMISSON_REQUEST_CODE) {
            if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                startCamera();
            } else {
                showPermissionDismissDialog();
            }
        }
        if ( requestCode == STORAGE_REQUEST_CODE ) {
            if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                startAlbum();
            } else {
                showPermissionDismissDialog();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode != RESULT_OK ) {
            return;
        }
        switch ( requestCode ) {
            case ACTIVITY_RESULT_FOR_EXTERNER_STORAGE:
                cropImage(uri);
                break;
            case ACTIVITY_RESULT_FOR_CROP_IMAGE:
                saveImageAndEmitData(data.getData());
                Toast.makeText(mContext, data.getType(), Toast.LENGTH_SHORT).show();
            default:

        }

    }
    private void saveImageAndEmitData(Uri data) {
    }
    private void showPermissionDismissDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setTitle("help");
        dialogBuilder.setMessage("카메라 권한이 있어야 이 기능을 사용 할 수 있습니다.");
        dialogBuilder.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
                startActivity(intent);
            }
        });
        dialogBuilder.show();
    }
    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, ACTIVITY_RESULT_FOR_CAMERA);
    }
    private void startAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, ACTIVITY_RESULT_FOR_EXTERNER_STORAGE);
    }
    private void cropImage(Uri targetUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(targetUri, "image/*");
        intent.putExtra("outputX", 200); // CROP한 이미지의 x축 크기
        intent.putExtra("outputY", 200); // CROP한 이미지의 y축 크기
        intent.putExtra("aspectX", 1); // CROP 박스의 X축 비율
        intent.putExtra("aspectY", 1); // CROP 박스의 Y축 비율
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, ACTIVITY_RESULT_FOR_CROP_IMAGE); // CROP_FROM_CAMERA case문 이동
    }
}
