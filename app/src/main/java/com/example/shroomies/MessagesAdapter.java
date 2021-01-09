package com.example.shroomies;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_chat_cards,parent,false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.MessageViewHolder holder, int position) {
       String senderID=mAuth.getCurrentUser().getUid();
       Messages messages=userMessagesList.get(position);
       String fromUserID=messages.getFrom();
       String fromMessageType=messages.getType();
       if(fromMessageType.equals("text")){
           holder.receiverCardView.setVisibility(View.INVISIBLE);
           if(fromUserID.equals(senderID)){
              holder.senderCardView.setBackgroundResource(R.drawable.sender_background_message);
              holder.senderCardView.setTextColor(Color.BLACK);
              holder.senderCardView.setGravity(Gravity.LEFT);
              holder.senderCardView.setText(messages.getMessage());
           }
           else{
               holder.senderCardView.setVisibility(View.INVISIBLE);
               holder.receiverCardView.setVisibility(View.VISIBLE);
               holder.receiverCardView.setBackgroundResource(R.drawable.receiver_message_background);
               holder.receiverCardView.setTextColor(Color.BLACK);
               holder.receiverCardView.setGravity(Gravity.LEFT);
               holder.receiverCardView.setText(messages.getMessage());
           }
       }if(fromMessageType.equals("image")) {
            holder.senderCardView.setVisibility(View.INVISIBLE);
            holder.receiverCardView.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(senderID)) {
                holder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_icon_awesome_image).into(holder.senderImageView);

            } else {
                holder.senderImageView.setVisibility(View.GONE);
                holder.receiverImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_icon_awesome_image).into(holder.receiverImageView);
            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderCardView,receiverCardView;
        ImageView senderImageView;
        ImageView receiverImageView;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderCardView=itemView.findViewById(R.id.sender_box_card);
            receiverCardView=itemView.findViewById(R.id.receiver_box_card);
            senderImageView=itemView.findViewById(R.id.sender_image_view);
            receiverImageView=itemView.findViewById(R.id.receiver_image_view);
        }

    }

}
