package com.beakya.hellotalk.objs;


/**
 * Created by goodlife on 2017. 7. 8..
 */

public class SocketJob extends Job {
    private String action;
    private PayLoad payLoad;

    public SocketJob(String action, PayLoad payLoad) {
        this.action = action;
        this.payLoad = payLoad;
    }

    public String getEvent() {
        return action;
    }

    public PayLoad getPayLoad() {
        return payLoad;
    }
}
