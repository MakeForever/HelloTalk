package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;

import java.util.List;

/**
 * Created by goodlife on 2017. 5. 1..
 */

public class ChatAdapter  extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private static final int VIEW_TYPE_MY_CHAT = 1;
    private static final int VIEW_TYPE_OTHER_CHAT = 2;
    private static final int VIEW_TYPE_SYSTEM = 3;
    private Cursor cursor;

    public ChatAdapter() {

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from( context );
        View view = null;

        switch ( viewType ) {
            case VIEW_TYPE_MY_CHAT:
                view = inflater.inflate(R.layout.my_chat_item_text, parent, false);
                break;
            case VIEW_TYPE_OTHER_CHAT:
                view = inflater.inflate(R.layout.other_chat_item_text, parent, false);
            case VIEW_TYPE_SYSTEM :
                break;
        }
        ChatAdapter.ViewHolder viewHolder = new ChatAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition( position );
        String content = cursor.getString(cursor.getColumnIndex(TalkContract.Chat.MESSAGE_CONTENT));
        holder.bind( content );
    }

    public void swapCursor( Cursor newCursor ) {
        cursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemViewType(int position) {
        cursor.moveToPosition( position );
        String id = cursor.getString(cursor.getColumnIndex(TalkContract.Chat.SENDER));
        switch ( id ) {
            case "me":
                return VIEW_TYPE_MY_CHAT;
            case "system" :
                return VIEW_TYPE_SYSTEM;
            default :
                return VIEW_TYPE_OTHER_CHAT;
        }
    }

    @Override
    public int getItemCount() {
        if( cursor != null )
            return cursor.getCount();
        else
            return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            contentTextView = (TextView) itemView.findViewById(R.id.chat_text_content);
        }

        public void bind( String content ) {
            contentTextView.setText(content);
        }
    }
}
