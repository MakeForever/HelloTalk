package com.beakya.hellotalk.utils;

import android.util.Log;

/**
 * Created by goodlife on 2017. 8. 10..
 */


public final class Logger {
    public static void d( String TAG, String message ) {
        String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        Log.d(TAG, lineOut() + " // " + message);
    }
    public static String lineOut() {
        int level = 4;
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        StackTraceElement element = traces[level];
//        return (" at "  + traces[level].toString()+ "");
        return (element.getFileName() != null && element.getLineNumber() >= 0 ?
                 "hellotalk / " + element.getMethodName()+" (" + element.getFileName()  + ":" + element.getLineNumber() + ")" :
                    (element.getFileName()  != null ?  "("+element.getLineNumber()+")" : "(Unknown Source)"));
    }


}