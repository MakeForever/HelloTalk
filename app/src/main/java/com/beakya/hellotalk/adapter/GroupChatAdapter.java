package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.viewholder.ChatItemViewHolder;
import com.beakya.hellotalk.viewholder.ChatViewHolder;
import com.beakya.hellotalk.viewholder.SystemAlertViewHolder;

import java.util.ArrayList;

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
    public ChatItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;

        switch (viewType) {
            case VIEW_TYPE_MY_CHAT:
                view = inflater.inflate(R.layout.my_chat_item_text, parent, false);
                return new ChatViewHolder(view);
            case VIEW_TYPE_OTHER_CHAT:
                view = inflater.inflate(R.layout.other_chat_item_text, parent, false);
                return new ChatViewHolder(view);
            case VIEW_TYPE_SYSTEM:
                view = inflater.inflate(R.layout.system_chat_item, parent, false);
                return new SystemAlertViewHolder(view);
            default:
                throw new RuntimeException("viewType not matched");
        }
    }

    @Override
    public void onBindViewHolder(ChatItemViewHolder holder, int position) {
        if ( holder instanceof ChatViewHolder ) {
            Message stringMessage = message.get((getItemCount() -1 ) - position );
            User messageCreatorInfo = chatRoom.findUser(stringMessage.getCreatorId());
            holder.bind(stringMessage, messageCreatorInfo);
        } else if ( holder instanceof SystemAlertViewHolder ) {
            Message stringMessage = message.get((getItemCount() -1 ) - position );
            holder.bind(stringMessage, null);
        }


    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void swapCursor(ArrayList<Message> message) {
        super.swapCursor(message);
    }

    @Override
    public void addMessage(Message message) {
        super.addMessage(message);
    }
}
