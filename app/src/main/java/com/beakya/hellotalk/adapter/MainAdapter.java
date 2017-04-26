package com.beakya.hellotalk.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.utils.Utils;

import java.util.Arrays;

/**
 * Created by cheolho on 2017. 3. 24..
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    public MainAdapter(Context context ) {
        mContext = context;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from( mContext );
        View view = inflater.inflate(R.layout.user_detail_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String name = mCursor.getString(mCursor.getColumnIndex(TalkContract.Friend.USER_NAME));
        String email = mCursor.getString(mCursor.getColumnIndex(TalkContract.Friend.USER_ID));
        Bitmap bitmapImg = Utils.getImageBitmap(mContext,
                mContext.getString(R.string.setting_friends_profile_img_name),
                mContext.getString(R.string.setting_profile_img_extension),
                Arrays.asList( new String[] { mContext.getString(R.string.setting_friends_img_directory), email }));
        holder.bind( name, email, bitmapImg );
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView emailTextView;
        private ImageView userProfileImage;
        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.textView_name);
            emailTextView = (TextView) itemView.findViewById(R.id.textView_email);
            userProfileImage = (ImageView) itemView.findViewById(R.id.user_profile_image_view);
        }

        public void bind( String name, String email, Bitmap image ) {
            nameTextView.setText(name);
            emailTextView.setText(email);
            if( image == null) {
                userProfileImage.setImageResource(R.mipmap.default_profile_img);
            } else {
                userProfileImage.setImageBitmap(image);
            }
        }

    }
}
