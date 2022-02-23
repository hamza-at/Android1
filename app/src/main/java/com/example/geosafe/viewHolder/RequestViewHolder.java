package com.example.geosafe.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geosafe.Interface.IRecycleItemClickListener;
import com.example.geosafe.R;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geosafe.Interface.IRecycleItemClickListener;
import com.example.geosafe.R;
import java.text.BreakIterator;

public class RequestViewHolder extends RecyclerView.ViewHolder{
    public TextView txt_user_email;
    public TextView txt_user_name;
    public ImageView accept_bt, decline_bt;
    public RequestViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_user_email = (TextView) itemView.findViewById(R.id.user_email);
        txt_user_name = (TextView) itemView.findViewById(R.id.user_name);
        accept_bt=(ImageView) itemView.findViewById(R.id.acceptRq);
        decline_bt=(ImageView) itemView.findViewById(R.id.declineRq);

    }
}