package com.beakya.hellotalk.objs;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by goodlife on 2017. 6. 20..
 */

public class Test<T> {
    String messageId;
    String creatorId;
    T messageContent;
    String chatId;
    int messageType;
    String createdTime;
    boolean isSend;
    int readCount;

    public Test(String messageId, String creatorId, T messageContent, String chatId, int messageType, String createdTime, boolean isSend, int readCount) {
        this.messageId = messageId;
        this.creatorId = creatorId;
        this.messageContent = messageContent;
        this.chatId = chatId;
        this.messageType = messageType;
        this.createdTime = createdTime;
        this.isSend = isSend;
        this.readCount = readCount;
    }
}
