package com.leafplain.excercise.firebase.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.leafplain.excercise.firebase.R;
import com.leafplain.excercise.firebase.datainfo.DataInfo;
import com.leafplain.excercise.firebase.util.GetDeviceIDUtil;
import com.leafplain.excercise.firebase.util.prefs.ChatUserUtil;

/**
 * Created by kennethyeh on 2016/12/28.
 */

public class DialogUserEdit {
    private static final String TAG = "AddEventDialogControl";
    private Context mContext;
    private Dialog mDialog ;
    private EditText nicknameET;
    private static final String REQUIRED = "Required";

    private TextView deviceIdTV;
    private DatabaseReference mDatabase;

    public static DialogUserEdit getInstance(Context context){
        return new DialogUserEdit(context);
    }
    private DialogUserEdit(Context mContext){
        this.mContext = mContext;


    }

    public Dialog getUserEditDialog(){
        mDialog = new Dialog(mContext, R.style.customDialog); //指定自定義樣式
        mDialog.setContentView(R.layout.dialog_user);


        deviceIdTV = (TextView) mDialog.findViewById(R.id.deviceIdTV);
        deviceIdTV.setText(GetDeviceIDUtil.getDeviceID(mContext));

        nicknameET = (EditText) mDialog.findViewById(R.id.nicknameET);
        ChatUserUtil mChatUserUtil = ChatUserUtil.getInstance(mContext);
        DataInfo.ChatUser mChatUser= mChatUserUtil.getChatUser();
        nicknameET.setText(mChatUser.nickname);

        Button negativeButton   = (Button) mDialog.findViewById(R.id.negativeButton);
        Button positiveButton   = (Button) mDialog.findViewById(R.id.positiveButton);
        negativeButton.setOnClickListener(mCloseListener);
        positiveButton.setOnClickListener(mCloseListener);
        return mDialog;
    }

    private View.OnClickListener mCloseListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {

            int id = view.getId();
            switch (id){
                case R.id.negativeButton:
                    mDialog.dismiss();
                    break;
                case R.id.positiveButton:
                    final String nickname = nicknameET.getText().toString().trim();
                    if (TextUtils.isEmpty(nickname)) {
                        nicknameET.setError(REQUIRED);
                        return;
                    }
                    DataInfo.ChatUser chatUser = new DataInfo.ChatUser();
                    chatUser.deviceId = GetDeviceIDUtil.getDeviceID(mContext);
                    chatUser.nickname = nickname;
                    ChatUserUtil mChatUserUtil = ChatUserUtil.getInstance(mContext);
                    mChatUserUtil.setChatUser(chatUser);
                    mDialog.dismiss();
                    break;
            }
        }
    };
}
