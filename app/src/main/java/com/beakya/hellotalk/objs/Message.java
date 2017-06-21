package com.beakya.hellotalk.objs;

import android.os.Parcel;
import android.os.Parcelable;

import com.beakya.hellotalk.database.TalkContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by goodlife on 2017. 5. 27..
 */

public class Message extends Test<String> implements Parcelable {

    public Message(String messageId, String creatorId, String messageContent, String chatId, int messageType, String createdTime, boolean isSend, int readCount) {
        super(
                messageId,
                creatorId,
                messageContent,
                chatId,
                messageType,
                createdTime,
                isSend,
                readCount
        );
    }

    protected Message(Parcel in) {
        super(
                in.readString(),
                in.readString(),
                in.readString(),
                in.readString(),
                in.readInt(),
                in.readString(),
                in.readByte() != 0,
                in.readInt()
        );


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
        dest.writeString(createdTime);
        dest.writeInt(readCount);
        dest.writeByte( (byte) (isSend ? 1 : 0));
    }
    public void printAll() {
        System.out.println( "Chat ID : " + chatId );
        System.out.println( "Message ID : " + messageId );
        System.out.println( "Message type :"  + messageType );
        System.out.println( "Message messageContent" + messageContent );
        System.out.println( "Creator ID : " + creatorId );
    }
    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put(TalkContract.ChatRooms.CHAT_ID, getChatId());
            object.put(TalkContract.Message.MESSAGE_ID, getMessageId());
            object.put(TalkContract.Message.CREATOR_ID, getCreatorId());
            object.put(TalkContract.Message.MESSAGE_CONTENT, getMessageContent());
            object.put(TalkContract.Message.MESSAGE_TYPE, getMessageType());
            object.put(TalkContract.Message.READING_COUNT, isReadCount());
            object.put(TalkContract.Message.CREATED_TIME, getCreatedTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
}
