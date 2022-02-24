package com.example.geosafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.geosafe.model.Chat;
import com.example.geosafe.model.MyResponse;
import com.example.geosafe.model.Request;
import com.example.geosafe.model.User;
import com.example.geosafe.request.IFCMService;
import com.example.geosafe.utils.Tools;
import com.example.geosafe.viewHolder.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MessageActivity extends AppCompatActivity {
CircleImageView profile_image;
TextView username;
FirebaseUser fuser;
DatabaseReference reference;
Intent intent;
ImageButton btn_send;
EditText text_send;
MessageAdapter messageAdapter;
List<Chat> mchat;
RecyclerView recycler_msg;
    String userid;
    IFCMService ifcmService;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Messages");
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#E02947")));

        recycler_msg=findViewById(R.id.recycler_messages);
        recycler_msg.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recycler_msg.setLayoutManager(linearLayoutManager);
        profile_image=findViewById(R.id.profile);
        username=findViewById(R.id.username);
        btn_send=findViewById(R.id.btn_send);
        text_send=findViewById(R.id.text_send);
        intent=getIntent();
         userid= intent.getStringExtra("userid");
        Log.wtf("userid",""+userid);

        fuser=FirebaseAuth.getInstance().getCurrentUser();
        btn_send.setOnClickListener(v -> {
            String msg=text_send.getText().toString();
            if(!msg.equals("")){
                sendMessage(fuser.getUid(),userid,msg);
            }else{
                Toast.makeText(MessageActivity.this, "message est vide!", Toast.LENGTH_SHORT).show();
            }
            text_send.setText("");
        });

        reference= FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                User user = snapshot1.getValue(User.class);
                assert user != null;
                    Log.wtf("id",""+userid);

                    if (user.getUid().equals(userid)) {
                    Log.wtf("usersnap",""+user);
                    username.setText(user.getEmail());
                if(user.getAvatar().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(MessageActivity.this).load(user.getAvatar()).into(profile_image);
                }
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                    readMessages(fuser.getUid(), userid, "");
                }
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private  void sendMessage(String sender,String receiver,String message)
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("seen",false);

        reference.child(Tools.CHATS).push().setValue(hashMap);

        //Add user to chatlist
        final DatabaseReference chatRef=FirebaseDatabase.getInstance().getReference(Tools.CHATLIST)
        .child(fuser.getUid()).child(userid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("uid").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    /*later adding the msg seen and delivered feature
    * */
    private void readMessages(final String myid, final String userid,final String imageurl){
        mchat=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference(Tools.CHATS);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mchat.clear();
                for (DataSnapshot snap : snapshot.getChildren()){
                    Chat chat=snap.getValue(Chat.class);

                    assert chat != null;
                    Log.wtf("chatSender",""+chat.getSender());
                    Log.wtf("chatReceiver",""+chat.getReceiver());
                    Log.wtf("id",""+myid);
                    Log.wtf("id from intent",""+userid);
                    if(chat.getReceiver().equals(userid)&& chat.getSender().equals(myid) || chat.getReceiver().equals(myid) && chat.getSender().equals(userid)){
                        mchat.add(chat);
                    }
                    messageAdapter=new MessageAdapter(MessageActivity.this,mchat,imageurl);
                    recycler_msg.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                Intent aboutIntent = new Intent(MessageActivity.this,ChatActivity.class);
                startActivity(aboutIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}