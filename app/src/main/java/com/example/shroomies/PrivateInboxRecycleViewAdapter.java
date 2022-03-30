package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PrivateInboxRecycleViewAdapter extends RecyclerView.Adapter<PrivateInboxRecycleViewAdapter.UsersListViewHolder> {
    private final List<RecieverInbox> recieverInboxList;
    private final Context context , applicationContext;
    private HashMap<String, User> userHashMap;
    private FirebaseUser mUser;


    public PrivateInboxRecycleViewAdapter(List<RecieverInbox> receiverUsersList , HashMap<String , User > userHashMap, Context context, Context applicationContext) {
        this.recieverInboxList = receiverUsersList;
        this.context=context;
        this.userHashMap = userHashMap;
        this.applicationContext = applicationContext;
        mUser = FirebaseAuth.getInstance().getCurrentUser();

    }
    public void updateInbox(List<RecieverInbox> newList) {
        recieverInboxList.clear();
        recieverInboxList.addAll(newList);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsersListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_inbox_users_view,parent,false);
        return new UsersListViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final UsersListViewHolder holder, final int position) {

                //get the user data from the user hashmap
                if(userHashMap!=null){
                    User user=userHashMap.get(recieverInboxList.get(position).getReceiverID());
                    if(user!=null){
                        holder.receiverName.setText(user.getUsername());
                        holder.skeletonLoaderName.clearAnimation();
                        holder.skeletonLoaderName.setVisibility(View.GONE);
                        if(!user.getImage().isEmpty()) {
                            //todo add error image
                            GlideApp.with(context)
                                    .load(user.getImage())
                                    .fitCenter()
                                    .circleCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(holder.receiverImageView);
                        }else {
                            // make sure Glide doesn't load anything into this view until told otherwise
                            //todo change this
                            GlideApp.with(context).load(R.drawable.ic_user_profile_svgrepo_com).into(holder.receiverImageView);
                        }


                        holder.skeletonLoaderImage.clearAnimation();
                        holder.skeletonLoaderImage.setVisibility(View.GONE);
                    }
                }
                int unSeenMessages = recieverInboxList.get(position).getUnSeenMessageCount();
                if(unSeenMessages>0){
                    holder.newMessages.setVisibility(View.VISIBLE);
                    holder.newMessages.setText(Integer.toString(unSeenMessages));
                }else{
                    holder.newMessages.setVisibility(View.GONE);

                }
                if(recieverInboxList.get(position).getLastMessageTime()!=null){

                    Instant instant = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(recieverInboxList.get(position).getLastMessageTime(), Instant::from);
                    LocalDateTime messageDate = instant.atZone(ZoneOffset.systemDefault()).toLocalDateTime();
                    LocalDateTime now = LocalDateTime.now(OffsetDateTime.now().getOffset()).truncatedTo(ChronoUnit.SECONDS);

                    final long days = ChronoUnit.DAYS.between(messageDate, now);
                    //if the meesage is older than a week display the exact date
                    if(days>6){
                        holder.messageDate.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(messageDate));
                    }else if(days>0){
                        holder.messageDate.setText(DayOfWeek.from(messageDate.getDayOfWeek()).getDisplayName(TextStyle.SHORT , Locale.getDefault()));
                    }else{
                        holder.messageDate.setText(DateTimeFormatter.ofPattern("h:mm a").format(messageDate));
                    }
                }


                // set the last message
                if(recieverInboxList.get(position).getLastMessage()!=null && recieverInboxList.get(position).getFrom()!=null) {
                    String lastMessage = "";
                    if (recieverInboxList.get(position).getLastMessage().get("from").equals(mUser.getUid())) {
                        lastMessage += "You: " + recieverInboxList.get(position).getLastMessage().get(Config.message);

                    } else {
                        lastMessage += recieverInboxList.get(position).getLastMessage().get(Config.message);
                    }
                    holder.lastMessage.setText(lastMessage);
                    holder.skeletonLoaderLastMessage.clearAnimation();
                    holder.skeletonFrameLastMessage.setVisibility(View.GONE);
                }
    }

    @Override
    public int getItemCount() {
        return recieverInboxList.size();
    }

    public class UsersListViewHolder extends RecyclerView.ViewHolder{
        TextView lastMessage,receiverName , messageDate;
        MaterialButton newMessages;
        ImageView receiverImageView;
        RelativeLayout messageInboxViewHolderLayout;
        LottieAnimationView skeletonLoaderLastMessage, skeletonLoaderImage , skeletonLoaderName;
        CardView skeletonFrameLastMessage, skeletonFrameImage , skeletonFrameName ;
        public UsersListViewHolder(@NonNull View itemView) {
            super(itemView);
            lastMessage=itemView.findViewById(R.id.inbox_last_message);
            receiverImageView=itemView.findViewById(R.id.inbox_chat_item_image);
            receiverName=itemView.findViewById(R.id.inbox_chat_item_name);
            messageDate = itemView.findViewById(R.id.message_date);
            newMessages = itemView.findViewById(R.id.new_messages);

            skeletonLoaderLastMessage = itemView.findViewById(R.id.skeleton_loader_last_message);
            skeletonLoaderName = itemView.findViewById(R.id.skeleton_loader_name);
            skeletonLoaderImage = itemView.findViewById(R.id.skeleton_loader_image);

            skeletonFrameName = itemView.findViewById(R.id.skeleton_frame_name);
            skeletonFrameImage = itemView.findViewById(R.id.skeleton_frame_image);
            skeletonFrameLastMessage = itemView.findViewById(R.id.skeleton_frame_last_message);

            messageInboxViewHolderLayout=itemView.findViewById(R.id.message_inbox_users_view_layout);
            messageInboxViewHolderLayout.setOnClickListener(v -> {
                Intent intent=new Intent(applicationContext, ChattingActivity.class);
                if(userHashMap.containsKey(recieverInboxList.get(getAdapterPosition()).getReceiverID())){
                    User user = userHashMap.get(recieverInboxList.get(getAdapterPosition()).getReceiverID());
                    intent.putExtra("USER"  ,user);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    applicationContext.startActivity(intent);
                }else{
                    //todo handle error
                }
            });
        }

    }
    void setUserHashMap(HashMap<String , User> userHashMap){
        this.userHashMap = userHashMap;
    }

}
