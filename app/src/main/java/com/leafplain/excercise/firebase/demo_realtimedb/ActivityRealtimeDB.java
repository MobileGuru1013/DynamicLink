package com.leafplain.excercise.firebase.demo_realtimedb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.leafplain.excercise.firebase.R;
import com.leafplain.excercise.firebase.datainfo.DataInfo;
import com.leafplain.excercise.firebase.util.DebugLog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kennethyeh on 2016/12/27.
 */

public class ActivityRealtimeDB  extends AppCompatActivity {

    private static final String TAG = "ActivityRealtimeDB";
    private DatabaseReference mDatabase;

    private Button addBTN;
    private Button updateBTN;
    private Button deleteBTN;
    private Button queryBTN;
    private Button query1BTN;
    private Button query2BTN;

    private EditText nameTV;
    private EditText noteTV;
    private EditText keyTV;
    private static final String REQUIRED = "Required";

    private static int countKey = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_db);

        nameTV = (EditText) findViewById(R.id.nameTV);
        noteTV = (EditText) findViewById(R.id.noteTV);
        keyTV = (EditText) findViewById(R.id.keyTV);

        addBTN = (Button) findViewById(R.id.addBTN);
        updateBTN = (Button) findViewById(R.id.updateBTN);
        deleteBTN = (Button) findViewById(R.id.deleteBTN);
        queryBTN = (Button) findViewById(R.id.queryBTN);
        query1BTN = (Button) findViewById(R.id.query1BTN);
        query2BTN = (Button) findViewById(R.id.query2BTN);



        addBTN.setOnClickListener(clickListener);
        updateBTN.setOnClickListener(clickListener);
        deleteBTN.setOnClickListener(clickListener);
        queryBTN.setOnClickListener(clickListener);
        query1BTN.setOnClickListener(clickListener);
        query2BTN.setOnClickListener(clickListener);

//
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabase.addChildEventListener(mChildEventListener);
    }


    private View.OnClickListener clickListener= new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case (R.id.addBTN):
                    setUser();
                    break;
                case (R.id.updateBTN):
                    updateUser();
                    break;
                case (R.id.deleteBTN):
                    deleteUser();
                    break;
                case (R.id.queryBTN):
                    query();
                    break;
                case (R.id.query1BTN):
                    query1();
                    break;
                case (R.id.query2BTN):
                    query2();
                    break;
            }
        }
    };

    private void setUser(){

        final String name = nameTV.getText().toString();
        final String note = noteTV.getText().toString();
        // Title is required
        if (TextUtils.isEmpty(name)) {
            nameTV.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(note)) {
            noteTV.setError(REQUIRED);
            return;
        }


        DebugLog.d(TAG,"setUser");
//        String key = (countKey++)+"";
//        String key = mDatabase.push().getKey();
        String key = name;
        keyTV.setText(key);
//        DebugLog.d(TAG,"key:"+key);
        DataInfo.User user = new DataInfo.User(name, note);
        mDatabase.child(key).setValue(user);
//        mDatabase.push().setValue(user);

    }

    private void updateUser(){
        DebugLog.d(TAG,"updateUser");
        final String key = keyTV.getText().toString();
        if (TextUtils.isEmpty(key)) {
            keyTV.setError(REQUIRED);
            return;
        }

        final String name = nameTV.getText().toString();
        final String note = noteTV.getText().toString();

        DatabaseReference userRef = mDatabase.child(key);
        Map<String, Object> nameMap = new HashMap <String, Object>();
        nameMap.put("username", name);
        nameMap.put("note", note);
        userRef.updateChildren(nameMap);

    }

    private void deleteUser(){
        DebugLog.d(TAG,"deleteUser");
        final String key = keyTV.getText().toString();
        if (TextUtils.isEmpty(key)) {
            keyTV.setError(REQUIRED);
            return;
        }

        DatabaseReference userRef = mDatabase.child(key);
        userRef.removeValue();
    }

    private void query(){
        DebugLog.d(TAG,"query");
        final String key = keyTV.getText().toString();
        if (TextUtils.isEmpty(key)) {
            keyTV.setError(REQUIRED);
            return;
        }
//        Query queryRef = mDatabase.orderByKey().equalTo(key);
        final Query queryRef = mDatabase.orderByChild("username").equalTo(key);
//        Query queryRef = mDatabase.orderByKey();
//        queryRef.addChildEventListener(mChildEventListener);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                queryRef.removeEventListener(mChildEventListener);
//            }
//        },1000);
        queryRef.addListenerForSingleValueEvent(mValueEventListener);
    }

    private void query1(){
        DebugLog.d(TAG,"query1");
        final Query queryRef = mDatabase.orderByKey().limitToLast(2);
//        queryRef.addChildEventListener(mChildEventListener);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                queryRef.removeEventListener(mChildEventListener);
//            }
//        },1000);
        queryRef.addListenerForSingleValueEvent(mValueEventListener);
    }

    private void query2(){
        DebugLog.d(TAG,"query2");
        final String key = keyTV.getText().toString();
        if (TextUtils.isEmpty(key)) {
            keyTV.setError(REQUIRED);
            return;
        }
        final Query queryRef = mDatabase.orderByKey().endAt(key).limitToLast(2);
//        queryRef.addChildEventListener(mChildEventListener);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                queryRef.removeEventListener(mChildEventListener);
//            }
//        },1000);
        queryRef.addListenerForSingleValueEvent(mValueEventListener);
    }

    private ChildEventListener mChildEventListener = new ChildEventListener(){

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            DebugLog.d(TAG,"onChildAdded");
            display(dataSnapshot);

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            DebugLog.d(TAG,"onChildChanged");
            display(dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            DebugLog.d(TAG,"onChildMoved");
            display(dataSnapshot);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            DebugLog.d(TAG,"onChildMoved");
            display(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            DebugLog.d(TAG,"onCancelled");
        }
    };


    private ValueEventListener mValueEventListener = new ValueEventListener(){

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            DebugLog.d(TAG,"onDataChange");
            if (dataSnapshot.getChildren() instanceof Collection<?>) {
                int size= ((Collection<?>)dataSnapshot).size();
                DebugLog.d(TAG,"size:"+size);
            }

            int i = 0;
            for (Object obj : dataSnapshot.getChildren()) i++;
            DebugLog.d(TAG,"loop size:"+i);

            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                display(postSnapshot);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            DebugLog.d(TAG,"onCancelled");
        }
    };

    private void display(DataSnapshot dataSnapshot){
        DebugLog.d(TAG,"dataSnapshot==null:"+(dataSnapshot==null));
        DataInfo.User user = dataSnapshot.getValue(DataInfo.User.class);
        if(user==null){
            DebugLog.d(TAG,"user==null");
            return;
        }
        DebugLog.d(TAG, "username = " + user.username + " , note = " + user.note);
        String key = dataSnapshot.getKey();
        DebugLog.i(TAG, "getKey:"+key);
        DebugLog.i(TAG, "---------------");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityRealtimeDB.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugLog.i(TAG, "onDestroy");
        mDatabase.removeEventListener(mChildEventListener);
    }
}
