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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.MessagesViewHolder> {
    private List<Group> groupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private Context context;
    public GroupMessagesAdapter(List<Group> userMessagesList,Context context) {
        this.groupMessagesList = userMessagesList;
        this.context=context;
    }

    @NonNull
    @Override
    public GroupMessagesAdapter.MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.group_chat_convo_cards,parent,false);
        mAuth = FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();
        return new GroupMessagesAdapter.MessagesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessagesAdapter.MessagesViewHolder holder, int position) {
        String senderID=mAuth.getCurrentUser().getUid();
        Group groupMessages=groupMessagesList.get(position);
        String fromUserID=groupMessages.getFrom();
        String fromMessageType= groupMessages.getType();

        holder.senderLinearLayout.setVisibility(View.GONE);
        holder.receiverLinearLayout.setVisibility(View.GONE);
        holder.receiverImageLinearLayout.setVisibility(View.GONE);
        holder.senderImageLinearLayout.setVisibility(View.GONE);
        if(fromMessageType.equals("text")){
            if(fromUserID.equals(senderID)){
                holder.senderLinearLayout.setVisibility(View.VISIBLE);
                holder.senderTextView.setText(groupMessages.getMessage());
                holder.senderDate.setText(groupMessages.getTime());
            }
            else{
                holder.receiverLinearLayout.setVisibility(View.VISIBLE);
                holder.receiverTextView.setText(groupMessages.getMessage());
                holder.receiverDate.setText(groupMessages.getTime());
                setUserName(groupMessages.getFrom() ,holder );
            }
        }if(fromMessageType.equals("image")) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(groupMessages.getMessage());
            if (fromUserID.equals(senderID)) {
                holder.senderImageLinearLayout.setVisibility(View.VISIBLE);
                holder.senderImageDate.setText(groupMessages.getTime());
                GlideApp.with(context)
                        .load(storageReference)
                        .fitCenter()
                        .centerCrop()
                        .transform(new RoundedCorners(30))
                        .placeholder(R.drawable.ic_icon_awesome_image)
                        .into(holder.senderImageView);
                holder.senderImageDate.setText(groupMessages.getTime());
            } else {
                holder.receiverImageLinearLayout.setVisibility(View.VISIBLE);
                GlideApp.with(context)
                        .load(storageReference)
                        .fitCenter()
                        .centerCrop()
                        .transform(new RoundedCorners(30))
                        .placeholder(R.drawable.ic_icon_awesome_image)
                        .into(holder.receiverImageView);
                holder.receiverImageDate.setText(groupMessages.getTime());
                holder.receiverImageDate.setText(groupMessages.getTime());
            }
        }


    }
    void setUserName(String fromUserID, final MessagesViewHolder holder){
        rootRef.child("Users").child(fromUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name=snapshot.child("name").getValue().toString();
                    holder.reciverName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return groupMessagesList.size();
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, receiverTextView, senderDate , receiverDate , receiverImageDate, reciverName , senderImageDate;
        private LinearLayout receiverLinearLayout , receiverImageLinearLayout , senderImageLinearLayout;
        private LinearLayout senderLinearLayout;
        ImageView senderImageView;
        ImageView receiverImageView;
        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverLinearLayout= itemView.findViewById(R.id.receiver_box_card_layout);
            senderLinearLayout = itemView.findViewById(R.id.sender_box_card_layout);
            senderTextView =itemView.findViewById(R.id.sender_box_card);
            senderDate = itemView.findViewById(R.id.sender_box_card_date);
            receiverDate = itemView.findViewById(R.id.reciever_box_card_date);
            receiverTextView =itemView.findViewById(R.id.receiver_box_card);
            reciverName = itemView.findViewById(R.id.receiver_name_text_view);
            senderImageView=itemView.findViewById(R.id.sender_image_view);
            receiverImageView=itemView.findViewById(R.id.receiver_image_view);
            receiverImageLinearLayout = itemView.findViewById(R.id.receiver_image_view_linear_layout);
            senderImageLinearLayout = itemView.findViewById(R.id.sender_image_view_linear_layout);
            receiverImageDate = itemView.findViewById(R.id.reciever_box_card_image_date);
            senderImageDate = itemView.findViewById(R.id.sender_box_card_date);
        }

    }




}
