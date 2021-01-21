package com.example.shroomies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    Context context;
    public MessagesAdapter(List<Messages> userMessagesList,Context context) {
        this.userMessagesList = userMessagesList;
        this.context = context;
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
        holder.senderLinearLayout.setVisibility(View.GONE);
        holder.receiverLinearLayout.setVisibility(View.GONE);
        holder.receiverImageLinearLayout.setVisibility(View.GONE);
        holder.senderImageLinearLayout.setVisibility(View.GONE);
       if(fromMessageType.equals("text")){
           if(fromUserID.equals(senderID)){
               holder.senderLinearLayout.setVisibility(View.VISIBLE);
              holder.senderTextView.setText(messages.getMessage());
              holder.senderDate.setText(messages.getTime());
           }
           else{
               holder.receiverLinearLayout.setVisibility(View.VISIBLE);
               holder.receiverTextView.setText(messages.getMessage());
               holder.receiverDate.setText(messages.getTime());
           }
       }if(fromMessageType.equals("image")) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(messages.getMessage());
            if (fromUserID.equals(senderID)) {
                holder.senderImageLinearLayout.setVisibility(View.VISIBLE);
                holder.senderImageDate.setText(messages.getTime());
                GlideApp.with(context)
                        .load(storageReference)
                        .fitCenter()
                        .centerCrop()
                        .transform(new RoundedCorners(30))
                        .placeholder(R.drawable.ic_icon_awesome_image)
                        .into(holder.senderImageView);
                holder.senderImageDate.setText(messages.getTime());
            } else {
                holder.receiverImageLinearLayout.setVisibility(View.VISIBLE);
                GlideApp.with(context)
                        .load(storageReference)
                        .fitCenter()
                        .centerCrop()
                        .transform(new RoundedCorners(30))
                        .placeholder(R.drawable.ic_icon_awesome_image)
                        .into(holder.receiverImageView);
                holder.receiverImageDate.setText(messages.getTime());
                holder.receiverImageDate.setText(messages.getTime());
            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, receiverTextView, senderDate , receiverDate , receiverImageDate , senderImageDate;
        private LinearLayout receiverLinearLayout , receiverImageLinearLayout , senderImageLinearLayout;
        private LinearLayout senderLinearLayout;
        ImageView senderImageView;
        ImageView receiverImageView;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverLinearLayout= itemView.findViewById(R.id.receiver_box_card_layout);
            senderLinearLayout = itemView.findViewById(R.id.sender_box_card_layout);
            senderTextView =itemView.findViewById(R.id.sender_box_card);
            senderDate = itemView.findViewById(R.id.sender_box_card_date);
            receiverDate = itemView.findViewById(R.id.reciever_box_card_date);
            receiverTextView =itemView.findViewById(R.id.receiver_box_card);
            senderImageView=itemView.findViewById(R.id.sender_image_view);
            receiverImageView=itemView.findViewById(R.id.receiver_image_view);
            receiverImageLinearLayout = itemView.findViewById(R.id.receiver_image_view_linear_layout);
            senderImageLinearLayout = itemView.findViewById(R.id.sender_image_view_linear_layout);
            receiverImageDate = itemView.findViewById(R.id.reciever_box_card_image_date);
            senderImageDate = itemView.findViewById(R.id.sender_box_card_date);

        }

    }

}
