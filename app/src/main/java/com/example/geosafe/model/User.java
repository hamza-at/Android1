package com.example.geosafe.model;

import androidx.annotation.NonNull;

import com.example.geosafe.CercleRequestActivity;
import com.example.geosafe.utils.Tools;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class User {
   private String uid,email;
   private HashMap<String,User> acceptList; //list user friend
    private String avatar;
    private HashMap<String,User> CercleRequests;



    public User(){}

    public void setAcceptList(HashMap<String, User> acceptList) {
        this.acceptList = acceptList;
    }

    public void setCercleRequests(HashMap<String, User> cercleRequests) {
        CercleRequests = cercleRequests;
    }

    public User(String uid, String email){
        this.email=email;
        this.uid=uid;
        DatabaseReference querygene=FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION);
        DatabaseReference userList= querygene.child(uid).child(Tools.ACCEPTLIST);

        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapShot : snapshot.getChildren()) {
                    User user = userSnapShot.getValue(User.class);
                    acceptList=new HashMap<>();

                    if (user != null){

                    acceptList.put(user.getUid(),user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference requests= querygene.child(uid).child(Tools.CERCLE_REQUEST);
        requests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapShot : snapshot.getChildren()) {
                    User user = userSnapShot.getValue(User.class);
                    CercleRequests = new HashMap<>();

                    if (user != null) {
                        CercleRequests.put(user.getUid(), user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public User(String uid, String email, String avatar){
         this.email=email;
         this.uid=uid;
         acceptList= new HashMap<>();
         this.avatar = avatar;
        CercleRequests=new HashMap<>();

     }

    public String getUid() {
         return uid;
    }
    public  String getEmail(){
         return email;
    }

    public HashMap<String, User> getAcceptList() {
        DatabaseReference querygene=FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION);
        DatabaseReference userList= querygene.child(uid).child(Tools.ACCEPTLIST);

        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapShot : snapshot.getChildren()) {
                    User user = userSnapShot.getValue(User.class);
                    acceptList=new HashMap<>();

                    if (user != null){

                        acceptList.put(user.getUid(),user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return acceptList;
    }

    public HashMap<String, User> getCercleRequests() {
        DatabaseReference querygene=FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION);

        DatabaseReference requests= querygene.child(uid).child(Tools.CERCLE_REQUEST);
        requests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapShot : snapshot.getChildren()) {
                    User user = userSnapShot.getValue(User.class);
                    CercleRequests = new HashMap<>();

                    if (user != null) {
                        CercleRequests.put(user.getUid(), user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return CercleRequests;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", Uid='" + uid + '\'' +
                ", username='" + email.substring(0,email.indexOf('@'))+ '\'' +
                ", avatar='" + avatar + '\'' +
                ", acceptList='" + acceptList + '\''+
                ", CercleRequests=' "+ CercleRequests + '\''+
                '}';
    }



}
