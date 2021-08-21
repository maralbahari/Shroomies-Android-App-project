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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class PrivateInboxRecycleViewAdapter extends RecyclerView.Adapter<PrivateInboxRecycleViewAdapter.UsersListViewHolder> {
    private List<RecieverInbox> recieverInboxList;
    private Context context;
    private HashMap<String , User> userHashMap;

    public PrivateInboxRecycleViewAdapter(List<RecieverInbox> receiverUsersList , HashMap<String , User > userHashMap, Context context) {
        this.recieverInboxList = receiverUsersList;
        this.context=context;
        this.userHashMap = userHashMap;

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

                holder.receiverName.setText(user.getName());
                if(!user.getImage().isEmpty()) {
                    holder.receiverImageView.setPadding(0, 0, 0, 0);
                    GlideApp.with(context)
                            .load(user.getImage())
                            .fitCenter()
                            .circleCrop()
                            .into(holder.receiverImageView);
                }
                // set the last message



    }

    @Override
    public int getItemCount() {
        return recieverInboxList.size();
    }

    public class UsersListViewHolder extends RecyclerView.ViewHolder{
        TextView lastMessage,receiverName , newMessages;
        ImageView receiverImageView;
        RelativeLayout messageInboxViewHolderLayout;
        public UsersListViewHolder(@NonNull View itemView) {
            super(itemView);
            lastMessage=itemView.findViewById(R.id.inbox_last_message);
            receiverImageView=itemView.findViewById(R.id.inbox_chat_item_image);
            receiverName=itemView.findViewById(R.id.inbox_chat_item_name);
            newMessages = itemView.findViewById(R.id.new_messages);
            messageInboxViewHolderLayout=itemView.findViewById(R.id.message_inbox_users_view_layout);
            messageInboxViewHolderLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context, ChattingActivity.class);
                    intent.putExtra("USERID", recieverInboxList.get(getAdapterPosition()).getReceiverID());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }
            });

        }
    }

}
