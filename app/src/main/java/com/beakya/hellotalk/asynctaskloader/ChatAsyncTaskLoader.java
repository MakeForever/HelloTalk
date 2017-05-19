package com.beakya.hellotalk.asynctaskloader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.beakya.hellotalk.objs.ChatModel;

import java.util.List;

/**
 * Created by goodlife on 2017. 5. 19..
 */

public class ChatAsyncTaskLoader extends AsyncTaskLoader<List<ChatModel>> {

    public ChatAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public List<ChatModel> loadInBackground() {
        return null;
    }
}
