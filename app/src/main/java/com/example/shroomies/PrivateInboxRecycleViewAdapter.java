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

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
                rootRef.child("User").equalTo(receiverUsersList.get(position)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            User user=snapshot.getValue(User.class);
                            holder.receiverName.setText(user.getName());
                            GlideApp.with(context)
                                    .load(user.getImage())
                                    .transform(new RoundedCorners(1))
                                    .fitCenter()
                                    .centerCrop()
                                    .into(holder.receiverImageView);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // put image here

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
                    intent.putExtra("USERNAME",receiverName.toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }
            });

        }
    }
    public String getLastMessage(String receiverID){
        return "";
    }
}
