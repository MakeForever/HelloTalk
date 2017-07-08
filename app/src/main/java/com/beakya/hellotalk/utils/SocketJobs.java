package com.beakya.hellotalk.utils;

import com.beakya.hellotalk.objs.Jobs;
import com.beakya.hellotalk.objs.SocketJob;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by goodlife on 2017. 7. 8..
 */

public class SocketJobs extends Jobs {
    private LinkedList<SocketJob> jobList;
    public SocketJobs() {
        jobList = new LinkedList<>();
    }

    public LinkedList<SocketJob> getJobList() {
        return jobList;
    }
    public void addJob ( SocketJob job ) {
        jobList.addLast(job);
    }
    public SocketJob getFirst() {
        if ( jobList.size() > 0 )
            return jobList.getFirst();
        else
            return null;
    }
    public boolean hasNext () {
        return jobList.size() > 0 ? true : false;
    }
    public SocketJob next() {
        if ( jobList.size() > 0 ) {
            return jobList.getFirst();
        } else
            return null;
    }
    public void remove () {
        if ( jobList.size() > 0 ) {
            jobList.remove();
        }
    }

}
