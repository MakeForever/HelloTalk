package com.beakya.hellotalk.objs;

import android.graphics.Bitmap;

/**
 * Created by cheolho on 2017. 4. 10..
 */

public class Friend {
    private String id;
    private String name;
    private Bitmap profileImage;
    private boolean isAdded = false;
    public Friend(String id, String name, Bitmap profileImage) {
        this.id = id;
        this.name = name;
        this.profileImage = profileImage;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }
}
