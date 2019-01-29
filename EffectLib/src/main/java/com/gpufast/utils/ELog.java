package com.gpufast.utils;

import android.util.Log;

public class ELog {

    private static final String ELOG_TAG = "effectLib_:";

    private ELog(){

    }

    public static void i(String tag,String msg){
        Log.i(tag, ELOG_TAG+msg);
    }

    public static void d(String tag,String msg){
        Log.d(tag, ELOG_TAG+msg);
    }

    public static void w(String tag,String msg){
        Log.w(tag, ELOG_TAG+msg);
    }

    public static void e(String tag,String msg){
        Log.e(tag, ELOG_TAG+msg);
    }

}
