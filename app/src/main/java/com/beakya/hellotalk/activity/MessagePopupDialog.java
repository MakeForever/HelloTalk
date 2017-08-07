package com.beakya.hellotalk.activity;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beakya.hellotalk.R;

public class MessagePopupDialog extends Fragment implements LoaderManager.LoaderCallbacks<Boolean> {
    public static final String TAG = MessagePopupDialog.class.getSimpleName();
    private Context mContext;
    private TextView textView;

    private static class Singleton {
        private static final MessagePopupDialog instance = new MessagePopupDialog();
    }
    public static MessagePopupDialog getInstance () {
        return MessagePopupDialog.Singleton.instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
//        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//        layoutParams.format = PixelFormat.TRANSLUCENT;
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.gravity = Gravity.CENTER | Gravity.TOP;
//        getActivity().getWindow().setAttributes(layoutParams);
//        getDialog()(Window.FEATURE_NO_TITLE);
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);

//        Log.d(TAG, "onCreate:  layoutParams x : " + layoutParams.x + " y : " + layoutParams.y);
//        getWindow().setAttributes(layoutParams);
//        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.toast_message,  container, false);
        return rootView;

    }
    @Override
    public void onResume() {
        super.onResume();

    }

//    @Override
//    public void finish() {
//        super.finish();
//        overridePendingTransition(0, R.anim.anim_exit_to_top);
//    }



    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        return new TimerAsyncTaskLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
        if ( data ) {
            Log.d(TAG, "onLoadFinished: ");

        }
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    private static class TimerAsyncTaskLoader extends AsyncTaskLoader<Boolean> {

        TimerAsyncTaskLoader(Context context) {
            super(context);
        }
        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }
        @Override
        public Boolean loadInBackground() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "loadInBackground: c ");
            return true;
        }
    }
}
