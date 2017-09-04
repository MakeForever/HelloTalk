package com.beakya.hellotalk.objs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.utils.Utils;

import java.util.Arrays;

/**
 * Created by cheolho on 2017. 4. 10..
 */

public class User implements Parcelable {
    public static final String TAG = User.class.getSimpleName();

    private String id;
    private String name;
    private Bitmap profileImage;
    private boolean hasProfileImg;
    private boolean isAdded = false;
    private boolean isMyFriend = false;
    private boolean isMember = true;
    public User(String id, String name, Bitmap profileImage) {
        this.id = id;
        this.name = name;
        this.profileImage = profileImage;
    }

    public User(String id, String name, boolean hasProfileImg) {
        this.id = id;
        this.name = name;
        this.hasProfileImg = hasProfileImg;
    }
    public User(String id, String name, boolean hasProfileImg, boolean isMember) {
        this.id = id;
        this.name = name;
        this.hasProfileImg = hasProfileImg;
        this.isMember = isMember;
    }
    public User(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.isAdded = in.readInt() == 1;
        this.hasProfileImg = in.readByte() != 0;
        this.isMember = in.readByte() != 0;
    }

    public boolean isMyFriend() {
        return isMyFriend;
    }

    public void setMyFriend(boolean myFriend) {
        isMyFriend = myFriend;
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

    public boolean hasProfileImg() {
        return hasProfileImg;
    }

    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }

    public void setToMember(boolean value) {
        this.isMember = value;
    }
    public boolean isMember() {
        return isMember;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Bitmap getProfileImg(Context context) {
        Bitmap bitmapImg;
        if( hasProfileImg == false ) {
            bitmapImg = BitmapFactory.decodeResource(context.getResources(), R.mipmap.default_profile_img);
        } else {
            bitmapImg = Utils.getImageBitmap(
                    context,
                    context.getString(R.string.setting_friends_profile_img_name),
                    context.getString(R.string.setting_profile_img_extension),
                    Arrays.asList( new String[] { context.getString(R.string.setting_friends_img_directory), id }));
        }
        return bitmapImg;
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
        dest.writeByte((byte) (hasProfileImg ? 1 : 0));
        dest.writeByte((byte) (isMember ? 1 : 0));
    }
}
