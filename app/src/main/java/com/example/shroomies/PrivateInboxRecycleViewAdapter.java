package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PrivateInboxRecycleViewAdapter extends RecyclerView.Adapter<PrivateInboxRecycleViewAdapter.UsersListViewHolder> {
    private List<String> receiverUsersList;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String receiverID;
    private Context context;

    public PrivateInboxRecycleViewAdapter(List<String> receiverUsersList, Context context) {
        this.receiverUsersList = receiverUsersList;
        this.context=context;
    }

    @NonNull
    @Override
    public UsersListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_inbox_users_view,parent,false);
        mAuth = FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();
        return new UsersListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersListViewHolder holder, final int position) {
                rootRef.child("Users").child(receiverUsersList.get(position)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            User user=snapshot.getValue(User.class);
                            holder.receiverName.setText(user.getName());
                            if(!user.getImage().isEmpty()) {
                                holder.receiverImageView.setPadding(0,0,0,0);
                                GlideApp.with(context)
                                        .load(user.getImage())
                                        .fitCenter()
                                        .circleCrop()
                                        .into(holder.receiverImageView);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // set the last message
                 getLastMessage(receiverUsersList.get(position) , holder);

    }

    @Override
    public int getItemCount() {
        return receiverUsersList.size();
    }

    public class UsersListViewHolder extends RecyclerView.ViewHolder{
        TextView lastMessage,receiverName;
        ImageView receiverImageView;
        RelativeLayout messageInboxViewHolderLayout;
        public UsersListViewHolder(@NonNull View itemView) {
            super(itemView);
            lastMessage=itemView.findViewById(R.id.inbox_last_message);
            receiverImageView=itemView.findViewById(R.id.inbox_chat_item_image);
            receiverName=itemView.findViewById(R.id.inbox_chat_item_name);
            messageInboxViewHolderLayout=itemView.findViewById(R.id.message_inbox_users_view_layout);
            messageInboxViewHolderLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context,ChattingActivity.class);
                    intent.putExtra("USERID",receiverUsersList.get(getAdapterPosition()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }
            });

        }
    }
    public void getLastMessage(String receiverID , final UsersListViewHolder viewHolder){
        rootRef.child("Messages").child(mAuth.getInstance().getCurrentUser().getUid()).child(receiverID).orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()) {
                 Messages messages = snapshot.getValue(Messages.class);
                 if(messages.getType().equals("text")){
                     viewHolder.lastMessage.setText(messages.getMessage());
                 }else{
                     viewHolder.lastMessage.setText("Photo");
                 }

                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
