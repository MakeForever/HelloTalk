package com.beakya.hellotalk.objs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by goodlife on 2017. 5. 31..
 */

public class PersonalChatRoom extends ChatRoom implements Parcelable {

    private User talkTo;

    public PersonalChatRoom(String chatId, int chatRoomType, boolean isSynchronized, User talkTo) {
        super(chatId, chatRoomType, isSynchronized);
        this.talkTo = talkTo;
    }

    protected PersonalChatRoom(Parcel in) {
        super(in.readString(), in.readInt(), in.readByte() != 0);
        talkTo = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<PersonalChatRoom> CREATOR = new Creator<PersonalChatRoom>() {
        @Override
        public PersonalChatRoom createFromParcel(Parcel in) {
            return new PersonalChatRoom(in);
        }

        @Override
        public PersonalChatRoom[] newArray(int size) {
            return new PersonalChatRoom[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getChatId());
        dest.writeInt(getChatRoomType());
        dest.writeByte((byte) (isSynchronized() ? 1 : 0));
        dest.writeParcelable(talkTo, flags);
    }

    public User getTalkTo() {
        return talkTo;
    }

    public void setTalkTo(User talkTo) {
        this.talkTo = talkTo;
    }

    public String toJson(Message message, User myInfo, String event) {
        JSONObject obj = new JSONObject();
        JSONObject chatRoomJsonObj = new JSONObject();
        JSONObject userJsonObj = new JSONObject();
        JSONObject myJsonObj = new JSONObject();
        try {

            //chatRoom
            chatRoomJsonObj.put(TalkContract.ChatRooms.CHAT_ID, getChatId());
            chatRoomJsonObj.put(TalkContract.ChatRooms.CHAT_ROOM_TYPE, getChatRoomType());
            //User
            userJsonObj.put(TalkContract.User.USER_NAME, talkTo.getName() );
            userJsonObj.put(TalkContract.User.USER_ID, talkTo.getId());
            //myJsonObj
            myJsonObj.put(TalkContract.User.USER_NAME, myInfo.getName() );
            myJsonObj.put(TalkContract.User.USER_ID, myInfo.getId() );
            //message
            obj.put("chat_room", chatRoomJsonObj);
            obj.put("message", message.toJson());
            obj.put("receive", userJsonObj );
            obj.put("from", myJsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

}
