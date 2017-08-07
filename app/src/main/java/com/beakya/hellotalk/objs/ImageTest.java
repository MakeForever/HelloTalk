package com.beakya.hellotalk.objs;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by goodlife on 2017. 6. 20..
 */

public class ImageTest extends Test<Bitmap> implements Parcelable {


    public ImageTest(String messageId, String creatorId, Bitmap messageContent, String chatId, int messageType, String createdTime, boolean isSend, int readCount) {
        super(messageId, creatorId, messageContent, chatId, messageType, createdTime, isSend, readCount);
    }

    protected ImageTest(Parcel in) {
        super(in.readString(),
                in.readString(),
                (Bitmap) in.readParcelable(Bitmap.class.getClassLoader()), in.readString(), in.readInt(), in.readString(), in.readByte() != 0, in.readInt());
    }

    public static final Creator<ImageTest> CREATOR = new Creator<ImageTest>() {
        @Override
        public ImageTest createFromParcel(Parcel in) {
            return new ImageTest(in);
        }

        @Override
        public ImageTest[] newArray(int size) {
            return new ImageTest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(messageId);
        dest.writeString(creatorId);
        dest.writeParcelable(messageContent, flags);
        dest.writeString(chatId);
        dest.writeInt(messageType);
        dest.writeString(createdTime);
        dest.writeByte((byte) (isSend ? 1 : 0));
        dest.writeInt(readCount);
    }
}
