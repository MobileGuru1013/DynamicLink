package com.leafplain.excercise.firebase.util.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.leafplain.excercise.firebase.datainfo.DataInfo;

/**
 * Created by kennethyeh on 2016/12/28.
 */

public class ChatUserUtil {
    private String TAG = "ChatUserUtil";

    private Context mContext = null;
    private SharedPreferences prefs                         = null;
    private static String PREFS_NAME 	                    = "ChatUsetInfo";
    private static final String PREFS_KEY_DeviceId          = "KeyDeviceId";
    private static final String PREFS_KEY_Nickname          = "KeyNickname";

    public static synchronized ChatUserUtil getInstance(Context context){
        return new ChatUserUtil(context);
    }

    private ChatUserUtil(Context context){
        mContext    = context;
        prefs       = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public synchronized void setChatUser(DataInfo.ChatUser chatUser){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_KEY_DeviceId, chatUser.deviceId);
        editor.putString(PREFS_KEY_Nickname, chatUser.nickname);
        editor.commit();
    }

    public synchronized DataInfo.ChatUser getChatUser(){
        DataInfo.ChatUser chatUser = new DataInfo.ChatUser();
        chatUser.deviceId = prefs.getString(PREFS_KEY_DeviceId, "");
        chatUser.nickname = prefs.getString(PREFS_KEY_Nickname, "");
        return chatUser;
    }
}
