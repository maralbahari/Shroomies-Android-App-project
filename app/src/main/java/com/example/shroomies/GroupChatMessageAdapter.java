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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupChatMessageAdapter extends RecyclerView.Adapter<GroupChatMessageAdapter.GroupMessageViewHolder>{
    private final ArrayList<GroupMessage> groupMessageList;
    private FirebaseAuth mAuth;
    private HashMap<String, User> memberHashmap;
     static Context context;

    public GroupChatMessageAdapter(ArrayList<GroupMessage> userMessagesList,Context context,HashMap<String, User> memberHashmap ) {
        this.groupMessageList = userMessagesList;
        this.memberHashmap=memberHashmap;
        this.context=context;
    }

    @NonNull
    @NotNull
    @Override
    public GroupChatMessageAdapter.GroupMessageViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.group_chat_convo_cards, parent, false);
        mAuth=FirebaseAuth.getInstance();
        return new GroupMessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupChatMessageAdapter.GroupMessageViewHolder holder, int position) {
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            GroupMessage groupMessage=groupMessageList.get(position);
            String senderID=groupMessage.getFrom();
            String messageType=groupMessage.getType();
            holder.senderLinearLayout.setVisibility(View.GONE);
            holder.receiverLinearLayout.setVisibility(View.GONE);
            holder.receiverImageLinearLayout.setVisibility(View.GONE);
            holder.senderImageLinearLayout.setVisibility(View.GONE);
            if (messageType.equals("text")){
                if(senderID.equals(currentUser.getUid())){
                    holder.senderLinearLayout.setVisibility(View.VISIBLE);
                    holder.senderTextView.setText(groupMessage.getMessage());
                    holder.senderDate.setText(groupMessage.getTime());

                }else{
                    String receiverName=memberHashmap.get(senderID).getUsername();
                    holder.receiverName.setText(receiverName);
                    holder.receiverLinearLayout.setVisibility(View.VISIBLE);
                    holder.receiverTextView.setText(groupMessage.getMessage());
                    holder.receiverDate.setText(groupMessage.getTime());
                }
            } if(messageType.equals("image")){
                if(senderID.equals(currentUser.getUid())){
                    GlideApp.with(context)
                            .load(groupMessage.getMessage())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .transform(new RoundedCorners(25))
                            .into(holder.senderImageView);
                    holder.senderImageDate.setText(groupMessage.getDate());
                }else{
                    GlideApp.with(context)
                            .load(groupMessage.getMessage())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .transform(new RoundedCorners(25))
                            .into(holder.receiverImageView);
                    holder.receiverImageDate.setText(groupMessage.getDate());
                }
            }

        }

    }

    @Override
    public int getItemCount() {
        return groupMessageList.size();
    }

    public class GroupMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView senderTextView, receiverTextView, senderDate, receiverDate, receiverImageDate, senderImageDate, receiverName;
        private LinearLayout receiverLinearLayout, receiverImageLinearLayout, senderImageLinearLayout;
        private LinearLayout senderLinearLayout;
        private ImageView senderImageView;
        private ImageView receiverImageView;
        public GroupMessageViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            receiverLinearLayout = itemView.findViewById(R.id.group_chat_receiver_box_card_layout);
            senderLinearLayout = itemView.findViewById(R.id.group_chat_sender_box_card_layout);
            senderTextView = itemView.findViewById(R.id.group_chat_sender_box_card_text);
            senderDate = itemView.findViewById(R.id.group_chat_sender_box_card_date);
            receiverDate = itemView.findViewById(R.id.group_chat_reciever_box_card_date);
            receiverTextView = itemView.findViewById(R.id.group_chat_receiver_box_card_text);
            senderImageView = itemView.findViewById(R.id.group_chat_sender_image_view);
            receiverImageView = itemView.findViewById(R.id.group_chat_receiver_image_view);
            receiverImageLinearLayout = itemView.findViewById(R.id.group_chat_receiver_image_view_linear_layout);
            senderImageLinearLayout = itemView.findViewById(R.id.group_chat_sender_image_view_linear_layout);
            receiverImageDate = itemView.findViewById(R.id.group_chat_reciever_box_card_image_date);
            senderImageDate = itemView.findViewById(R.id.group_chat_sender_box_card_date);
            receiverName=itemView.findViewById(R.id.group_chat_receiver_name_text_view);
        }
    }
}
