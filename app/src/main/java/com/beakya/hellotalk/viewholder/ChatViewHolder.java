package com.beakya.hellotalk.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.R;
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

    public void bind(Message stringMessage, User user) {
        if (stringMessage.isReadCount() == 0) {
            readCountView.setVisibility(View.INVISIBLE);
        } else {
            readCountView.setVisibility(View.VISIBLE);
            readCountView.setText(String.valueOf(stringMessage.isReadCount()));
        }
        contentTextView.setText(stringMessage.getMessageContent());
        profileImageView.setImageBitmap(user.getProfileImg(context));
        String time = Utils.timeToString(stringMessage.getCreatedTime());
        createTimeView.setText(time);
        nameView.setText(user.getName());
    }
}
