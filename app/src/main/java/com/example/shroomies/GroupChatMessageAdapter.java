package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimeZone;

public class GroupChatMessageAdapter extends RecyclerView.Adapter<GroupChatMessageAdapter.GroupMessageViewHolder>{
    private final ArrayList<GroupMessage> groupMessageList;
    private FirebaseAuth mAuth;
    private final HashMap<String, User> memberHashmap;
    private final Context context;
    public   DateTimeFormatter dateformat =  DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
            .withZone(TimeZone.getDefault().toZoneId());
//    public onCardRepliedSelected seletedCard;
    private FragmentTransaction ft;

    public GroupChatMessageAdapter(ArrayList<GroupMessage> userMessagesList,Context context,HashMap<String, User> memberHashmap) {
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
            holder.senderCardView.setVisibility(View.GONE);
            holder.receiverCardView.setVisibility(View.GONE);
            String receiverName = "";
            if (memberHashmap.get(senderID) == null) {
                receiverName = null;
            } else {
                receiverName= memberHashmap.get(senderID).getUsername();
            }
            String messageDateFormatted  =  DateTimeFormatter.ofPattern("HH:mm")
                    .withZone(TimeZone.getDefault().toZoneId()).format(dateformat.parse(groupMessageList.get(position).getDate()));
            if (messageType.equals("text")){
                if(senderID.equals(currentUser.getUid())){
                    holder.senderLinearLayout.setVisibility(View.VISIBLE);
                    holder.senderTextView.setText(groupMessage.getMessage());
                    holder.senderDate.setText(messageDateFormatted);

                }else{
                    if(receiverName==null){
                        holder.receiverNameText.setText("Deleted Member");
                    }else{
                        holder.receiverNameText.setText(receiverName);
                    }
                    holder.receiverLinearLayout.setVisibility(View.VISIBLE);
                    holder.receiverTextView.setText(groupMessage.getMessage());
                    holder.receiverDate.setText(messageDateFormatted);
                }
            } if(messageType.equals("image")){
                holder.senderLinearLayout.setVisibility(View.GONE);
                holder.receiverLinearLayout.setVisibility(View.GONE);
                if(senderID.equals(currentUser.getUid())){
                    holder.senderImageLinearLayout.setVisibility(View.VISIBLE);
                    GlideApp.with(context)
                            .load(groupMessage.getMessage())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .transform(new RoundedCorners(25))
                            .into(holder.senderImageView);
                    holder.senderImageDate.setText(messageDateFormatted);
                }else{
                    holder.receiverImageLinearLayout.setVisibility(View.VISIBLE);
                    if(receiverName!=null){
                        holder.receiverNameImage.setText(receiverName);
                    }else{
                        holder.receiverNameImage.setText("Deleted Member");
                    }
                    GlideApp.with(context)
                            .load(groupMessage.getMessage())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .transform(new RoundedCorners(25))
                            .into(holder.receiverImageView);
                    holder.receiverImageDate.setText(messageDateFormatted);
                }
            } if(messageType.equals(Config.expensesCards) || messageType.equals(Config.taskCards)){
                String cardType="";
                String bundledCardType="";
                if(messageType.equals(Config.expensesCards)){
                    cardType="Expense Card";
                    bundledCardType=Config.expenses;
                }if(messageType.equals(Config.taskCards)){
                    cardType="Task Card";
                    bundledCardType=Config.task;
                }
                String importanceView=groupMessage.getCardImportance();
                int color;
                switch (importanceView) {
                    case  "2":
                        color = (context.getColor(R.color.canceRed));
                        break;
                    case  "1":
                        color=(context.getColor(R.color.orange));
                        break;
                    default:
                        color=(context.getColor(R.color.okGreen));
                }
                if(senderID.equals(currentUser.getUid())){
                    holder.senderCardView.setVisibility(View.VISIBLE);
                    holder.senderCardMessage.setText(groupMessage.getMessage());
                    holder.senderCardTitle.setText(groupMessage.getCardTitle());
                    holder.senderCardType.setText(cardType);
                    holder.senderCardImportance.setBackgroundColor(color);
                    holder.senderCardDate.setText(messageDateFormatted);
                    String finalBundledCardType = bundledCardType;
                    holder.senderCardView.setOnClickListener(view -> {
                        Intent intent=new Intent(context,MyShroomiesActivity.class);
                        intent.putExtra("CARDID",groupMessage.getCardID());
                        intent.putExtra("CARDTYPE",finalBundledCardType);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);

                    });

                } else{
                    holder.receiverCardView.setVisibility(View.VISIBLE);
                    holder.receiverCardMessage.setText(groupMessage.getMessage());
                    if (receiverName!=null){
                        holder.receiverCardName.setText(receiverName);
                    }else{
                        holder.receiverCardName.setText("Deleted Member");
                    }
                    String finalBundledCardType1 = bundledCardType;
                    holder.receiverCardTitle.setText(groupMessage.getCardID());
                    holder.receiverCardDate.setText(messageDateFormatted);
                    holder.receiverCardType.setText(cardType);
                    holder.receiverCardImportance.setBackgroundColor(color);
                    holder.receiverCardView.setOnClickListener(view -> {
                        Intent intent=new Intent(context,MyShroomiesActivity.class);
                        intent.putExtra("CARDID",groupMessage.getCardID());
                        intent.putExtra("CARDTYPE",finalBundledCardType1);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    });

                }

            }


        }

    }

    @Override
    public int getItemCount() {
        return groupMessageList.size();
    }

    public static class GroupMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView senderTextView;
        private final TextView receiverTextView;
        private final TextView senderDate;
        private final TextView receiverDate;
        private final TextView receiverImageDate;
        private final TextView senderImageDate;
        private final TextView receiverNameText;
        private final TextView receiverNameImage;
        private final LinearLayout receiverLinearLayout;
        private final LinearLayout receiverImageLinearLayout;
        private final LinearLayout senderImageLinearLayout;
        private final LinearLayout senderLinearLayout;
        private final CardView senderCardView;
        private final CardView receiverCardView;
        private final TextView senderCardTitle;
        private final TextView senderCardType;
        private final TextView senderCardMessage;
        private final TextView senderCardDate;
        private final TextView receiverCardName;
        private final TextView receiverCardTitle;
        private final TextView receiverCardType;
        private final TextView receiverCardMessage;
        private final TextView receiverCardDate;
        private final View senderCardImportance;
        private final View receiverCardImportance;
        private final ImageView senderImageView;
        private final ImageView receiverImageView;
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
            senderImageDate = itemView.findViewById(R.id.sender_box_card_image_date);
            receiverNameText =itemView.findViewById(R.id.group_chat_receiver_name_text_view);
            receiverNameImage=itemView.findViewById(R.id.group_chat_image_receiver_name);
            senderCardView=itemView.findViewById(R.id.reply_message_to_cards_sender);
            senderCardTitle=itemView.findViewById(R.id.reply_message_title_sender);
            senderCardImportance=itemView.findViewById(R.id.reply_message_importance_card_sender);
            senderCardDate=itemView.findViewById(R.id.reply_message_date_sender);
            senderCardType=itemView.findViewById(R.id.reply_message_type_sender);
            senderCardMessage=itemView.findViewById(R.id.reply_message_body_sender);
            receiverCardView=itemView.findViewById(R.id.reply_message_to_cards_receiver);
            receiverCardTitle=itemView.findViewById(R.id.reply_message_title_receiver);
            receiverCardType=itemView.findViewById(R.id.reply_message_type_receiver);
            receiverCardDate=itemView.findViewById(R.id.repl_message_date_receiver);
            receiverCardMessage=itemView.findViewById(R.id.reply_message_body_receiver);
            receiverCardName=itemView.findViewById(R.id.reply_message_receiver_name);
            receiverCardImportance=itemView.findViewById(R.id.reply_message_importance_card_receiver);
        }
    }
}
