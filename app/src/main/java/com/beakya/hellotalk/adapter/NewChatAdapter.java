package com.beakya.hellotalk.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.viewholder.NewChatViewHolder;

import java.util.HashMap;

/**
 * Created by goodlife on 2017. 6. 8..
 */

public class NewChatAdapter extends RecyclerView.Adapter<NewChatViewHolder> {
    Cursor cursor = null;
    HashMap<String, User> users;
    public NewChatAdapter() {
        users = new HashMap<>();
    }

    @Override
    public NewChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        NewChatViewHolder holder = NewChatViewHolder.newInstance(parent);
        holder.setAdapter(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(NewChatViewHolder holder, int position) {
        cursor.moveToPosition(position);
        String id = cursor.getString(cursor.getColumnIndex(TalkContract.User.USER_ID));
        String name = cursor.getString(cursor.getColumnIndex(TalkContract.User.USER_NAME));
        boolean hasPic = cursor.getInt(cursor.getColumnIndex(TalkContract.User.HAVE_PROFILE_IMAGE)) > 0;
        holder.bind(new User(id, name, hasPic));
    }
    public void addMember(User user) {
        users.put(user.getId(), user);
    }
    public void deleteMember(User user) {
        users.remove(user.getId());
    }
    public HashMap getUsers() {
        return users;
    }
    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }
    public void swapCursor (Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }
}
