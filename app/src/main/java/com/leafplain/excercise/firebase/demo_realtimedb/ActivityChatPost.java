package com.leafplain.excercise.firebase.demo_realtimedb;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.leafplain.excercise.firebase.R;
import com.leafplain.excercise.firebase.datainfo.DataInfo;
import com.leafplain.excercise.firebase.util.ConnectUtil;
import com.leafplain.excercise.firebase.util.DebugLog;
import com.leafplain.excercise.firebase.util.GetDeviceIDUtil;
import com.leafplain.excercise.firebase.util.prefs.ChatUserUtil;
import com.leafplain.excercise.firebase.view.recyclerview.ViewTypeConstant;
import com.leafplain.excercise.firebase.view.recyclerview.ViewTypeInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.leafplain.excercise.firebase.R.id.recyclerView;
import static com.leafplain.excercise.firebase.util.GetDeviceIDUtil.getDeviceID;

public class ActivityChatPost extends AppCompatActivity {


    private static String TAG = "ActivityChatPost";
    private Context mContext;
    private DatabaseReference mDatabase;
    private DatabaseReference mRoomDatabase;
    private boolean roomDelete = false;
    private ProgressBar mProgressBar ;


    public static final String Para_key_Chatroom = "Chatroom" ;
    private DataInfo.Chatroom mChatroom;

    private EditText descET;
    private ImageButton enterBTN;

    private static final String REQUIRED = "Required";

