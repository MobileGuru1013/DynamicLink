package com.leafplain.excercise.firebase.util;

import android.content.Context;
import android.provider.Settings;

/**
 * Created by kennethyeh on 2016/12/28.
 */

public class GetDeviceIDUtil {
    public synchronized static String getDeviceID(Context context){
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
