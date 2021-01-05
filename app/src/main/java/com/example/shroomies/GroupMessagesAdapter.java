package com.example.shroomies;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.MessagesViewHolder> {
    private List<Group> groupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    public GroupMessagesAdapter(List<Group> userMessagesList) {
        this.groupMessagesList = userMessagesList;
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
        String fromMessageType=groupMessages.getType();
        if(fromMessageType.equals("text")){
            holder.receiverCardView.setVisibility(View.INVISIBLE);
            if(fromUserID.equals(senderID)){
                holder.senderCardView.setBackgroundResource(R.drawable.sender_background_message);
                holder.senderCardView.setGravity(Gravity.LEFT);
                holder.senderCardView.setText(groupMessages.getMessage());
            }
            else{
                holder.receiverInfoContainer.setVisibility(View.INVISIBLE);
                holder.receiverCardView.setVisibility(View.VISIBLE);
                holder.receiverCardView.setBackgroundResource(R.drawable.receiver_message_background);
                holder.receiverCardView.setGravity(Gravity.LEFT);
                holder.receiverCardView.setText(groupMessages.getMessage());
                setUserName(fromUserID,holder);
            }
        }


    }
    void setUserName(String fromUserID, final MessagesViewHolder holder){
        rootRef.child("Users").orderByChild("ID").equalTo(fromUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name=snapshot.child("name").getValue().toString();
                    holder.receiverNameTV.setText(name);
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
        TextView senderCardView,receiverCardView,receiverNameTV;
        LinearLayout receiverInfoContainer;
        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            senderCardView=itemView.findViewById(R.id.sender_card_message);
            receiverCardView=itemView.findViewById(R.id.receiver_card_message);
            receiverNameTV=itemView.findViewById(R.id.receiver_name_textView);
            receiverInfoContainer=itemView.findViewById(R.id.linear_layout_receiver_card);
        }

    }

}
