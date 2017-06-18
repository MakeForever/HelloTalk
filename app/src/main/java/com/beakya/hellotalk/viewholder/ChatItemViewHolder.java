package com.beakya.hellotalk.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.User;

/**
 * Created by goodlife on 2017. 6. 18..
 */

public abstract class ChatItemViewHolder extends RecyclerView.ViewHolder {
    public ChatItemViewHolder(View itemView) {
        super(itemView);
    }
    public abstract void bind(Message message, User user);
}
