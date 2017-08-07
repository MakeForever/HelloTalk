package com.beakya.hellotalk.utils;

/**
 * Created by goodlife on 2017. 7. 22..
 */

public class SocketEmitFunctions {

    public interface aFunction<T,R>       {
        public R apply ( T param );
    }
    public interface bFunction {
        public void apply ();
    }
}
