package com.beakya.hellotalk.asynctaskloader;

import android.support.v4.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.Message;

import java.util.ArrayList;

/**
 * Created by goodlife on 2017. 5. 19..
 */
//public static class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
//    final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
//    final PackageManager mPm;
//
//    List<AppEntry> mApps;
//    PackageIntentReceiver mPackageObserver;
//
//    public AppListLoader(Context context) {
//        super(context);
//
//        // Retrieve the package manager for later use; note we don't
//        // use 'context' directly but instead the save global application
//        // context returned by getContext().
//        mPm = getContext().getPackageManager();
//    }
//
//    /**
//     * This is where the bulk of our work is done.  This function is
//     * called in a background thread and should generate a new set of
//     * data to be published by the loader.
//     */
//    @Override public List<AppEntry> loadInBackground() {
//        // Retrieve all known applications.
//        List<ApplicationInfo> apps = mPm.getInstalledApplications(
//                PackageManager.GET_UNINSTALLED_PACKAGES |
//                        PackageManager.GET_DISABLED_COMPONENTS);
//        if (apps == null) {
//            apps = new ArrayList<ApplicationInfo>();
//        }
//
//        final Context context = getContext();
//
//        // Create corresponding array of entries and load their labels.
//        List<AppEntry> entries = new ArrayList<AppEntry>(apps.size());
//        for (int i=0; i<apps.size(); i++) {
//            AppEntry entry = new AppEntry(this, apps.get(i));
//            entry.loadLabel(context);
//            entries.add(entry);
//        }
//
//        // Sort the list.
//        Collections.sort(entries, ALPHA_COMPARATOR);
//
//        // Done!
//        return entries;
//    }
//
//    /**
//     * Called when there is new data to deliver to the client.  The
//     * super class will take care of delivering it; the implementation
//     * here just adds a little more logic.
//     */
//    @Override public void deliverResult(List<AppEntry> apps) {
//        if (isReset()) {
//            // An async query came in while the loader is stopped.  We
//            // don't need the result.
//            if (apps != null) {
//                onReleaseResources(apps);
//            }
//        }
//        List<AppEntry> oldApps = mApps;
//        mApps = apps;
//
//        if (isStarted()) {
//            // If the Loader is currently started, we can immediately
//            // deliver its results.
//            super.deliverResult(apps);
//        }
//
//        // At this point we can release the resources associated with
//        // 'oldApps' if needed; now that the new result is delivered we
//        // know that it is no longer in use.
//        if (oldApps != null) {
//            onReleaseResources(oldApps);
//        }
//    }
//
//    /**
//     * Handles a request to start the Loader.
//     */
//    @Override protected void onStartLoading() {
//        if (mApps != null) {
//            // If we currently have a result available, deliver it
//            // immediately.
//            deliverResult(mApps);
//        }
//
//        // Start watching for changes in the app data.
//        if (mPackageObserver == null) {
//            mPackageObserver = new PackageIntentReceiver(this);
//        }
//
//        // Has something interesting in the configuration changed since we
//        // last built the app list?
//        boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());
//
//        if (takeContentChanged() || mApps == null || configChange) {
//            // If the data has changed since the last time it was loaded
//            // or is not currently available, start a load.
//            forceLoad();
//        }
//    }
//
//    /**
//     * Handles a request to stop the Loader.
//     */
//    @Override protected void onStopLoading() {
//        // Attempt to cancel the current load task if possible.
//        cancelLoad();
//    }
//
//    /**
//     * Handles a request to cancel a load.
//     */
//    @Override public void onCanceled(List<AppEntry> apps) {
//        super.onCanceled(apps);
//
//        // At this point we can release the resources associated with 'apps'
//        // if needed.
//        onReleaseResources(apps);
//    }
//
//    /**
//     * Handles a request to completely reset the Loader.
//     */
//    @Override protected void onReset() {
//        super.onReset();
//
//        // Ensure the loader is stopped
//        onStopLoading();
//
//        // At this point we can release the resources associated with 'apps'
//        // if needed.
//        if (mApps != null) {
//            onReleaseResources(mApps);
//            mApps = null;
//        }
//
//        // Stop monitoring for changes.
//        if (mPackageObserver != null) {
//            getContext().unregisterReceiver(mPackageObserver);
//            mPackageObserver = null;
//        }
//    }
//
//    /**
//     * Helper function to take care of releasing resources associated
//     * with an actively loaded data set.
//     */
//    protected void onReleaseResources(List<AppEntry> apps) {
//        // For a simple List<> there is nothing to do.  For something
//        // like a Cursor, we would close it here.
//    }
//}
public class ChatAsyncTaskLoader extends AsyncTaskLoader<ArrayList<Message>> {
    public static final String TAG = ChatAsyncTaskLoader.class.getSimpleName();
    ArrayList<Message> messagesList;
    String chatId;
    Context mContext;
    public ChatAsyncTaskLoader(Context context, String chatId) {
        super(context);
        this.chatId = chatId;
        mContext = getContext();
    }


    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        if( messagesList != null ) {
            onReleaseResources(messagesList);
            messagesList = null;
        }
    }
    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<Message> data) {
        super.onCanceled(data);
        onReleaseResources(messagesList);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if( chatId == null ) {
            onStopLoading();
        }

        if ( messagesList != null ) {
            deliverResult(messagesList);
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<Message> loadInBackground() {
        messagesList = new ArrayList<>();
        ContentResolver resolver = mContext.getContentResolver();
        String[] queryItems = new String [] {
                TalkContract.Message.CREATOR_ID,
                TalkContract.Message.MESSAGE_ID,
                TalkContract.Message.MESSAGE_CONTENT,
                TalkContract.Message.MESSAGE_TYPE,
                TalkContract.Message.CREATED_TIME,
                TalkContract.Message.IS_SEND,
                TalkContract.Message.READING_COUNT
        };
        Cursor cursor = resolver.query(
                TalkContract.Message.CONTENT_URI,
                queryItems,
                TalkContract.ChatRooms.CHAT_ID + " = ?",
                new String[] { chatId },
                null);
//        String query = " SELECT " + " u."+ TalkContract.User.USER_NAME + " m.?, m.?, m.?, m.?, m.?, m.?, m.? from " + TalkContract.Message.TABLE_NAME + " INNER JOIN " + TalkContract.User.TABLE_NAME +
//                " as u  ON " + "u."+ TalkContract.User.USER_ID + " = " + TalkContract.Message.CREATOR_ID + " WHERE " + TalkContract.ChatRooms.CHAT_ID + " = ? ";
//        Log.d(TAG, "loadInBackground: " + qu);
//        Cursor cursor = db.rawQuery(query, queryItems);
        while ( cursor.moveToNext() ) {

            String creatorId = cursor.getString(cursor.getColumnIndex(TalkContract.Message.CREATOR_ID));
            String messageId = cursor.getString(cursor.getColumnIndex(TalkContract.Message.MESSAGE_ID));
            String messageContent = cursor.getString(cursor.getColumnIndex(TalkContract.Message.MESSAGE_CONTENT));
            int messageType = cursor.getInt(cursor.getColumnIndex(TalkContract.Message.MESSAGE_TYPE));
            String createTime = cursor.getString(cursor.getColumnIndex(TalkContract.Message.CREATED_TIME));
            boolean isSend = cursor.getInt(cursor.getColumnIndex(TalkContract.Message.IS_SEND)) > 0;
            int readCount = cursor.getInt(cursor.getColumnIndex(TalkContract.Message.READING_COUNT));

            Message stringMessage = new Message(
                    messageId,
                    creatorId,
                    messageContent,
                    chatId,
                    messageType,
                    createTime,
                    isSend,
                    readCount);

            messagesList.add(stringMessage);
        }
        return messagesList;
    }

    @Override
    public void deliverResult(ArrayList<Message> data) {
        if (isReset()) {
            if (data != null) {
                onReleaseResources(data);
            }
        }
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    protected void onReleaseResources(ArrayList<Message> messagesList) {
//        // For a simple List<> there is nothing to do.  For something
//        // like a Cursor, we would close it here.
    }
}
