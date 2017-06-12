package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.beakya.hellotalk.objs.ChatListItem;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.viewholder.BaseViewHolder;
import com.beakya.hellotalk.viewholder.GroupChatListViewHolder;
import com.beakya.hellotalk.viewholder.PersonalChatListViewHolder;

import java.util.ArrayList;


/**
 * Created by goodlife on 2017. 5. 5..
 */

public class ChatListAdapter  extends RecyclerView.Adapter<BaseViewHolder>  {
    public static final String TAG = ChatListAdapter.class.getSimpleName();

    private ArrayList<ChatListItem> rooms;
    private Context mContext;


    public ChatListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ChatRoom.PERSONAL_CHAT_TYPE) {
            return PersonalChatListViewHolder.newInstance(parent);
        } else if ( viewType == ChatRoom.GROUP_CHAT_TYPE ) {
            return GroupChatListViewHolder.newInstance(parent);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        ChatListItem room = rooms.get( position );
        if( holder instanceof PersonalChatListViewHolder) {
            ((PersonalChatListViewHolder) holder ).bind( room );
        } else if ( holder instanceof  GroupChatListViewHolder ) {
            ((GroupChatListViewHolder) holder).bind(room);
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
}
