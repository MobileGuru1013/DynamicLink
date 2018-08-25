package com.leafplain.excercise.firebase.demo_realtimedb;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leafplain.excercise.firebase.R;
import com.leafplain.excercise.firebase.datainfo.DataInfo;
import com.leafplain.excercise.firebase.util.ConnectUtil;
import com.leafplain.excercise.firebase.util.DebugLog;
import com.leafplain.excercise.firebase.util.prefs.ChatUserUtil;
import com.leafplain.excercise.firebase.view.dialog.DialogRoomAdd;
import com.leafplain.excercise.firebase.view.dialog.DialogUserEdit;

import java.util.ArrayList;
import java.util.List;

public class ActivityChatroom extends AppCompatActivity {

    private static String TAG = "ActivityChatroom";
    private Context mContext;
    private DatabaseReference mDatabase;
    private ProgressBar mProgressBar ;

    private List<DataInfo.Chatroom> roomList;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    protected LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("welcom chat");
        toolbar.setTitle("Welcom Room");

        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        mDatabase = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://leafplainexercise.firebaseio.com/chattest/room");
        mDatabase.addChildEventListener(mChildEventListener);
        mDatabase.addListenerForSingleValueEvent(mValueEventListener);// 只執行一次

        if(!ConnectUtil.isNetworkAvailable(mContext)){
            Snackbar.make(mProgressBar, "連線異常！", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void addRoom(DataInfo.Chatroom chatroom){
        DebugLog.d(TAG, "addRoom");
        String key = mDatabase.push().getKey();
        chatroom.id = key;
        DebugLog.d(TAG, "id:"+chatroom.id);
        DebugLog.d(TAG, "name:"+chatroom.name);
        DebugLog.d(TAG, "desc:"+chatroom.desc);
        mDatabase.child(key).setValue(chatroom);
    }

    private ValueEventListener mValueEventListener = new ValueEventListener(){

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            DebugLog.d(TAG,"ValueEvent onDataChange");
            int i = 0;
            for (Object obj : dataSnapshot.getChildren()){ i++ ;}
            DebugLog.d(TAG,"loop size:"+i);
            roomList = new ArrayList<>();
            for (DataSnapshot roomSnapshot: dataSnapshot.getChildren()) {
                display(roomSnapshot);
                DataInfo.Chatroom chatroom = roomSnapshot.getValue(DataInfo.Chatroom.class);
                roomList.add(chatroom);
            }
            setAdapter(roomList);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            DebugLog.d(TAG,"ValueEvent onCancelled");
        }
    };

    private ChildEventListener mChildEventListener = new ChildEventListener(){

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            DebugLog.d(TAG,"ChildEvent onChildAdded");
            display(dataSnapshot);
            if(mRecyclerViewAdapter!=null){
                DataInfo.Chatroom chatroom = dataSnapshot.getValue(DataInfo.Chatroom.class);
                if(chatroom==null){
                    DebugLog.d(TAG,"chatroom==null");
                    return;
                }
                mRecyclerViewAdapter.addRoom(chatroom);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            DebugLog.d(TAG,"ChildEvent onChildChanged");
            display(dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            DebugLog.d(TAG,"ChildEvent onChildMoved");
            display(dataSnapshot);
            if(mRecyclerViewAdapter!=null){
                DataInfo.Chatroom chatroom = dataSnapshot.getValue(DataInfo.Chatroom.class);
                if(chatroom==null){
                    DebugLog.d(TAG,"chatroom==null");
                    return;
                }
                mRecyclerViewAdapter.removeRoom(chatroom);
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            DebugLog.d(TAG,"ChildEvent onChildMoved");
            display(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            DebugLog.d(TAG,"onCancelled");
        }
    };

    private void display(DataSnapshot dataSnapshot){
        DebugLog.d(TAG,"dataSnapshot==null:"+(dataSnapshot==null));
        DataInfo.Chatroom chatroom = dataSnapshot.getValue(DataInfo.Chatroom.class);
        if(chatroom==null){
            DebugLog.d(TAG,"chatroom==null");
            return;
        }
        DebugLog.d(TAG, "id = " + chatroom.id + " , name = " + chatroom.name+ " , desc = " + chatroom.desc);
        String key = dataSnapshot.getKey();
        DebugLog.i(TAG, "getKey:"+key);
        DebugLog.i(TAG, "---------------");
    }


    private void setAdapter(final List<DataInfo.Chatroom> roomList){
        DebugLog.d(TAG, "setAdapter");
        mRecyclerView       = (RecyclerView)findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerViewAdapter = new RecyclerViewAdapter(roomList);
                mRecyclerView.setAdapter(mRecyclerViewAdapter);
                mProgressBar.setVisibility(View.GONE);
                if(roomList.size()==0){
                    Snackbar.make(mRecyclerView, "尚無聊天室", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        },500);

    }
    private final int type_room = 1;
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<DataInfo.Chatroom> roomList;

        public RecyclerViewAdapter(List<DataInfo.Chatroom> roomList){
            this.roomList   = roomList;
        }

        @Override
        public int getItemCount() {
            return roomList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return type_room;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder = null;
            LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
            View vHolderEvent = mInflater.inflate(R.layout.list_item_room, parent, false);
            viewHolder = new HolderListRoom(vHolderEvent);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            HolderListRoom vhHolderListRoom  = (HolderListRoom) viewHolder;
            DataInfo.Chatroom mRecordEvent = roomList.get(position);

            RelativeLayout roomRL = vhHolderListRoom.roomRL;
            vhHolderListRoom.roomnameTV.setText(mRecordEvent.name.trim());
            vhHolderListRoom.descTV.setText(mRecordEvent.desc.trim());

            roomRL.setTag(mRecordEvent);
            roomRL.setOnClickListener(enterRoomListener);
        }

        public void addRoom(DataInfo.Chatroom chatroom){
            roomList.add(chatroom);
            notifyDataSetChanged();
        }

        public void removeRoom(DataInfo.Chatroom chatroom){
           for(int i=0 ; i<roomList.size() ; i++){
               DataInfo.Chatroom room = roomList.get(i);
               if(room.id == chatroom.id){
                   roomList.remove(i);
               }
           }
            notifyDataSetChanged();
        }

        public void destroy(){
        }
    }

    private View.OnClickListener enterRoomListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {

            ChatUserUtil mChatUserUtil = ChatUserUtil.getInstance(mContext);
            DataInfo.ChatUser mChatUser= mChatUserUtil.getChatUser();
            if(mChatUser.nickname.trim().equalsIgnoreCase("")){
                Dialog userEditDialog =  DialogUserEdit.getInstance(mContext).getUserEditDialog();
                userEditDialog.show();
                return;
            }
            DataInfo.Chatroom mRecordEvent = (DataInfo.Chatroom) view.getTag();


            Intent intentChatroom = new Intent();
            intentChatroom.setClass(mContext, ActivityChatPost.class);
            intentChatroom.putExtra(ActivityChatPost.Para_key_Chatroom, mRecordEvent);
            mContext.startActivity(intentChatroom);
        }
    };


    public class HolderListRoom extends RecyclerView.ViewHolder{
        public RelativeLayout roomRL;
        public TextView roomnameTV;
        public TextView descTV;

        public HolderListRoom(View itemView) {
            super(itemView);
            roomRL  = (RelativeLayout) itemView.findViewById(R.id.roomRL);
            roomnameTV  = (TextView) itemView.findViewById(R.id.roomnameTV);
            descTV  = (TextView) itemView.findViewById(R.id.descTV);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chatroom, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_chatroom) {
            DebugLog.d(TAG, "action_add_chatroom");
            Dialog roomAddDialog =  DialogRoomAdd.getInstance(mContext).getRoomAddDialog();
            roomAddDialog.show();
            return true;
        }
        if (id == R.id.action_edit_user) {
            DebugLog.d(TAG, "action_edit_user");
            Dialog userEditDialog =  DialogUserEdit.getInstance(mContext).getUserEditDialog();
            userEditDialog.show();
            return true;
        }
        if (id == android.R.id.home) {
            DebugLog.d(TAG, "menu");
            ((Activity)mContext).finish();
            return true;
        }
        return super.onOptionsItemSelected(item); // important line
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ((Activity)mContext).finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugLog.i(TAG, "onDestroy");
        mDatabase.removeEventListener(mChildEventListener);
    }
}
