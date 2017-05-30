package com.beakya.hellotalk.objs;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by cheolho on 2017. 4. 10..
 */

public class User implements Parcelable {
    public static final String TAG = User.class.getSimpleName();

    private String id;
    private String name;
    private Bitmap profileImage;
    private boolean isAdded = false;
    public User(String id, String name, Bitmap profileImage) {
        this.id = id;
        this.name = name;
        this.profileImage = profileImage;
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public User(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.isAdded = in.readInt() == 1;
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

    @Override
    public int describeContents() {
        return 0;
    }



    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt( isAdded ? 1 : 0);
    }
}
