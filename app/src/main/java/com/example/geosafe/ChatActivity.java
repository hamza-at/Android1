package com.example.geosafe;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geosafe.Adapter.UserAdapter;
import com.example.geosafe.model.ChatList;
import com.example.geosafe.model.User;
import com.example.geosafe.utils.Tools;
import com.example.geosafe.viewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity implements UserAdapter.OnListener {
    public RecyclerView recyclerView;
private List<User> mUsers;
   public  FirebaseRecyclerAdapter<User, UserViewHolder> adapter;
  FirebaseUser fuser;
DatabaseReference reference;
UserAdapter userAdapter;

private List<ChatList> usersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("All messages");
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#E02947")));

        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fuser= FirebaseAuth.getInstance().getCurrentUser();
        usersList=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference(Tools.CHATLIST).child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    ChatList chatlist=snapshot1.getValue(ChatList.class);
                    usersList.add(chatlist);
                    assert chatlist != null;
                    Log.wtf("Chatlistitem",""+chatlist.uid);
                    Log.wtf("fuser",""+fuser);



                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void chatList() {
        mUsers=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    User user=snapshot1.getValue(User.class);
                    Log.wtf("userx",""+user);
                    Log.wtf("chatlist",""+usersList);

                    for(ChatList chatList: usersList){

                        assert user != null;
                        if(user.getUid().equals(chatList.uid)){
                            mUsers.add(user);
                        }
                    }
                }
                Log.wtf("list",""+mUsers);


                userAdapter=new UserAdapter(getApplicationContext(),mUsers,true,ChatActivity.this::onListener);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent aboutIntent = new Intent(ChatActivity.this,HomeActivity.class);
                startActivity(aboutIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStop() {
        if (adapter != null) {
            adapter.stopListening();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onListener(int position,String email) {
        List<String> list = new ArrayList<>();
        Log.wtf("email", email);
        DatabaseReference query = FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    if (user == null)
                        Toast.makeText(ChatActivity.this, "user has deleted his account", Toast.LENGTH_SHORT).show();
                    assert user != null;
                    //check if it is not null
                    Log.wtf("usersnapshot contains id", "" + user.getUid());
                    Log.wtf("usersnapshot contains email", "" + user.getEmail());


                    if (user.getEmail().equals(email)) {
                        Log.wtf("id", "" + user.getUid());
                        list.add(user.getUid());
                        Log.wtf("size", "" + list.size());
                        if (list.size() > 0) {
                            Intent intent = new Intent(ChatActivity.this, MessageActivity.class);
                            intent.putExtra("userid", list.get(0));
                            list.clear();
                            startActivity(intent);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//check list size again
        Log.wtf(" list size",""+list.size());

    }
}