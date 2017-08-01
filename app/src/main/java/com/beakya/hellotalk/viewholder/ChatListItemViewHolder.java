package com.beakya.hellotalk.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.beakya.hellotalk.adapter.ChatAdapter;
import com.beakya.hellotalk.adapter.ChatListAdapter;
import com.beakya.hellotalk.objs.ChatListItem;

/**
 * Created by goodlife on 2017. 7. 30..
 */

public abstract class ChatListItemViewHolder<ITEM> extends RecyclerView.ViewHolder {
    public ChatListItemViewHolder(View itemView) {
        super(itemView);
    }
    public abstract void bind(ITEM item, ChatListAdapter.onDeleteBtnClickListener mListener );
}
