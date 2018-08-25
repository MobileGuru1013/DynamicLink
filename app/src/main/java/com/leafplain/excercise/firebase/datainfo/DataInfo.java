package com.leafplain.excercise.firebase.datainfo;

import java.io.Serializable;

/**
 * Created by kennethyeh on 2016/12/27.
 */

public class DataInfo {

    public static class User  implements Serializable {

        public String username;
        public String note;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String note) {
            this.username = username;
            this.note = note;
        }

    }


    public static class Chatroom  implements Serializable {
        public String id;
        public String name;
        public String desc;

        public Chatroom() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }
    }

    public static class ChatUser implements Serializable {
        public String deviceId;
        public String nickname;

        public ChatUser() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }
    }

    public static class ChatPost implements Serializable {
        public String id;
        public String content;
        public String deviceId;
        public String nickname;
        public Object timestamp ;

        public ChatPost() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }
    }
}
