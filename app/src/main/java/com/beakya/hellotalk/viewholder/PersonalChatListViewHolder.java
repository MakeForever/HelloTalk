package com.beakya.hellotalk.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.PersonalChatActivity;
import com.beakya.hellotalk.objs.ChatListItem;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.utils.Utils;
import com.daimajia.swipe.SwipeLayout;

/**
 * Created by goodlife on 2017. 6. 2..
 */

public class PersonalChatListViewHolder extends BaseViewHolder<ChatListItem> {

    private TextView lastChatItemView;
    private ImageView userImageView;
    private TextView nameTextView;
    private TextView notReadCountView;
    private TextView dateTextView;
    private Context context;
    private SwipeLayout swipeLayout;
    private ConstraintLayout constraintLayout;
    private boolean isSwiped = false;
    public static PersonalChatListViewHolder newInstance(ViewGroup parent) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_list_item, parent, false);

        return new PersonalChatListViewHolder(itemView);
    }



    public PersonalChatListViewHolder(View v) {
        super(v);
        context = v.getContext();
        constraintLayout = (ConstraintLayout) itemView.findViewById(R.id.personal_chat_constraint_layout);
        lastChatItemView = (TextView) v.findViewById(R.id.last_chat_view);
        userImageView = (ImageView) v.findViewById(R.id.user_profile_image_view);
        nameTextView = (TextView) v.findViewById(R.id.name_text_view);
        notReadCountView = (TextView) v.findViewById(R.id.not_read_count_view);
        dateTextView = (TextView) v.findViewById(R.id.date_text_view);
        swipeLayout =  (SwipeLayout)itemView.findViewById(R.id.my_swipe_layout);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
//                    Log.d(TAG, "onClose: ");
                //when the SurfaceView totally cover the BottomView.
                isSwiped = false;
                constraintLayout.setEnabled(true);
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
//                    Log.d(TAG, "onUpdate: " +" leftOffset :" + leftOffset +"  topOffset: " + topOffset );
                //you are swiping.
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {
//                    Log.d(TAG, "onStartOpen: ");
            }

            @Override
            public void onOpen(SwipeLayout layout) {
//                    Log.d(TAG, "onOpen: ");
                //when the BottomView totally show.
                isSwiped = true;
                constraintLayout.setEnabled(false);
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
//                    Log.d(TAG, "onStartClose: ");
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
//                    Log.d(TAG, "onHandRelease: ");
                //when user's hand released.
            }
        });
    }

    @Override
    public void bind(final ChatListItem chatListItem ) {

        final PersonalChatRoom chatRoom = (PersonalChatRoom) chatListItem.getChatRoom();
        if(chatListItem.getNotReadCount() == 0 ) {
            notReadCountView.setVisibility(View.INVISIBLE);
        } else {
            notReadCountView.setVisibility(View.VISIBLE);
            notReadCountView.setText(String.valueOf(chatListItem.getNotReadCount()));
        }
        lastChatItemView.setText(chatListItem.getLastMessage());
        nameTextView.setText(chatRoom.getTalkTo().getName());


        dateTextView.setText(Utils.timeToString(chatListItem.getLastMessageCreatedTime()));
        userImageView.setImageBitmap(chatRoom.getTalkTo().getProfileImg(context));

        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PersonalChatActivity.class);
                intent.putExtra("chatRoom", chatRoom);
                intent.putExtra("is_stored", true);
                context.startActivity(intent);
            }
        });
    }
}
