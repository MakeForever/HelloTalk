package com.beakya.hellotalk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.viewholder.BaseViewHolder;
import com.beakya.hellotalk.viewholder.ChatMemberViewHolder;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by goodlife on 2017. 6. 15..
 */

public class MemberListAdapter extends RecyclerView.Adapter<ChatMemberViewHolder> {
    List<User> storage;

    @Override
    public ChatMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ChatMemberViewHolder(inflater.inflate(R.layout.small_user_info, parent, false));

    }


    @Override
    public void onBindViewHolder(ChatMemberViewHolder holder, int position) {
        User user = storage.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemViewType(int position) {
        return storage.get(position).isMember() ? 1 : 0 ;
    }

    @Override
    public int getItemCount() {
        if ( storage != null )
            return storage.size();
        else
            return 0;
    }

    public void swapData( List<User> newData ) {
        this.storage = newData;
        notifyDataSetChanged();
    }
    public void addMember( List<User> users ) {
        int startPoint = storage.size();
        storage.addAll(users);
        notifyItemRangeInserted(startPoint, users.size());
    }
    public void deleteMember ( String userId ) {
        for ( int i = 0 ; storage.size() > i; i++ ) {
            User user = storage.get(i);
            if ( userId.equals(user.getId()) ) {
                storage.remove(i);
                notifyItemRemoved(i);
            }
        }
    }
}
