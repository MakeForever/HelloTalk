package com.beakya.hellotalk.viewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.NewChatAdapter;
import com.beakya.hellotalk.objs.User;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by goodlife on 2017. 6. 8..
 */

public class NewChatViewHolder extends BaseViewHolder<User> {
    private TextView nameTextView;
    private TextView emailTextView;
    private CircleImageView profileImageView;
    private RadioButton selectButton;
    private Context mContext;
    private NewChatAdapter adapter;
    boolean isChecked;
    private User user;
    public static NewChatViewHolder newInstance(ViewGroup parent) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_chat_item, parent, false);

        return new NewChatViewHolder(itemView, parent.getContext());
    }


    public NewChatViewHolder(View view, final Context context) {
        super(view);
        this.mContext = context;
        isChecked = false;
        nameTextView = (TextView) view.findViewById(R.id.textView_name);
        emailTextView = (TextView) view.findViewById(R.id.textView_email);
        profileImageView = (CircleImageView) view.findViewById(R.id.user_profile_image_view);
        selectButton = (RadioButton) view.findViewById(R.id.new_chat_radio_button);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( isChecked ) {
                    selectButton.setChecked(false);
                    adapter.deleteMember(user);
                    isChecked = false;

                } else {
                    selectButton.setChecked(true);
                    adapter.addMember(user);
                    isChecked = true;
                }

            }
        });
    }
    public void setAdapter( NewChatAdapter adapter ) {
        this.adapter = adapter;
    }
    @Override
    public void bind(final User user) {
        this.user = user;
        nameTextView.setText(user.getName());
        emailTextView.setText(user.getId());
        profileImageView.setImageBitmap(user.getProfileImg(mContext));
    }
}
