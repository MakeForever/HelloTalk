package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.utils.Utils;

import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by goodlife on 2017. 5. 1..
 */

public class ChatAdapter  extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final String TAG = ChatAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_MY_CHAT = 1;
    private static final int VIEW_TYPE_OTHER_CHAT = 2;
    private static final int VIEW_TYPE_SYSTEM = 3;
    private Cursor mCursor;
    private Context mContext;
    private String myId;
    private Bitmap bitmapImg;

    public ChatAdapter(Context context) {
        this.mContext = context;
        myId = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE).getString("id", null);
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
            case VIEW_TYPE_SYSTEM:
                break;
        }
        ChatAdapter.ViewHolder viewHolder = new ChatAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition((mCursor.getCount() - 1) - position);
        Log.d(TAG, "onBindViewHolder: " + position + " : " + ((mCursor.getCount() - 1) - position));
        String content = mCursor.getString(mCursor.getColumnIndex(TalkContract.Chat.MESSAGE_CONTENT));
        String id = mCursor.getString(mCursor.getColumnIndex(TalkContract.Chat.CREATOR_ID));
        boolean hasPic;
        if( id.equals(myId) ) {
            hasPic = mContext.getSharedPreferences(mContext.getString(R.string.my_info), MODE_PRIVATE).getBoolean(mContext.getString(R.string.user_img_boolean), false);
        } else {
            Cursor test = mContext.getContentResolver().query(
                TalkContract.User.CONTENT_URI,
                null,
                TalkContract.User.USER_ID + " = ? ",
                new String[]{ id },
                null);

            test.moveToNext();
            hasPic = test.getInt(test.getColumnIndex(TalkContract.User.USER_HAVE_PROFILE_IMAGE)) > 0;
        }
        holder.bind(content, hasPic, id);
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemViewType(int position) {
        mCursor.moveToPosition((mCursor.getCount() - 1) - position);
        String id = mCursor.getString(mCursor.getColumnIndex(TalkContract.Chat.CREATOR_ID));

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
        if (mCursor != null)
            return mCursor.getCount();
        else
            return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView;
        ImageView profileImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            contentTextView = (TextView) itemView.findViewById(R.id.chat_text_content);
            profileImageView = (ImageView) itemView.findViewById(R.id.chat_user_profile_image_view);

        }

        public void bind(String content, boolean hasProfileImg, String id) {
            contentTextView.setText(content);
            if (hasProfileImg == false) {
                profileImageView.setImageResource(R.mipmap.default_profile_img);
            } else {
                String fileName = mContext.getString(R.string.setting_profile_img_name);
                String extension = mContext.getString(R.string.setting_profile_img_extension);
                String directory = mContext.getString(R.string.setting_profile_img_directory);
                Bitmap bitmapImg = Utils.getImageBitmap(mContext,
                        fileName,
                        extension,
                        Arrays.asList(new String[]{ directory }));

                profileImageView.setImageBitmap(bitmapImg);


            }
        }
    }
}
