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
import com.beakya.hellotalk.utils.Utils;
import com.daimajia.swipe.SwipeLayout;

import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cheolho on 2017. 3. 24..
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    public static final String TAG = MainAdapter.class.getSimpleName();
    private Cursor mCursor;
    private Context mContext;
    private mOnClickListener mListener;
    public MainAdapter(Context context, mOnClickListener listener ) {
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
        String email = mCursor.getString(mCursor.getColumnIndex(TalkContract.User.USER_ID));
        boolean hasPic = mCursor.getInt(mCursor.getColumnIndex(TalkContract.User.USER_HAVE_PROFILE_IMAGE)) > 0;
        holder.bind( name, email, hasPic );
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
        private ImageView userProfileImage;
        private SwipeLayout swipeLayout;
        private ImageButton deleteButton;
        private boolean isSwiped = false;
        private boolean hasProfileImg = false;
        public ViewHolder(final View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.textView_name);
            emailTextView = (TextView) itemView.findViewById(R.id.textView_email);
            userProfileImage = (ImageView) itemView.findViewById(R.id.user_profile_image_view);
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

        public void bind(String name, final String email, final boolean image ) {
            isSwiped = false;
            nameTextView.setText(name);
            emailTextView.setText(email);
            hasProfileImg = image;
            if( hasProfileImg == false ) {
                userProfileImage.setImageResource(R.mipmap.default_profile_img);
            } else {

                Bitmap bitmapImg = Utils.getImageBitmap(mContext,
                        mContext.getString(R.string.setting_friends_profile_img_name),
                        mContext.getString(R.string.setting_profile_img_extension),
                        Arrays.asList( new String[] { mContext.getString(R.string.setting_friends_img_directory), email }));

                userProfileImage.setImageBitmap(bitmapImg);


            }
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentResolver resolver = mContext.getContentResolver();
//                    Cursor cursor = resolver.query(TalkContract.ChatRoom.CONTENT_URI, null, TalkContract.ChatRoom.CHAT_LIST_ID + "=?", new String[]{ Utils.sha256(email) }, null);
//                    cursor.moveToFirst();
                    SharedPreferences tokenStorage = mContext.getSharedPreferences(mContext.getString(R.string.my_info), MODE_PRIVATE);
                    String myId = tokenStorage.getString( mContext.getString(R.string.user_id), null );
                    String chatTableName = Utils.ChatTableNameCreator(Arrays.asList(new String[] { email, myId }));
                    Log.d(TAG, "chatTableName: " + chatTableName);
                    if( chatTableName != null ) {
                        int deletedRow1 = resolver.delete(TalkContract.ChatRoom.CONTENT_URI, TalkContract.ChatRoom.CHAT_LIST_ID + " = ?", new String[] {chatTableName});
                        int deletedRow2 = resolver.delete(TalkContract.Chat.CONTENT_URI, TalkContract.ChatRoom.CHAT_LIST_ID + " = ? ", new String[] {chatTableName});
                        int deletedRow3 = resolver.delete(TalkContract.Chat_User_Rooms.CONTENT_URI, TalkContract.ChatRoom.CHAT_LIST_ID + "=?", new String[] {chatTableName});
                        Log.d(TAG, "delete user result " + deletedRow1 + " : " + deletedRow2 + " : " + deletedRow3);
                    }

                    Uri uri = TalkContract.User.CONTENT_URI.buildUpon().appendPath(email).build();
                    int deletedRow = resolver.delete(uri, TalkContract.User.USER_ID+ "=?",new String[]{ email });
                    if( hasProfileImg != false ) {
                        Utils.deleteFile(mContext,
                                mContext.getString(R.string.setting_friends_profile_img_name),
                                mContext.getString(R.string.setting_profile_img_extension),
                                Arrays.asList(new String[] { mContext.getString(R.string.setting_friends_img_directory), email }));
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
                mListener.onListItemClick( id );
            } else {
                Toast.makeText(mContext, "스와이프를 닫아주세요", Toast.LENGTH_SHORT).show();
            }
        }
        private void deleteFriend() {

        }
    }
    public interface mOnClickListener {
        void onListItemClick( String userId );
    }
}