    private List<DataInfo.ChatPost> postList;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    protected LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_post);
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        descET      = (EditText) findViewById(R.id.descET);
        enterBTN    = (ImageButton) findViewById(R.id.enterBTN);
        enterBTN.setOnClickListener(postlistener);


        String user = ChatUserUtil.getInstance(mContext).getChatUser().nickname;
        final String editHint = user+" say...";
        descET.setHint(editHint);

        mChatroom   = (DataInfo.Chatroom) getIntent().getSerializableExtra(Para_key_Chatroom);

        DebugLog.d(TAG, "mChatroom name:"+mChatroom.name);
        DebugLog.d(TAG, "mChatroom desc:"+mChatroom.desc);
        DebugLog.d(TAG, "mChatroom id:"+mChatroom.id);
        getSupportActionBar().setTitle(""+mChatroom.name);

        mDatabase = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://leafplainexercise.firebaseio.com/chattest/chatpost/"+mChatroom.id);
        mDatabase.addChildEventListener(mChildEventListener);
        Query queryRef = mDatabase.orderByKey().limitToLast(100);
        queryRef.addListenerForSingleValueEvent(mValueEventListener);// 只執行一次


        mRoomDatabase = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://leafplainexercise.firebaseio.com/chattest/room/"+mChatroom.id);
        mRoomDatabase.addChildEventListener(mRoomChildEventListener);

        if(!ConnectUtil.isNetworkAvailable(mContext)){
            Snackbar.make(descET, "連線異常！", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private ChildEventListener mRoomChildEventListener = new ChildEventListener(){

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            DebugLog.d(TAG,"Room ChildEvent onChildAdded");
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            DebugLog.d(TAG,"Room ChildEvent onChildChanged");
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            DebugLog.d(TAG,"Room ChildEvent onChildMoved");
            roomDelete =true;
            Snackbar.make(mRecyclerView, "聊天室已被清除！！", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            DebugLog.d(TAG,"Room ChildEvent onChildMoved");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            DebugLog.d(TAG,"onCancelled");
        }
    };


    private View.OnClickListener postlistener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            if(roomDelete) {
                Snackbar.make(mRecyclerView, "聊天室已被清除！！", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                return;
            }
            DataInfo.ChatPost post = new  DataInfo.ChatPost();
            final String desc = descET.getText().toString().trim();
            if (TextUtils.isEmpty(desc)) {
                descET.setError(REQUIRED);
                return;
            }
            post.content = desc;
            post.deviceId = getDeviceID(mContext);
            ChatUserUtil mChatUserUtil = ChatUserUtil.getInstance(mContext);
            DataInfo.ChatUser mChatUser= mChatUserUtil.getChatUser();
            post.nickname = mChatUser.nickname;
            post.timestamp = ServerValue.TIMESTAMP;
            addPost(post);
        }
    };

    public void addPost(DataInfo.ChatPost post){
        DebugLog.d(TAG, "addPost");
        String key = mDatabase.push().getKey();
        post.id = key;
        DebugLog.d(TAG, "id:"+post.id);
        DebugLog.d(TAG, "deviceId:"+ post.deviceId);
        DebugLog.d(TAG, "nickname:"+post.nickname);
        DebugLog.d(TAG, "content:"+post.content);
        mDatabase.child(key).setValue(post);
        descET.setText("");
        DebugLog.i(TAG, "addPost =====");
    }

    private ValueEventListener mValueEventListener = new ValueEventListener(){

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            DebugLog.d(TAG,"ValueEvent onDataChange");
            int i = 0;
            for (Object obj : dataSnapshot.getChildren()){ i++ ;}
            postList = new ArrayList<>();
            DebugLog.d(TAG,"loop size:"+i);
            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                display(postSnapshot);
                DataInfo.ChatPost mChatPost = postSnapshot.getValue(DataInfo.ChatPost.class);
                postList.add(mChatPost);
            }
            setAdapter(postList);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            DebugLog.d(TAG,"onCancelled");
        }
    };

    private ChildEventListener mChildEventListener = new ChildEventListener(){

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            DebugLog.d(TAG,"ChildEvent onChildAdded");
            display(dataSnapshot);
            if(mRecyclerViewAdapter!=null){
                DataInfo.ChatPost chatPost = dataSnapshot.getValue(DataInfo.ChatPost.class);
                if(chatPost==null){
                    DebugLog.d(TAG,"chatPost==null");
                    return;
                }
                ViewTypeInfo viewType = new ViewTypeInfo();
                String userDeviceId = GetDeviceIDUtil.getDeviceID(mContext);
                if(userDeviceId.equals(chatPost.deviceId)){
                    viewType.viewType     = ViewTypeConstant.ChatpostCellType.ITEM_LIST_USER;
                }else{
                    viewType.viewType     = ViewTypeConstant.ChatpostCellType.ITEM_LIST_OTHER;
                }
                viewType.dataObject   = chatPost;
                mRecyclerViewAdapter.addPost(viewType);
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
                DataInfo.ChatPost chatPost = dataSnapshot.getValue(DataInfo.ChatPost.class);
                if(chatPost==null){
                    DebugLog.d(TAG,"chatPost==null");
                    return;
                }
                mRecyclerViewAdapter.removeRoom(chatPost);
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
//        DebugLog.d(TAG,"dataSnapshot==null:"+(dataSnapshot==null));
//        DataInfo.ChatPost mChatPost = dataSnapshot.getValue(DataInfo.ChatPost.class);
//        if(mChatPost==null){
//            DebugLog.d(TAG,"mChatPost==null");
//            return;
//        }
//        DebugLog.d(TAG, "id:" + mChatPost.id);
//        DebugLog.d(TAG, "deviceId:" + mChatPost.deviceId);
//        DebugLog.d(TAG, "nickname:" + mChatPost.nickname);
//        DebugLog.d(TAG, "content:" + mChatPost.content);
//        String key = dataSnapshot.getKey();
//        DebugLog.i(TAG, "getKey:"+key);
//        DebugLog.i(TAG, "---------------");
    }


    private void setAdapter(List<DataInfo.ChatPost> postList){
        DebugLog.d(TAG, "setAdapter");
        mRecyclerView       = (RecyclerView)findViewById(recyclerView);
        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);
        final List<ViewTypeInfo> mViewTypeList = convert(postList);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerViewAdapter = new RecyclerViewAdapter(mViewTypeList);
                mRecyclerView.setAdapter(mRecyclerViewAdapter);
                mProgressBar.setVisibility(View.GONE);
                if(mViewTypeList.size()==0){
                    Snackbar.make(mRecyclerView, "尚無留言", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        },1000);
    }
    public List<ViewTypeInfo> convert(List<DataInfo.ChatPost> postList){
        List<ViewTypeInfo> mViewTypeList = new ArrayList<ViewTypeInfo>();
        for(int i = 0 ; i<postList.size() ; i++) {
            DataInfo.ChatPost chatPost = postList.get(i);
            String userDeviceId = GetDeviceIDUtil.getDeviceID(mContext);
            if(userDeviceId.equals(chatPost.deviceId)){
                ViewTypeInfo viewType = new ViewTypeInfo();
                viewType.viewType     = ViewTypeConstant.ChatpostCellType.ITEM_LIST_USER;
                viewType.dataObject   = chatPost;
                mViewTypeList.add(viewType);
            }else{
                ViewTypeInfo viewType = new ViewTypeInfo();
                viewType.viewType     = ViewTypeConstant.ChatpostCellType.ITEM_LIST_OTHER;
                viewType.dataObject   = chatPost;
                mViewTypeList.add(viewType);
            }
        }
        return mViewTypeList;
    }
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ViewTypeInfo> mViewTypeList;

        public RecyclerViewAdapter(List<ViewTypeInfo> mViewTypeList ){
            this.mViewTypeList   = mViewTypeList;
        }

        @Override
        public int getItemCount() {
            return mViewTypeList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mViewTypeList.get(position).viewType;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder = null;
            LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
            if(viewType==ViewTypeConstant.ChatpostCellType.ITEM_LIST_USER){
                View vPost = mInflater.inflate(R.layout.list_item_post_user, parent, false);
                viewHolder = new HolderPost(vPost);
            }
            if(viewType==ViewTypeConstant.ChatpostCellType.ITEM_LIST_OTHER){
                View vPost = mInflater.inflate(R.layout.list_item_post_other, parent, false);
                viewHolder = new HolderPost(vPost);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            ViewTypeInfo viewTypeInfo = mViewTypeList.get(position);
            HolderPost vhHolderPost = (HolderPost) viewHolder;
            DataInfo.ChatPost chatPost = (DataInfo.ChatPost) viewTypeInfo.dataObject;
            vhHolderPost.nicknameTV.setText(chatPost.nickname);
            vhHolderPost.contentTV.setText(chatPost.content);
            long timestamp = (Long)chatPost.timestamp;

            try{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = (new Date(timestamp));
                String time = sdf.format(date);
                vhHolderPost.timeTV.setText(time);
            }
            catch(Exception ex){}
        }

        public void addPost(ViewTypeInfo viewTypeInfo){
            mViewTypeList.add(viewTypeInfo);
            notifyDataSetChanged();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.scrollToPosition(mViewTypeList.size()-1);
                }
            },400);
        }

        public void removeRoom(DataInfo.ChatPost chatPost){
            for(int i=0 ; i<mViewTypeList.size() ; i++){
                ViewTypeInfo viewTypeInfo = mViewTypeList.get(i);
                DataInfo.ChatPost obj = (DataInfo.ChatPost) viewTypeInfo.dataObject;
                if(obj.id == chatPost.id){
                    mViewTypeList.remove(i);
                }
            }
            if(mViewTypeList.size()==0){
                Snackbar.make(mRecyclerView, "聊天資料已被清除！！", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            notifyDataSetChanged();
        }
    }


    public class HolderPost extends RecyclerView.ViewHolder{
        public TextView nicknameTV;
        public TextView contentTV;
        public TextView timeTV;

        public HolderPost(View itemView) {
            super(itemView);
            nicknameTV  = (TextView) itemView.findViewById(R.id.nicknameTV);
            contentTV   = (TextView) itemView.findViewById(R.id.contentTV);
            timeTV      = (TextView) itemView.findViewById(R.id.timeTV);
        }
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu., menu);
//        return true;
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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
        mRoomDatabase.removeEventListener(mRoomChildEventListener);
    }

}
