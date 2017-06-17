package com.beakya.hellotalk.viewholder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.GroupChatActivity;
import com.beakya.hellotalk.objs.ChatListItem;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by goodlife on 2017. 6. 2..
 */

public class GroupChatListViewHolder extends BaseViewHolder<ChatListItem> implements View.OnClickListener{
    private static final int MEMBER_MAX = 5;
    private LinearLayout profileImageArea;
    private TextView chatNameTextView;
    private TextView memberCountView;
    private TextView notReadCountView;
    private TextView dateTextView;
    private Context mContext;
    private GroupChatRoom groupChatRoom;
    public static GroupChatListViewHolder newInstance(ViewGroup parent) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_list_item, parent, false);

        return new GroupChatListViewHolder(itemView, parent.getContext());
    }

    public GroupChatListViewHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;
        profileImageArea = (LinearLayout) itemView.findViewById(R.id.group_image_view_area);
        memberCountView = (TextView) itemView.findViewById(R.id.group_members_count);
        notReadCountView = (TextView) itemView.findViewById(R.id.not_read_count_view);
        dateTextView = (TextView) itemView.findViewById(R.id.date_text_view);
        chatNameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
        itemView.setOnClickListener(this);
    }

    @Override
    public void bind(ChatListItem chatListItem) {
        int userCount = 0;
        groupChatRoom = (GroupChatRoom) chatListItem.getChatRoom();
        HashMap<String, User> map = groupChatRoom.getUsers();
        int childCount = profileImageArea.getChildCount();
        for ( int i = 0;  i < childCount; i++ ) {

            //[ view, view ] 일때 0번의 view를 지우면 1번이 0번으로 인덱스가 변경 된다.
            View view = profileImageArea.getChildAt(0);
            profileImageArea.removeView(view);
        }
        for( User user : map.values()) {
            CircleImageView imageView = createImageView(mContext, user.getProfileImg(mContext));

            profileImageArea.addView(imageView);
            userCount++;
            if( userCount > 4) {
                break;
            }
        }
        if( map.size() > MEMBER_MAX ){
            memberCountView.setVisibility(View.VISIBLE);
            memberCountView.setText("외 " + (map.size() - MEMBER_MAX) +"명");
        } else {
            memberCountView.setVisibility(View.INVISIBLE);
        }
        dateTextView.setText(Utils.timeToString(chatListItem.getLastMessageCreatedTime()));
        if(chatListItem.getNotReadCount() == 0 ) {
            notReadCountView.setVisibility(View.INVISIBLE);
        } else {
            notReadCountView.setVisibility(View.VISIBLE);
        }
        chatNameTextView.setText(groupChatRoom.getChatName());
    }
    CircleImageView createImageView (Context context, Bitmap bitmap) {

        float d = context.getResources().getDisplayMetrics().density;
        int sizeParam = 40;
        CircleImageView imageView = new CircleImageView(context);
        imageView.setBorderColor(ContextCompat.getColor(context, R.color.colorAccent));
        imageView.setBorderWidth((int) (d*1));
        imageView.setImageBitmap(bitmap);
        int size = (int) (sizeParam * d);
        int leftMargin = (int) (10 * d);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size,size);
        layoutParams.setMargins(0, 0, leftMargin ,0 );
        imageView.setLayoutParams(layoutParams);
        return imageView;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(mContext, GroupChatActivity.class);
        intent.putExtra("chatRoom", groupChatRoom);
        intent.putExtra("is_stored", true);
        mContext.startActivity(intent);
    }
}
