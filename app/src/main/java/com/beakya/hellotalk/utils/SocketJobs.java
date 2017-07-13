package com.beakya.hellotalk.utils;

import com.beakya.hellotalk.objs.Job;
import com.beakya.hellotalk.objs.Jobs;
import com.beakya.hellotalk.objs.SocketJob;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by goodlife on 2017. 7. 8..
 */

public class SocketJobs extends Jobs {
    private LinkedList<Job> jobList;
    public SocketJobs() {
        jobList = new LinkedList<>();
    }

    public LinkedList<Job> getJobList() {
        return jobList;
    }
    public void addJob ( Job job ) {
        jobList.addLast(job);
    }
    public Job getFirst() {
        if ( jobList.size() > 0 )
            return jobList.getFirst();
        else
            return null;
    }
    public boolean hasNext () {
        return jobList.size() > 0 ? true : false;
    }
    public Job next() {
        return jobList.poll();
    }
    public void remove () {
        if ( jobList.size() > 0 ) {
            jobList.remove();
        }
    }

}
