package com.beakya.hellotalk.permission;

/**
 * Created by goodlife on 2017. 8. 29..
 */

public class Permission {
    private PermissionGrantCallback callback;
    private String[] permissions;
    private String title;

    public PermissionGrantCallback getCallback() {
        return callback;
    }

    public void setCallback(PermissionGrantCallback callback) {
        this.callback = callback;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}

