package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import com.virgilsecurity.common.callback.OnResultListener;
import com.virgilsecurity.sdk.cards.Card;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PrivateInboxRecycleViewAdapter extends RecyclerView.Adapter<PrivateInboxRecycleViewAdapter.UsersListViewHolder> {
    private final List<RecieverInbox> recieverInboxList;
    private final Context context , applicationContext;
    private final HashMap<String , User> userHashMap;


    public PrivateInboxRecycleViewAdapter(List<RecieverInbox> receiverUsersList , HashMap<String , User > userHashMap, Context context, Context applicationContext) {
        this.recieverInboxList = receiverUsersList;
        this.context=context;
        this.userHashMap = userHashMap;
        this.applicationContext = applicationContext;

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
                User user=userHashMap.get(recieverInboxList.get(position).getReceiverID());
                int unSeenMessages = recieverInboxList.get(position).getUnSeenMessageCount();
                if(unSeenMessages>0){
                    holder.newMessages.setVisibility(View.VISIBLE);
                    holder.newMessages.setText(Integer.toString(unSeenMessages));
                }
                if(recieverInboxList.get(position).getLastMessageTime()!=null){
                    DateTimeFormatter formatter = DateTimeFormatter
                            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                            .withZone(ZoneOffset.UTC);
                    ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
                    String currentDateString = formatter.format(now);
                    final LocalDateTime messageDate = LocalDateTime.parse(recieverInboxList.get(position).getLastMessageTime(), formatter);
                    final LocalDateTime currentDate = LocalDateTime.parse(currentDateString  , formatter);
                    final long days = ChronoUnit.DAYS.between(messageDate, currentDate);
                    //if the meesage is older than a week display the exact date
                    if(days>6){
                        holder.messageDate.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(messageDate));
                    }else if(days>0){
                        holder.messageDate.setText(DayOfWeek.from(messageDate.getDayOfWeek()).getDisplayName(TextStyle.SHORT , Locale.getDefault()));
                    }else{
                        String time = messageDate.getHour()+":"+messageDate.getMinute();
                        holder.messageDate.setText(time);
                    }
                }

                holder.receiverName.setText(user.getName());
                if(!user.getImage().isEmpty()) {
                    //todo add error image
                    GlideApp.with(context)
                            .load(user.getImage())
                            .fitCenter()
                            .circleCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(holder.receiverImageView);
                }
                // set the last message
                if(recieverInboxList.get(position).getLastMessage()!=null){
                    decryptMessage(recieverInboxList.get(position).getFrom() ,recieverInboxList.get(position).getLastMessage() , holder);
                }
    }
    private void decryptMessage(String userID , String encryptedMessage ,UsersListViewHolder usersListViewHolder) {
        // get the user's card
        OnResultListener<Card> findUsersListener =
                new OnResultListener<Card>() {
                    @Override
                    public void onSuccess(Card card) {
//                            com.virgilsecurity.common.model.Data data = new com.virgilsecurity.common.model.Data(messageText.getBytes());
//                            // Encrypt data using user public keys
//                            com.virgilsecurity.common.model.Data encryptedData = eThree.authEncrypt(data, findUsersResult);
////                             Encrypt message using user public key
                        String decryptMessage= EthreeSingleton
                                .getInstance(null,null,null)
                                .getEthreeInstance()
                                .authDecrypt(encryptedMessage , card);

                        ((AppCompatActivity) context).runOnUiThread(() -> {
                            usersListViewHolder.lastMessage.setText(decryptMessage);
                        });
                    }

                    @Override
                    public void onError(@NotNull Throwable throwable) {
                        Log.d("recepientVirgilCard", throwable.getMessage());
                    }
                };
        EthreeSingleton.getInstance(null,null,null).getEthreeInstance().findUser(userID, true).addCallback(findUsersListener);

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
        public UsersListViewHolder(@NonNull View itemView) {
            super(itemView);
            lastMessage=itemView.findViewById(R.id.inbox_last_message);
            receiverImageView=itemView.findViewById(R.id.inbox_chat_item_image);
            receiverName=itemView.findViewById(R.id.inbox_chat_item_name);
            messageDate = itemView.findViewById(R.id.message_date);
            newMessages = itemView.findViewById(R.id.new_messages);
            messageInboxViewHolderLayout=itemView.findViewById(R.id.message_inbox_users_view_layout);
            messageInboxViewHolderLayout.setOnClickListener(v -> {
                Intent intent=new Intent(applicationContext, ChattingActivity.class);
                intent.putExtra("USERID", recieverInboxList.get(getAdapterPosition()).getReceiverID());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
            });
        }

    }

}
