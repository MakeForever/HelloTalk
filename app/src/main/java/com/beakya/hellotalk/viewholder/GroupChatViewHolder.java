package com.beakya.hellotalk.viewholder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.GroupChatActivity;
import com.beakya.hellotalk.adapter.ChatListAdapter;
import com.beakya.hellotalk.objs.ChatListItem;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.JsonUtils;
import com.beakya.hellotalk.utils.Utils;
import com.daimajia.swipe.SwipeLayout;
import com.google.gson.JsonObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Socket;

/**
 * Created by goodlife on 2017. 6. 2..
 */

public class GroupChatViewHolder extends ChatListItemViewHolder<ChatListItem> implements View.OnClickListener {
    public static final String TAG = GroupChatViewHolder.class.getSimpleName();
    private static final int MEMBER_MAX = 5;
    private LinearLayout profileImageArea;
    private TextView chatNameTextView;
    private TextView memberCountView;
    private TextView notReadCountView;
    private TextView dateTextView;
    private Context mContext;
    private GroupChatRoom groupChatRoom;
    private SwipeLayout swipeLayout;
    private ConstraintLayout rootLayout;
    private ImageButton deleteButton;
    private boolean isSwiped = false;
    public static GroupChatViewHolder newInstance(ViewGroup parent) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_list_item, parent, false);

        return new GroupChatViewHolder(itemView, parent.getContext());
    }

    public GroupChatViewHolder(View itemView, final Context context) {
        super(itemView);
        mContext = context;
        rootLayout = (ConstraintLayout) itemView.findViewById(R.id.group_constraint_layout);
        profileImageArea = (LinearLayout) itemView.findViewById(R.id.group_image_view_area);
        memberCountView = (TextView) itemView.findViewById(R.id.group_members_count);
        notReadCountView = (TextView) itemView.findViewById(R.id.not_read_count_view);
        dateTextView = (TextView) itemView.findViewById(R.id.date_text_view);
        chatNameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
        rootLayout.setOnClickListener(this);
        swipeLayout =  (SwipeLayout)itemView.findViewById(R.id.my_swipe_layout);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
//                    Log.d(TAG, "onClose: ");
                //when the SurfaceView totally cover the BottomView.
                isSwiped = false;
//                rootLayout.setEnabled(true);
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
//                rootLayout.setEnabled(false);
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
        deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);


    }


    private CircleImageView createImageView(Context context, Bitmap bitmap) {

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
        if ( isSwiped ) {
            Snackbar snackbar = Snackbar.make(rootLayout, "스와이프를 닫고 터치해주세요", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            Intent intent = new Intent(mContext, GroupChatActivity.class);
            intent.putExtra("chatRoom", groupChatRoom);
            intent.putExtra("is_stored", true);
            mContext.startActivity(intent);
        }
    }

    @Override
    public void bind(ChatListItem chatListItem, final ChatListAdapter.onDeleteBtnClickListener mListener) {
        int userCount = 0;
        groupChatRoom = (GroupChatRoom) chatListItem.getChatRoom();
        final HashMap<String, User> map = groupChatRoom.getUsers();
        int childCount = profileImageArea.getChildCount();
        for ( int i = 0;  i < childCount; i++ ) {

            //[ view, view ] 일때 0번의 view를 지우면 1번이 0번으로 인덱스가 변경 된다.
            View view = profileImageArea.getChildAt(0);
            profileImageArea.removeView(view);
        }
        for( User user : map.values()) {
            if ( !user.isMember() )
                continue;
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
        dateTextView.setText(Utils.changeMessageString(chatListItem.getLastMessageCreatedTime()));
        if(chatListItem.getNotReadCount() == 0 ) {
            notReadCountView.setVisibility(View.INVISIBLE);
        } else {
            notReadCountView.setVisibility(View.VISIBLE);
            notReadCountView.setText(String.valueOf(chatListItem.getNotReadCount()));
        }
        chatNameTextView.setText(groupChatRoom.getChatName());

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
                                groupChatRoom.getChatId(),
                                groupChatRoom.getChatRoomType(),
                                Utils.getMyInfo(mContext).getId()
                        );
                        Socket socket = ((MyApp)mContext.getApplicationContext()).getSocket();
                        socket.emit("someone_leave_chat_room", object.toString());
                        Utils.deleteChatRoom(mContext, groupChatRoom.getChatId());
                        mListener.onClick(getAdapterPosition());
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
