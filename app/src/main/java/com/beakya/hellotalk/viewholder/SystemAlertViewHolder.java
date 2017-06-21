package com.beakya.hellotalk.viewholder;

import android.view.View;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.User;

/**
 * Created by goodlife on 2017. 6. 18..
 */

public class SystemAlertViewHolder extends ChatItemViewHolder {
    TextView textView;
    public SystemAlertViewHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.system_message_content);
    }

    @Override
    public void bind(Message stringMessage, User user) {
        textView.setText(stringMessage.getMessageContent());
    }
}
