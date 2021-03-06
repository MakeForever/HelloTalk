package com.beakya.hellotalk.adapter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Logger;
import com.beakya.hellotalk.utils.Utils;
import com.daimajia.swipe.SwipeLayout;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Socket;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cheolho on 2017. 3. 24..
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    public static final String TAG = UserAdapter.class.getSimpleName();
    private Cursor mCursor;
    private Context mContext;
    private mOnClickListener mListener;
    public UserAdapter(Context context, mOnClickListener listener ) {
        mContext = context;
        mListener = listener;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from( context );
        View view = inflater.inflate(R.layout.user_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String name = mCursor.getString(mCursor.getColumnIndex(TalkContract.User.USER_NAME));
        String id = mCursor.getString(mCursor.getColumnIndex(TalkContract.User.USER_ID));
        boolean hasPic = mCursor.getInt(mCursor.getColumnIndex(TalkContract.User.HAVE_PROFILE_IMAGE)) > 0;
        User user = new User( id, name, hasPic );
        holder.bind( user );
    }


    @Override
    public int getItemCount() {
        if( mCursor != null )
            return mCursor.getCount();
        else
            return 0;
    }

    public void swapCursor( Cursor cursor ) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nameTextView;
        private TextView emailTextView;
        private CircleImageView userProfileImage;
        private SwipeLayout swipeLayout;
        private ImageButton deleteButton;
        private boolean isSwiped = false;

        private User userInfo;
        public ViewHolder(final View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.textView_name);
            emailTextView = (TextView) itemView.findViewById(R.id.textView_email);
            userProfileImage = (CircleImageView) itemView.findViewById(R.id.user_profile_image_view);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_user_button);
            ConstraintLayout userInfoLayout = (ConstraintLayout) itemView.findViewById(R.id.user_info);
            userInfoLayout.setOnClickListener(this);
            swipeLayout =  (SwipeLayout)itemView.findViewById(R.id.my_swipe_layout);
            swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onClose(SwipeLayout layout) {
//                    Log.d(TAG, "onClose: ");
                    //when the SurfaceView totally cover the BottomView.
                    deleteButton.setEnabled(false);
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
                    deleteButton.setEnabled(true);
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

        }

        public void bind( final User user ) {
            isSwiped = false;
            this.userInfo = user;
            nameTextView.setText(user.getName());
            emailTextView.setText(user.getId());
            userProfileImage.setImageBitmap(user.getProfileImg(mContext));
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentResolver resolver = mContext.getContentResolver();
//                    Cursor cursor = resolver.query(TalkContract.ChatRooms.CONTENT_URI, null, TalkContract.ChatRooms.CHAT_ID + "=?", new String[]{ Utils.hashFunction(email) }, null);
//                    cursor.moveToFirst();
                    SharedPreferences tokenStorage = mContext.getSharedPreferences(mContext.getString(R.string.my_info), MODE_PRIVATE);
                    String myId = tokenStorage.getString( mContext.getString(R.string.user_id), null );
                    String chatTableName = Utils.ChatTableNameCreator(Arrays.asList(user.getId(), myId));
                    Log.d(TAG, "chatTableName: " + chatTableName);
                    int deletedRow1 = resolver.delete(TalkContract.ChatRooms.CONTENT_URI, TalkContract.ChatRooms.CHAT_ID + " = ?", new String[] {chatTableName});
                    int deletedRow2 = resolver.delete(TalkContract.Message.CONTENT_URI, TalkContract.ChatRooms.CHAT_ID + " = ? ", new String[] {chatTableName});
                    int deletedRow3 = resolver.delete(TalkContract.ChatRoomUsers.CONTENT_URI, TalkContract.ChatRooms.CHAT_ID + "=?", new String[] {chatTableName});
                    Log.d(TAG, "delete user result " + deletedRow1 + " : " + deletedRow2 + " : " + deletedRow3);
                    Cursor memberCheckCursor = resolver.query(
                            TalkContract.ChatRoomUsers.CONTENT_URI,
                            new String[] { "count(*) AS count"},
                            TalkContract.User.USER_ID + " = ? ",
                            new String[] { user.getId() },
                            null
                    );
                    if (memberCheckCursor != null) {
                        while ( memberCheckCursor.moveToNext() ) {
                            int count = memberCheckCursor.getInt(0);
                            if ( count == 0 ) {
                                if(userInfo.hasProfileImg() ) {
                                    Utils.deleteFile(mContext,
                                            mContext.getString(R.string.setting_friends_profile_img_name),
                                            mContext.getString(R.string.setting_profile_img_extension),
                                            Arrays.asList(mContext.getString(R.string.setting_friends_img_directory), user.getId()));
                                }
                                resolver.delete(
                                        TalkContract.User.CONTENT_URI,
                                        TalkContract.User.USER_ID + " = ? ",
                                        new String[] { user.getId() }
                                );
                            } else {
                                ContentValues values = new ContentValues();
                                values.put(TalkContract.User.IS_MY_FRIEND, 0);
                                    int deletedRow = resolver.update(
                                    TalkContract.User.CONTENT_URI,
                                    values,
                                    TalkContract.User.USER_ID + " = ?",
                                    new String[] { user.getId() }
                                );
                            }
                        }
                    }
//
//
                    Socket socket = ((MyApp) mContext.getApplicationContext()).getSocket();
                    socket.emit("delete_friend", user.getId());
                    memberCheckCursor.close();
                    isSwiped = false;
                }
            });
        }
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: ");
            if ( !isSwiped ) {
                mListener.onListItemClick(userInfo);
            } else {
                mListener.onSwipeOn();
            }
        }

    }
    public interface mOnClickListener {
        void onListItemClick(User user);
        void onSwipeOn();
    }
}
