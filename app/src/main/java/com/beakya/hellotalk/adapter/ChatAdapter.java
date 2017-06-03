package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by goodlife on 2017. 5. 1..
 */

public class ChatAdapter  extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final String TAG = ChatAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_MY_CHAT = 1;
    private static final int VIEW_TYPE_OTHER_CHAT = 2;
    private static final int VIEW_TYPE_SYSTEM = 3;
    private ArrayList<Message> messages = new ArrayList<>();
    private PersonalChatRoom mChatRoom;
    private Context mContext;
    private String myId;
    private String myName;
    private RecyclerView recyclerView;
    public ChatAdapter(Context context, PersonalChatRoom mChatRoom, RecyclerView recyclerView) {
        this.mContext = context;
        this.mChatRoom = mChatRoom;
        this.recyclerView = recyclerView;
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE);
        myId = preferences.getString(context.getString(R.string.user_id), null);
        myName = preferences.getString(context.getString(R.string.user_name), null);
        if( myId == null) {
            throw new RuntimeException("something wrong");
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
        ChatAdapter.ViewHolder viewHolder = new ChatAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String content;
        String name;
        Message message;
        String creatorId;
        int readCount;
//        mCursor.moveToPosition((mCursor.getCount() - 1) - position);
//        switch (  holder.getItemViewType() ) {
//            case VIEW_TYPE_MY_CHAT:
//                Log.d(TAG, "onBindViewHolder: " + position + " : " + ((mCursor.getCount() - 1) - position));
//                content = mCursor.getString(mCursor.getColumnIndex(TalkContract.Message.MESSAGE_CONTENT));
//                id = mCursor.getString(mCursor.getColumnIndex(TalkContract.Message.CREATOR_ID));
//                String fileName = mContext.getString(R.string.setting_profile_img_name);
//                String extension = mContext.getString(R.string.setting_profile_img_extension);
//                String directory = mContext.getString(R.string.setting_profile_img_directory);
//                //TODO : 비트맵 얻는 메소드 실패 했을때 null 리턴하지 말고 기본 이미지 리턴 하도록
//                Bitmap profileBitmap = Utils.getImageBitmap(mContext, fileName, extension, Arrays.asList(directory));
//
//                holder.bind(content, profileBitmap, id);
//                break;
//            case VIEW_TYPE_OTHER_CHAT:
//                content = mCursor.getString(mCursor.getColumnIndex(TalkContract.Message.MESSAGE_CONTENT));
//                id = mCursor.getString(mCursor.getColumnIndex(TalkContract.Message.CREATOR_ID));
//                Bitmap bitmap = Utils.getImageBitmap(mContext,
//                        mContext.getString(R.string.setting_friends_profile_img_name),
//                        mContext.getString(R.string.setting_profile_img_extension),
//                        Arrays.asList( new String[]{ mContext.getString(R.string.setting_friends_img_directory), id }));
//                holder.bind(content, bitmap, id);
//                break;
//        }
        message = messages.get( (messages.size()-1) - position );
            switch (  holder.getItemViewType() ) {

                case VIEW_TYPE_MY_CHAT:
                    //TODO : 비트맵 얻는 메소드 실패 했을때 null 리턴하지 말고 기본 이미지 리턴 하도록
                    String fileName = mContext.getString(R.string.setting_profile_img_name);
                    String extension = mContext.getString(R.string.setting_profile_img_extension);
                    String directory = mContext.getString(R.string.setting_profile_img_directory);

                    content = message.getMessageContent();

                    Bitmap profileBitmap = Utils.getImageBitmap(mContext, fileName, extension, Arrays.asList(directory));
                    readCount = message.isReadCount();
                    holder.bind(content, profileBitmap, readCount, myName, Utils.timeToString(message.getCreatedTime()) );
                    break;
                case VIEW_TYPE_OTHER_CHAT:
                    content = message.getMessageContent();
                    creatorId = message.getCreatorId();
                    Bitmap bitmap = Utils.getImageBitmap(mContext,
                            mContext.getString(R.string.setting_friends_profile_img_name),
                            mContext.getString(R.string.setting_profile_img_extension),
                            Arrays.asList( new String[]{ mContext.getString(R.string.setting_friends_img_directory), creatorId }));
                    readCount = message.isReadCount();
                    User talker = mChatRoom.getTalkTo();
                    holder.bind(content, bitmap, readCount, talker.getName(), Utils.timeToString(message.getCreatedTime()) );
                    break;
            }
    }

    public void swapCursor(ArrayList<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }
    public void addMessage ( Message message ) {
        messages.add(message);
        Log.d(TAG, "addMessage: " + messages.size());
        notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get( ( messages.size() - 1 ) - position);
        String id = message.getCreatorId();

        if( id.equals( myId ) ) {
            return VIEW_TYPE_MY_CHAT;
        } else if ( id.equals("system")) {
            return VIEW_TYPE_SYSTEM;
        } else {
            return VIEW_TYPE_OTHER_CHAT;
        }
    }

    @Override
    public int getItemCount() {
        if (messages != null)
            return messages.size();
        else
            return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView;
        TextView readCountView;
        ImageView profileImageView;
        TextView createTimeView;
        TextView nameView;
        public ViewHolder(View itemView) {
            super(itemView);
            contentTextView = (TextView) itemView.findViewById(R.id.chat_text_content);
            profileImageView = (ImageView) itemView.findViewById(R.id.chat_user_profile_image_view);
            readCountView = (TextView) itemView.findViewById(R.id.read_count);
            createTimeView = (TextView) itemView.findViewById(R.id.chat_time_view);
            nameView = (TextView) itemView.findViewById(R.id.chat_name_view);

        }

        public void bind(String content, Bitmap profileImg, int readCount, String name, String time) {
            if( readCount == 0 ) {
                readCountView.setVisibility(View.INVISIBLE);
            } else {
                readCountView.setVisibility(View.VISIBLE);
                readCountView.setText(String.valueOf(readCount));
            }
            contentTextView.setText(content);
            profileImageView.setImageBitmap(profileImg);
            createTimeView.setText(time);
            nameView.setText(name);
        }
    }
}
