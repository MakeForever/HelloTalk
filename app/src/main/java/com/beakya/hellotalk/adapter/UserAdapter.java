package com.beakya.hellotalk.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;
import com.daimajia.swipe.SwipeLayout;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

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
        private boolean hasProfileImg = false;
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
            nameTextView.setText(user.getName());
            emailTextView.setText(user.getId());
            hasProfileImg = user.hasProfileImg();
            userProfileImage.setImageBitmap(user.getProfileImg(mContext));
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentResolver resolver = mContext.getContentResolver();
//                    Cursor cursor = resolver.query(TalkContract.ChatRooms.CONTENT_URI, null, TalkContract.ChatRooms.CHAT_ID + "=?", new String[]{ Utils.hashFunction(email) }, null);
//                    cursor.moveToFirst();
                    SharedPreferences tokenStorage = mContext.getSharedPreferences(mContext.getString(R.string.my_info), MODE_PRIVATE);
                    String myId = tokenStorage.getString( mContext.getString(R.string.user_id), null );
                    String chatTableName = Utils.ChatTableNameCreator(Arrays.asList(new String[] { user.getId(), myId }));
                    Log.d(TAG, "chatTableName: " + chatTableName);
                    if( chatTableName != null ) {
                        int deletedRow1 = resolver.delete(TalkContract.ChatRooms.CONTENT_URI, TalkContract.ChatRooms.CHAT_ID + " = ?", new String[] {chatTableName});
                        int deletedRow2 = resolver.delete(TalkContract.Message.CONTENT_URI, TalkContract.ChatRooms.CHAT_ID + " = ? ", new String[] {chatTableName});
                        int deletedRow3 = resolver.delete(TalkContract.ChatUserRooms.CONTENT_URI, TalkContract.ChatRooms.CHAT_ID + "=?", new String[] {chatTableName});
                        Log.d(TAG, "delete user result " + deletedRow1 + " : " + deletedRow2 + " : " + deletedRow3);
                    }

                    Uri uri = TalkContract.User.CONTENT_URI.buildUpon().appendPath(user.getId()).build();
                    int deletedRow = resolver.delete(uri, TalkContract.User.USER_ID+ "=?",new String[]{ user.getId() });
                    if( hasProfileImg != false ) {
                        Utils.deleteFile(mContext,
                                mContext.getString(R.string.setting_friends_profile_img_name),
                                mContext.getString(R.string.setting_profile_img_extension),
                                Arrays.asList(new String[] { mContext.getString(R.string.setting_friends_img_directory), user.getId() }));
                    }
                    isSwiped = false;
                }
            });
        }
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: ");
            if ( !isSwiped ) {
                String id = emailTextView.getText().toString();
                String name = nameTextView.getText().toString();
                Bitmap bitmap = userProfileImage.getDrawingCache(true);
                User user = new User(id, name, bitmap);
                mListener.onListItemClick(user);
            } else {
                Toast.makeText(mContext, "스와이프를 닫아주세요", Toast.LENGTH_SHORT).show();
            }
        }
        private void deleteFriend() {

        }
    }
    public interface mOnClickListener {
        void onListItemClick(User user);
    }
}
