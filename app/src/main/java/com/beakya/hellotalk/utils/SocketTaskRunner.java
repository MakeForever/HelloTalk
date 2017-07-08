package com.beakya.hellotalk.utils;

import android.content.Context;

import com.beakya.hellotalk.activity.PersonalChatActivity;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.PersonalChatReadEventInfo;
import com.beakya.hellotalk.objs.SocketJob;
import com.beakya.hellotalk.objs.User;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by goodlife on 2017. 7. 8..
 */

public class SocketTaskRunner {
    private SocketJobs jobsQueue;
    private Thread worker = null;
    private static boolean state = false;
    public SocketTaskRunner() {
        jobsQueue = new SocketJobs();
    }

    private static class Singleton {
        private static final SocketTaskRunner instance = new SocketTaskRunner();
    }
    public static SocketTaskRunner getInstance () {
        System.out.println("create instance");
        return SocketTaskRunner.Singleton.instance;
    }
    public void run ( final Context context ) {
        worker = new Thread( new Runnable() {
            @Override
            public void run() {
                while ( jobsQueue.hasNext() ) {
                    SocketJob job = jobsQueue.next();
                    if (job.getPayLoad().getData() instanceof PersonalChatReadEventInfo) {
                        handleActionReadMessage( (PersonalChatReadEventInfo) job.getPayLoad().getData(), context );
                    }
                    jobsQueue.remove();
                }
            }
        } );
        worker.run();
    }

    private void handleActionReadMessage ( PersonalChatReadEventInfo info, Context context ) {
        ArrayList<String> notReadMessageList = MsgUtils.getNotReadMessages(context, info.getChatId());
        if ( notReadMessageList.size() > 0 ) {
            if ( info.getChatType() == ChatRoom.PERSONAL_CHAT_TYPE ) {
                MsgUtils.readAllMessage(context, info.getChatType(), info.getChatId(), info.getReceiver(), notReadMessageList);
            } else if ( info.getChatType() == ChatRoom.GROUP_CHAT_TYPE ) {
                MsgUtils.readAllMessage(context, info.getChatType(), info.getChatId(), notReadMessageList);
            }
        }
        EventBus.getDefault().post(new Events.MessageEvent(PersonalChatActivity.EVENT_SOMEONE_READ_MESSAGE, null));
    }

    public void addJob ( SocketJob job ) {
        jobsQueue.addJob(job);
    }
    public SocketConnectionListener getListener () {
        return new SocketConnectionListener() {
            @Override
            public void onConnection( Context context ) {
                run( context );
            }
        };
    }
}
