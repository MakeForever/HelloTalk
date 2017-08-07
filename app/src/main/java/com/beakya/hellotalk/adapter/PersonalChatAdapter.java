package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.viewholder.ChatItemViewHolder;
import com.beakya.hellotalk.viewholder.ChatViewHolder;

/**
 * Created by goodlife on 2017. 5. 1..
 */

public class PersonalChatAdapter extends ChatAdapter {

    public static final String TAG = PersonalChatAdapter.class.getSimpleName();
    private PersonalChatRoom mChatRoom;
    public PersonalChatAdapter(Context context, PersonalChatRoom mChatRoom, RecyclerView recyclerView) {
        super(context, recyclerView);
        this.mChatRoom = mChatRoom;
    }
    @Override
    public ChatItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(ChatItemViewHolder holder, int position) {
        Message stringMessage = message.get( (message.size()-1) - position );
            switch (  holder.getItemViewType() ) {
                case VIEW_TYPE_MY_CHAT:
                    holder.bind(stringMessage, myInfo);
                    break;
                case VIEW_TYPE_OTHER_CHAT:
                    holder.bind(stringMessage, mChatRoom.getTalkTo());
                    break;
            }
    }
}
