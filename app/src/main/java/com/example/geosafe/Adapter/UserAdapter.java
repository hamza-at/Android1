package com.example.geosafe.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geosafe.ChatActivity;
import com.example.geosafe.HomeActivity;
import com.example.geosafe.Interface.IRecycleItemClickListener;
import com.example.geosafe.MessageActivity;
import com.example.geosafe.R;
import com.example.geosafe.model.Chat;
import com.example.geosafe.model.User;
import com.example.geosafe.utils.Tools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
   private List<User> mUsers;
   private OnListener mListener;
   private boolean isChat;
   String lmessage;

    public UserAdapter(Context mContext, List<User> mUsers,boolean b,OnListener onListener) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.mListener=onListener;
        this.isChat=b;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView email,message,name;
        public OnListener onListener;


        public ViewHolder(@NonNull View itemView,OnListener onListener) {
            super(itemView);
           email=itemView.findViewById(R.id.email_chat);
            name=itemView.findViewById(R.id.user_name_chat);
            message=itemView.findViewById(R.id.message);
            this.onListener=onListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onListener.onListener(getAbsoluteAdapterPosition(),email.getText().toString());
        }
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.list_chat_item,parent,false);

        return new UserAdapter.ViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
User user =mUsers.get(position);
holder.email.setText(user.getEmail());

holder.name.setText(user.getEmail().substring(0,user.getEmail().indexOf('@')));

        lastMessage(user.getUid(),holder.message);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public interface OnListener{
        void onListener(int position,String email);
    }


    private void lastMessage(final String userid,final TextView last_msg){
        lmessage="noMessage";
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference(Tools.CHATS);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Chat chat=snapshot1.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid())&& chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){
                        lmessage=chat.getMessage();
                    }}
                switch (lmessage){
                    case"noMessage" :
                        last_msg.setText("noMessage");
                        break;
                    default:
                        last_msg.setText(lmessage);
                        break;
                }
                lmessage="noMessage";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
