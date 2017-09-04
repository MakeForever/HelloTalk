package com.beakya.hellotalk.permission;

import android.content.Context;
import android.content.Intent;

/**
 * Created by goodlife on 2017. 8. 29..
 */

public class PermissionBuilder {
    private Permission permission;

    public PermissionBuilder() {
        this.permission = new Permission();
    }
    public static PermissionBuilder get() {
        return new PermissionBuilder();
    }
    public PermissionBuilder setCallBack(PermissionGrantCallback callBack) {
        permission.setCallback(callBack);
        return this;
    }
    public PermissionBuilder permissions(String... permissions) {
        permission.setPermissions(permissions);
        return this;
    }
    public void request(Context context) {
        Intent intent = new Intent(context, PermissionActivity.class);
        context.startActivity(intent);
        PermissionActivity.setCallBack(permission);
    }
}
