package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;
import com.beakya.hellotalk.viewholder.ChatViewHolder;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by goodlife on 2017. 6. 7..
 */

public class GroupChatAdapter extends ChatAdapter {
    GroupChatRoom chatRoom;
    public GroupChatAdapter(Context context,  RecyclerView recyclerView, GroupChatRoom chatRoom) {
        super(context, recyclerView);
        this.chatRoom = chatRoom;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = null;

        switch (viewType) {
            case VIEW_TYPE_MY_CHAT:
                view = inflater.inflate(R.layout.my_chat_item_text, parent, false);
                break;
            case VIEW_TYPE_OTHER_CHAT:
                view = inflater.inflate(R.layout.other_chat_item_text, parent, false);
                break;
            case VIEW_TYPE_SYSTEM:
                break;
        }
        ChatViewHolder viewHolder = new ChatViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        Message message = messages.get((getItemCount() -1 ) - position );
        User messageCreatorInfo = chatRoom.findUser(message.getCreatorId());

//        if ( holder.getItemViewType() == ChatAdapter.VIEW_TYPE_MY_CHAT ) {
//            String fileName = mContext.getString(R.string.setting_profile_img_name);
//            String extension = mContext.getString(R.string.setting_profile_img_extension);
//            String directory = mContext.getString(R.string.setting_profile_img_directory);
//            String content = message.getMessageContent();
//            Bitmap profileBitmap = Utils.getImageBitmap(mContext, fileName, extension, Arrays.asList(directory));
//            int readCount = message.isReadCount();
//            holder.bind(content, profileBitmap, readCount, myInfo.getName(), Utils.timeToString(message.getCreatedTime()) );
//        } else if ( holder.getItemViewType() == ChatAdapter.VIEW_TYPE_OTHER_CHAT ) {
//            String content = message.getMessageContent();
//            String creatorId = message.getCreatorId();
//            Bitmap bitmap = Utils.getImageBitmap(mContext,
//                    mContext.getString(R.string.setting_friends_profile_img_name),
//                    mContext.getString(R.string.setting_profile_img_extension),
//                    Arrays.asList( new String[]{ mContext.getString(R.string.setting_friends_img_directory), creatorId }));
//            int readCount = message.isReadCount();
//            User user = chatRoom.getUsers().get(creatorId);
//            holder.bind(content, bitmap, readCount, user.getName(), Utils.timeToString(message.getCreatedTime()) );
//        }
        holder.bind(message, messageCreatorInfo);

    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void swapCursor(ArrayList<Message> messages) {
        super.swapCursor(messages);
    }

    @Override
    public void addMessage(Message message) {
        super.addMessage(message);
    }
}
