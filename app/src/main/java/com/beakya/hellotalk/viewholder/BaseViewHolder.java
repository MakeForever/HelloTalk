package com.beakya.hellotalk.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by goodlife on 2017. 6. 2..
 */

public abstract class BaseViewHolder<ITEM> extends RecyclerView.ViewHolder {
    public BaseViewHolder(View itemView) {
        super(itemView);
    }
    public abstract void bind( ITEM item );
}
