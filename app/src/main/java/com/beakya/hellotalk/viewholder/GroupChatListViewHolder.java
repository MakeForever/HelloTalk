package com.beakya.hellotalk.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.ChatListItem;

/**
 * Created by goodlife on 2017. 6. 2..
 */

public class GroupChatViewHolder extends BaseViewHolder<ChatListItem> {

    public static NewChatViewHolder newInstance(ViewGroup parent) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_chat_item, parent, false);

        return new NewChatViewHolder(itemView, parent.getContext());
    }

    public GroupChatViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(ChatListItem chatListItem) {

    }
}
