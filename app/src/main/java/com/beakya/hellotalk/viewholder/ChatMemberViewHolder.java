package com.beakya.hellotalk.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by goodlife on 2017. 6. 15..
 */

public class ChatMemberViewHolder extends BaseViewHolder<User> {
    private CircleImageView imageView;
    private TextView nameTextView;
    private Context mContext;
    public ChatMemberViewHolder(View itemView) {
        super(itemView);
        imageView = (CircleImageView) itemView.findViewById(R.id.user_profile_image_view);
        nameTextView = (TextView) itemView.findViewById(R.id.textView_name);
        mContext = itemView.getContext();
    }

    @Override
    public void bind(User user) {
        imageView.setImageBitmap(user.getProfileImg(mContext));
        nameTextView.setText(user.getName());
    }
}
