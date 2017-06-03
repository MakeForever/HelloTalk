package com.beakya.hellotalk.objs;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by goodlife on 2017. 5. 27..
 */

public class Message implements Parcelable {
    private String messageId;
    private String creatorId;
    private String messageContent;
    private String chatId;
    private int messageType;
    private String createdTime;
    private boolean isSend;
    private int readCount;
    /* TODO // important
        Parcelable 만들때 하나의 constructor 가 존재해야 하기 때문에 Parcelable 만드는 메소드 수정 및
    *   메세지 데이터 오고 보낼때 전부 다 보내도록 할것 Message 동기화 할것
    */
    public Message(String messageId, String creatorId, String messageContent, String chatId, int messageType, String createdTime, int readCount) {
        this.messageId = messageId;
        this.creatorId = creatorId;
        this.messageContent = messageContent;
        this.chatId = chatId;
        this.messageType = messageType;
        this.readCount = readCount;
        this.createdTime = createdTime;
    }

    public Message(String messageId, String creatorId, String messageContent, String chatId, int messageType, String createdTime, boolean isSend, int readCount) {
        this.messageId = messageId;
        this.creatorId = creatorId;
        this.messageContent = messageContent;
        this.chatId = chatId;
        this.messageType = messageType;
        this.createdTime = createdTime;
        this.isSend = isSend;
        this.readCount = readCount;
    }

    protected Message(Parcel in) {
        messageId = in.readString();
        creatorId = in.readString();
        messageContent = in.readString();
        chatId = in.readString();
        messageType = in.readInt();
        readCount = in.readInt();
        createdTime = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public int isReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(messageId);
        dest.writeString(creatorId);
        dest.writeString(messageContent);
        dest.writeString(chatId);
        dest.writeInt(messageType);
        dest.writeInt(readCount);
        dest.writeString(createdTime);
    }
    public void printAll() {
        System.out.println( "Chat ID : " + chatId );
        System.out.println( "Message ID : " + messageId );
        System.out.println( "Message type :"  + messageType );
        System.out.println( "Message messageContent" + messageContent );
        System.out.println( "Creator ID : " + creatorId );
    }
}
