package com.beakya.hellotalk.adapter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;

import java.util.Arrays;


/**
 * Created by cheolho on 2017. 4. 10..
 */

public class FriendAddAdapter extends RecyclerView.Adapter<FriendAddAdapter.ViewHolder> {
    public static final String TAG = FriendAddAdapter.class.getSimpleName();


    Context context;
    User[] users;
    public FriendAddAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from( context );
        View view = inflater.inflate(R.layout.search_user_detail_item, parent, false);
        FriendAddAdapter.ViewHolder viewHolder = new FriendAddAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = users[position];
        String name = user.getName();
        String email = user.getId();
        boolean isAdded = user.isAdded();
        Bitmap profile = user.getProfileImage();
        holder.bind( name, email, isAdded, profile);


    }

    @Override
    public int getItemCount() {
        if(users == null)
            return 0;
        else return users.length;
    }

    public void swapData(User[] data) {
        this.users = data;
        notifyDataSetChanged();
    }

    class ViewHolder extends  RecyclerView.ViewHolder {

        private TextView nameTextView;
        private TextView emailTextView;
        private ImageView userProfileImage;
        private Button addButton;
        private boolean mBoolean = false;
        private Bitmap mBitmap = null;
        private Context mContext;
        public ViewHolder(final View v) {
            super(v);
            mContext = v.getContext();
            nameTextView = (TextView) v.findViewById(R.id.textView_name);
            emailTextView = (TextView) v.findViewById(R.id.textView_email);
            userProfileImage = (ImageView) v.findViewById(R.id.user_profile_image_view);
            addButton = (Button) v.findViewById(R.id.friend_add_button);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentResolver resolver = v.getContext().getContentResolver();
                    String id = emailTextView.getText().toString();
                    String name = nameTextView.getText().toString();
                    if( !mBoolean ) {
                        ContentValues values = new ContentValues();
                        values.put(TalkContract.User.USER_ID, id);
                        values.put(TalkContract.User.USER_NAME, name);
                        values.put(TalkContract.User.IS_MY_FRIEND, 1);
                        if ( mBitmap != null) {
                            values.put(TalkContract.User.HAVE_PROFILE_IMAGE, 1);
                        }
                        resolver.insert(TalkContract.User.CONTENT_URI, values);
                        if( mBitmap != null) {
                            Utils.saveToInternalStorage(mContext, mBitmap,
                                    mContext.getString(R.string.setting_friends_profile_img_name),
                                    mContext.getString(R.string.setting_profile_img_extension),
                                    Arrays.asList( new String[]{ mContext.getString(R.string.setting_friends_img_directory), id }));
                        }
                        addButton.setText("X");
                        addButton.setVisibility(View.INVISIBLE);
                        addButton.setEnabled(false);
                        mBoolean = true;
                    }
                }
            });

        }


        public void bind( String name, String email, boolean isAdded, Bitmap profileImg ) {
            nameTextView.setText(name);
            emailTextView.setText(email);
            mBoolean = isAdded;
            mBitmap = profileImg;
            if( mBitmap != null ) {
                userProfileImage.setImageBitmap(mBitmap);
            } else {
                userProfileImage.setImageResource(R.mipmap.default_profile_img);
            }
            if( isAdded ) {
//                changeButtonText("추가");
                addButton.setText("X");
                addButton.setVisibility(View.INVISIBLE);
                addButton.setEnabled(false);
            } else {
                addButton.setText("추가");
                addButton.setVisibility(View.VISIBLE);
                addButton.setEnabled(true);
            }
            Log.d(TAG, "bind: " + isAdded);
        }
        private void changeButtonText(String value) {
            addButton.setText(value);
        }
    }
}
