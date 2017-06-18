package com.beakya.hellotalk.viewholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;

/**
 * Created by goodlife on 2017. 6. 7..
 */

public class ChatViewHolder extends ChatItemViewHolder {

    TextView contentTextView;
    TextView readCountView;
    ImageView profileImageView;
    TextView createTimeView;
    TextView nameView;
    Context context;
    public ChatViewHolder(View itemView) {
        super(itemView);
        contentTextView = (TextView) itemView.findViewById(R.id.chat_text_content);
        profileImageView = (ImageView) itemView.findViewById(R.id.chat_user_profile_image_view);
        readCountView = (TextView) itemView.findViewById(R.id.read_count);
        createTimeView = (TextView) itemView.findViewById(R.id.chat_time_view);
        nameView = (TextView) itemView.findViewById(R.id.chat_name_view);
        context = itemView.getContext();
    }

    public void bind(Message message, User user) {
        if (message.isReadCount() == 0) {
            readCountView.setVisibility(View.INVISIBLE);
        } else {
            readCountView.setVisibility(View.VISIBLE);
            readCountView.setText(String.valueOf(message.isReadCount()));
        }
        contentTextView.setText(message.getMessageContent());
        profileImageView.setImageBitmap(user.getProfileImg(context));
        createTimeView.setText(Utils.timeToString(message.getCreatedTime()));
        nameView.setText(user.getName());
    }
}
