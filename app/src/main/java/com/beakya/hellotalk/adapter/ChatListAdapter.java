package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.beakya.hellotalk.objs.ChatListItem;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.viewholder.BaseViewHolder;
import com.beakya.hellotalk.viewholder.ChatListItemViewHolder;
import com.beakya.hellotalk.viewholder.GroupChatViewHolder;
import com.beakya.hellotalk.viewholder.PersonalChatViewHolder;

import java.util.ArrayList;


/**
 * Created by goodlife on 2017. 5. 5..
 */

public class ChatListAdapter  extends RecyclerView.Adapter<ChatListItemViewHolder>  {
    public static final String TAG = ChatListAdapter.class.getSimpleName();

    private ArrayList<ChatListItem> rooms;
    private Context mContext;
    private onDeleteBtnClickListener mListener;

    public ChatListAdapter(Context mContext) {
        this.mContext = mContext;
        this.mListener = new onDeleteBtnClickListener() {
            @Override
            public void onClick(int position) {
                rooms.remove(position);
                notifyItemRemoved(position);
            }
        };
    }

    @Override
    public ChatListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ChatRoom.PERSONAL_CHAT_TYPE) {
            return PersonalChatViewHolder.newInstance(parent);
        } else if ( viewType == ChatRoom.GROUP_CHAT_TYPE ) {
            return GroupChatViewHolder.newInstance(parent);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(ChatListItemViewHolder holder, int position) {
        ChatListItem room = rooms.get( position );
        if( holder instanceof PersonalChatViewHolder) {
            ((PersonalChatViewHolder) holder ).bind( room, mListener );
        } else if ( holder instanceof GroupChatViewHolder) {
            ((GroupChatViewHolder) holder).bind( room, mListener );
        }
    }


    @Override
    public int getItemViewType(int position) {
        ChatListItem chatListItem = rooms.get( position );
        return chatListItem.getChatType();
    }

    @Override
    public int getItemCount() {
        if( rooms == null ) {
            return 0;
        } else {
            return rooms.size();
        }
    }
    public void swapData( ArrayList<ChatListItem> data) {
        rooms = data;
        notifyDataSetChanged();
    }
    public interface onDeleteBtnClickListener {
        public void onClick(int position);
    }
}
