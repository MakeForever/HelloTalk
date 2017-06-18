package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;
import com.beakya.hellotalk.viewholder.ChatItemViewHolder;
import com.beakya.hellotalk.viewholder.ChatViewHolder;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by goodlife on 2017. 6. 7..
 */

public abstract class ChatAdapter  extends RecyclerView.Adapter<ChatItemViewHolder> {
    public static final int VIEW_TYPE_MY_CHAT = 1;
    public static final int VIEW_TYPE_OTHER_CHAT = 2;
    public static final int VIEW_TYPE_SYSTEM = 3;
    protected ArrayList<Message> messages = new ArrayList<>();
    User myInfo;
    Context mContext;
    private RecyclerView recyclerView;
    public ChatAdapter(Context context, RecyclerView recyclerView) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.myInfo = Utils.getMyInfo(context);
    }

    @Override
    public abstract ChatItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType);
    @Override
    public abstract void onBindViewHolder(ChatItemViewHolder holder, int position);

    @Override
    public int getItemCount() {
        if (messages != null)
            return messages.size();
        else
            return 0;
    }
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get( ( messages.size() - 1 ) - position);
        String id = message.getCreatorId();

        if( id.equals( myInfo.getId() ) ) {
            return VIEW_TYPE_MY_CHAT;
        } else if ( id.equals("system")) {
            return VIEW_TYPE_SYSTEM;
        } else {
            return VIEW_TYPE_OTHER_CHAT;
        }
    }
    public void swapCursor(ArrayList<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }
    public void addMessage ( Message message ) {
        messages.add(message);
        notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
    }
    public Message getMessage(int index) {
        return messages.get(index);
    }
}
