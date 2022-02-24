package com.example.geosafe.viewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geosafe.Interface.IRecycleItemClickListener;
import com.example.geosafe.R;

public class MessagesListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public IRecycleItemClickListener iRecycleItemClickListener;
    public TextView txt_user_email;
    public TextView txt_user_name,txt_message;

    public void setiRecycleItemClickListener(IRecycleItemClickListener iRecycleItemClickListener){
        this.iRecycleItemClickListener=iRecycleItemClickListener;
    }

    public MessagesListViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_user_email = (TextView) itemView.findViewById(R.id.user_email);
        txt_user_name = (TextView) itemView.findViewById(R.id.user_name);
        txt_message =(TextView) itemView.findViewById(R.id.bio);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        iRecycleItemClickListener.onItemClickListener(view,getAbsoluteAdapterPosition());

    }
}
