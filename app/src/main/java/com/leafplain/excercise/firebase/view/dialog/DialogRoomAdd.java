package com.leafplain.excercise.firebase.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.leafplain.excercise.firebase.demo_realtimedb.ActivityChatroom;
import com.leafplain.excercise.firebase.R;
import com.leafplain.excercise.firebase.datainfo.DataInfo;

/**
 * Created by kennethyeh on 2016/12/28.
 */

public class DialogRoomAdd {
    private static final String TAG = "DialogRoomAdd";
    private Context mContext;
    private Dialog mDialog ;
    private EditText roomnameET;
    private EditText descET;
    private static final String REQUIRED = "Required";

    private DatabaseReference mDatabase;

    public static DialogRoomAdd getInstance(Context context){
        return new DialogRoomAdd(context);
    }
    private DialogRoomAdd(Context mContext){
        this.mContext = mContext;


    }

    public Dialog getRoomAddDialog(){
        mDialog = new Dialog(mContext, R.style.customDialog); //指定自定義樣式
        mDialog.setContentView(R.layout.dialog_room_add);

        roomnameET = (EditText) mDialog.findViewById(R.id.roomnameET);
        descET      = (EditText) mDialog.findViewById(R.id.descET);


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
                    final String roomname = roomnameET.getText().toString().trim();
                    if (TextUtils.isEmpty(roomname)) {
                        roomnameET.setError(REQUIRED);
                        return;
                    }
                    final String desc = descET.getText().toString().trim();
                    if (TextUtils.isEmpty(desc)) {
                        descET.setError(REQUIRED);
                        return;
                    }
                    DataInfo.Chatroom chatroom = new DataInfo.Chatroom();
                    chatroom.desc = desc;
                    chatroom.name = roomname;

                    ((ActivityChatroom)mContext).addRoom(chatroom);
                    mDialog.dismiss();
                    break;
            }
        }
    };
}
