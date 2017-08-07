package com.beakya.hellotalk.objs;

/**
 * Created by goodlife on 2017. 7. 8..
 */

public class PayLoad<E> {
    private E data;
    public PayLoad( E data ) {
        this.data = data;
    }

    public E getData() {
        return data;
    }
}
