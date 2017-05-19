package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.ChatActivity;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by goodlife on 2017. 5. 5..
 */

public class ChatListAdapter  extends RecyclerView.Adapter<ChatListAdapter.ViewHolder>  {
    public static final String TAG = ChatListAdapter.class.getSimpleName();

    private ArrayList<ChatRoom> rooms;
    private Context mContext;
    private LayoutInflater inflater;

    public ChatListAdapter(Context mContext) {
        this.mContext = mContext;
        inflater = LayoutInflater.from(this.mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        ViewHolder viewHolder = null;
        if (viewType == TalkContract.ChatRooms.PERSONAL_CHAT_TYPE) {
            view = inflater.inflate(R.layout.chat_list_item, parent, false);
            viewHolder = new ViewHolder(view);
        } else if ( viewType == TalkContract.ChatRooms.GROUP_CHAT_TYPE ) {

        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatRoom room = rooms.get( position );
        holder.bind(room);
    }

    @Override
    public int getItemViewType(int position) {
        ChatRoom chatRoom = rooms.get( position );
        return chatRoom.getChatRoomType();
    }

    @Override
    public int getItemCount() {
        if( rooms == null ) {
            return 0;
        } else {
            return rooms.size();
        }
    }
    public void swapData( ArrayList<ChatRoom> data) {
        rooms = data;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mNameTextView;
        private TextView mLastChatView;
        private ImageView mImageView;
        private ChatRoom chatRoom;
        private TextView notReadCountView;
        private TextView lastChatTimeView;
        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.user_profile_image_view);
            mNameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
            mLastChatView = (TextView) itemView.findViewById(R.id.last_chat_view);
            notReadCountView = (TextView) itemView.findViewById(R.id.not_read_count_view);
            lastChatTimeView = (TextView) itemView.findViewById(R.id.date_text_view);
            itemView.setOnClickListener(this);
        }
        public void bind( ChatRoom room ) {
            this.chatRoom = room;
            ArrayList<User> users = room.getUserList();
            Bitmap profileBitmap = null;
            if( users.size() > 0 ) {
                User user = users.get(0);
                profileBitmap = Utils.getImageBitmap(
                        mContext,
                        mContext.getString(R.string.setting_friends_profile_img_name),
                        mContext.getString(R.string.setting_profile_img_extension),
                        Arrays.asList(new String[] { mContext.getString(R.string.setting_friends_img_directory), user.getId() }));
                mNameTextView.setText(user.getName());
            }
            notReadCountView.setText(String.valueOf(room.getNotReadChatCount()));
            lastChatTimeView.setText(room.getLastContentTimeMessage());
            mImageView.setImageBitmap(profileBitmap);
            mLastChatView.setText(room.getLastContent());

        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent( v.getContext(), ChatActivity.class );
            Log.d(TAG, "onClick: " + chatRoom.getChatId());
            Log.d(TAG, "onClick: " + chatRoom.getUserList());
            Log.d(TAG, "onClick: " + chatRoom.getChatRoomType());
            intent.putExtra(TalkContract.ChatRooms.CHAT_LIST_ID, chatRoom.getChatId());
//            intent.putExtra("receiveList", chatRoom.getUserList());
            intent.putExtra("chatType", chatRoom.getChatRoomType());
            v.getContext().startActivity(intent);
        }
    }
}
