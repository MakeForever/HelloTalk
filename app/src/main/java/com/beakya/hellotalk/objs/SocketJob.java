package com.beakya.hellotalk.objs;


import android.content.Context;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.activity.PersonalChatActivity;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.utils.MsgUtils;
import com.beakya.hellotalk.utils.SocketEmitFunctions;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import io.socket.client.Socket;

/**
 * Created by goodlife on 2017. 7. 8..
 */

public class SocketJob extends Job {
    private String action;
    private PayLoad payLoad;
    private Context context;
    public SocketJob(String action, PayLoad payLoad, Context context) {
        this.action = action;
        this.payLoad = payLoad;
        this.context = context;
    }

    public String getEvent() {
        return action;
    }

    public PayLoad getPayLoad() {
        return payLoad;
    }

    @Override
    public void run() {
        if ( payLoad.getData() instanceof SocketEmitFunctions.bFunction ) {
            ((SocketEmitFunctions.bFunction) payLoad.getData()).apply();
        }
    }
//    private static void handleActionReadMessage ( PersonalChatReadEventInfo info, Context context ) {
//        ArrayList<String> notReadMessageList = MsgUtils.getNotReadMessages(context, info.getChatId());
//        if ( notReadMessageList.size() > 0 ) {
//            if ( info.getChatType() == ChatRoom.PERSONAL_CHAT_TYPE ) {
//                MsgUtils.readAllMessage(context, info.getChatType(), info.getChatId(), info.getReceiver(), notReadMessageList);
//            } else if ( info.getChatType() == ChatRoom.GROUP_CHAT_TYPE ) {
//                MsgUtils.readAllMessage(context, info.getChatType(), info.getChatId(), notReadMessageList);
//            }
//        }
//        EventBus.getDefault().post(new Events.MessageEvent(PersonalChatActivity.EVENT_SOMEONE_READ_MESSAGE, null));
//    }
}
