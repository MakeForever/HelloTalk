package com.beakya.hellotalk.viewholder;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.R;

/**
 * Created by goodlife on 2017. 6. 7..
 */

public class ChatViewHolder extends RecyclerView.ViewHolder {

    TextView contentTextView;
    TextView readCountView;
    ImageView profileImageView;
    TextView createTimeView;
    TextView nameView;

    public ChatViewHolder(View itemView) {
        super(itemView);
        contentTextView = (TextView) itemView.findViewById(R.id.chat_text_content);
        profileImageView = (ImageView) itemView.findViewById(R.id.chat_user_profile_image_view);
        readCountView = (TextView) itemView.findViewById(R.id.read_count);
        createTimeView = (TextView) itemView.findViewById(R.id.chat_time_view);
        nameView = (TextView) itemView.findViewById(R.id.chat_name_view);

    }

    public void bind(String content, Bitmap profileImg, int readCount, String name, String time) {
        if (readCount == 0) {
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
