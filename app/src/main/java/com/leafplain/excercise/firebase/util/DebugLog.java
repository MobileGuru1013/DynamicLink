package com.leafplain.excercise.firebase.util;

import android.util.Log;

/**
 * Created by kennethyeh on 2016/12/27.
 */

public class DebugLog {
    private static boolean DEBUG_FLAG_DEBUG = true;
    private static boolean DEBUG_FLAG_INFO = true;
    private static boolean DEBUG_FLAG_WARN = true;

    public static void d(String tag, String debugStr){
        if(DEBUG_FLAG_DEBUG){
//			int maxLogSize = 1000;
//		    for(int i = 0; i <= debugStr.length() / maxLogSize; i++) {
//		        int start = i * maxLogSize;
//		        int end = (i+1) * maxLogSize;
//		        end = end > debugStr.length() ? debugStr.length() : end;
//		        Log.d(tag, debugStr.substring(start, end));
//		    }
            Log.d(tag, debugStr);
        }
    }

    public static void i(String tag, String debugStr){
        if(DEBUG_FLAG_INFO){
            Log.i(tag, debugStr);
        }
    }

    public static void w(String tag, String debugStr){
        if(DEBUG_FLAG_WARN){
            Log.w(tag, debugStr);
        }
    }

}
