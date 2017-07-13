package com.beakya.hellotalk.utils;

import android.content.Context;

import com.beakya.hellotalk.activity.PersonalChatActivity;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.Job;
import com.beakya.hellotalk.objs.PersonalChatReadEventInfo;
import com.beakya.hellotalk.objs.SocketJob;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by goodlife on 2017. 7. 8..
 */

public class TaskRunner {
    private SocketJobs jobsQueue;
    private static boolean state = false;
    private static boolean socketState = false;
    private ExecutorService service;
    public TaskRunner() {
        jobsQueue = new SocketJobs();
        service = Executors.newSingleThreadExecutor();
    }

    private static class Singleton {
        private static final TaskRunner instance = new TaskRunner();
    }
    public static TaskRunner getInstance () {
        return TaskRunner.Singleton.instance;
    }
    public void execute () {

        if ( socketState == true && state == false ) {
            state = true;
            while ( jobsQueue.hasNext() ) {
                service.execute(jobsQueue.next());
            }
            state = false;
        }
    }


    public void addJob ( Job job ) {
        jobsQueue.addJob(job);
        execute();
    }

    public static void setSocketState(boolean socketState) {
        TaskRunner.socketState = socketState;
    }
}
