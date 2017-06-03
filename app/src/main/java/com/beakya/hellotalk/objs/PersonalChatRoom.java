package com.beakya.hellotalk.objs;

import android.os.Parcel;
import android.os.Parcelable;

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
}
