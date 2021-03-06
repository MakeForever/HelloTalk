package com.beakya.hellotalk.viewholder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.PersonalChatActivity;
import com.beakya.hellotalk.adapter.ChatListAdapter;
import com.beakya.hellotalk.objs.ChatListItem;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.utils.JsonUtils;
import com.beakya.hellotalk.utils.Utils;
import com.daimajia.swipe.SwipeLayout;
import com.google.gson.JsonObject;

import io.socket.client.Socket;

/**
 * Created by goodlife on 2017. 6. 2..
 */

public class PersonalChatViewHolder extends ChatListItemViewHolder<ChatListItem> {

    private TextView lastChatItemView;
    private ImageView userImageView;
    private TextView nameTextView;
    private TextView notReadCountView;
    private TextView dateTextView;
    private Context mContext;
    private SwipeLayout swipeLayout;
    private ConstraintLayout rootLayout;
    private ImageButton deleteButton;
    private PersonalChatRoom chatRoom;
    private boolean isSwiped = false;
    public static PersonalChatViewHolder newInstance(ViewGroup parent) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_list_item, parent, false);
        return new PersonalChatViewHolder(itemView);
    }

    public PersonalChatViewHolder(View v) {
        super(v);
        mContext = v.getContext();
        rootLayout = (ConstraintLayout) v.findViewById(R.id.personal_chat_constraint_layout);
        lastChatItemView = (TextView) v.findViewById(R.id.last_chat_view);
        userImageView = (ImageView) v.findViewById(R.id.user_profile_image_view);
        nameTextView = (TextView) v.findViewById(R.id.name_text_view);
        notReadCountView = (TextView) v.findViewById(R.id.not_read_count_view);
        dateTextView = (TextView) v.findViewById(R.id.date_text_view);
        swipeLayout =  (SwipeLayout)v.findViewById(R.id.my_swipe_layout);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
//                    Log.d(TAG, "onClose: ");
                //when the SurfaceView totally cover the BottomView.
                isSwiped = false;
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
        deleteButton = (ImageButton) v.findViewById(R.id.delete_button);

    }

    @Override
    public void bind(final ChatListItem chatListItem, final ChatListAdapter.onDeleteBtnClickListener listener ) {

        final PersonalChatRoom mChatRoom = (PersonalChatRoom) chatListItem.getChatRoom();
        this.chatRoom = mChatRoom;
        if(chatListItem.getNotReadCount() == 0 ) {
            notReadCountView.setVisibility(View.INVISIBLE);
        } else {
            notReadCountView.setVisibility(View.VISIBLE);
            notReadCountView.setText(String.valueOf(chatListItem.getNotReadCount()));
        }
        lastChatItemView.setText(chatListItem.getLastMessage());
        nameTextView.setText(mChatRoom.getTalkTo().getName());


        dateTextView.setText(Utils.changeMessageString(chatListItem.getLastMessageCreatedTime()));
        userImageView.setImageBitmap(mChatRoom.getTalkTo().getProfileImg(mContext));

        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( isSwiped ) {
                    Snackbar snackbar = Snackbar.make(rootLayout, "스와이프를 닫고 터치해주세요", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    Intent intent = new Intent(mContext, PersonalChatActivity.class);
                    intent.putExtra("chatRoom", mChatRoom);
                    intent.putExtra("is_stored", true);
                    mContext.startActivity(intent);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("채팅방 나가기");
                builder.setMessage("이 채팅방을 나가시겠습니까?");
                builder.setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JsonObject object = JsonUtils.makeLeaveRoomObj(
                                mContext,
                                mChatRoom.getChatId(),
                                mChatRoom.getChatRoomType(),
                                Utils.getMyInfo(mContext).getId()
                        );
                        Socket socket = ((MyApp) mContext.getApplicationContext()).getSocket();
                        socket.emit("someone_leave_chat_room", object.toString());
                        Utils.deleteChatRoom(mContext, mChatRoom.getChatId());
                        listener.onClick(getAdapterPosition());
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        swipeLayout.close();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        swipeLayout.close();
                    }
                });
                builder.show();
            }
        });
    }
}
