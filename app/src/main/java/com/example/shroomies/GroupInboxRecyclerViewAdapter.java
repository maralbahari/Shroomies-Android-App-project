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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;

public class GroupInboxRecyclerViewAdapter extends RecyclerView.Adapter<GroupInboxRecyclerViewAdapter.GroupChatViewHolder> {
    private List<Group> groupList;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private Context context;

    public GroupInboxRecyclerViewAdapter(List<Group> groupList, Context context) {
        this.groupList = groupList;
        this.context=context;
    }

    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_inbox_users_view,parent,false);
        mAuth = FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();
        return new GroupChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupChatViewHolder holder, final int position) {
        holder.groupName.setText(groupList.get(position).getGroupName());
        //get the last message of this group
        getLastMessage(groupList.get(position).getGroupID() ,holder);
        getNumberOfUnreadMessages(groupList.get(position).getGroupID() ,holder);
        if(!groupList.get(position).getImage().isEmpty()) {
            holder.groupImage.setPadding(0, 0, 0, 0);
            GlideApp.with(context)
                    .load(groupList.get(position).getGroupImage())
                    .fitCenter()
                    .circleCrop()
                    .into(holder.groupImage);
        }

    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class GroupChatViewHolder extends RecyclerView.ViewHolder{
        TextView lastMessage,groupName , newMessages;
        ImageView groupImage;
        RelativeLayout messageInboxViewHolderLayout;
        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            lastMessage=itemView.findViewById(R.id.inbox_last_message);
            groupImage =itemView.findViewById(R.id.inbox_chat_item_image);
           groupName =itemView.findViewById(R.id.inbox_chat_item_name);
           newMessages = itemView.findViewById(R.id.new_messages);
            messageInboxViewHolderLayout=itemView.findViewById(R.id.message_inbox_users_view_layout);
            messageInboxViewHolderLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context,GroupChattingActivity.class);
                    intent.putExtra("GROUPID",groupList.get(getAdapterPosition()).getGroupID());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }
            });

        }
    }
    public void getLastMessage(String groupID , final GroupChatViewHolder holder){
       rootRef.child("GroupChats").child(groupID).child("Messages").orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
               if(snapshot.exists()){
                   Group groupMessages = snapshot.getValue(Group.class);
                   if(groupMessages.getType().equals("text")){
                        if(groupMessages.getMessage()!=null){
                             holder.lastMessage.setText(groupMessages.getMessage());
                             // check if the message is not seen
                        }
                   }else{
                       holder.lastMessage.setText("Photo");
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

           }});

    }
   void  getNumberOfUnreadMessages(String groupID , final GroupChatViewHolder holder){
       rootRef.child("GroupChats").child(groupID).child("Messages").addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {

               if(snapshot.exists()){
                   int numberOfUnseenMessages = 0;
                   for (DataSnapshot dataSnapshot
                   :snapshot.getChildren()) {
                       Group groupMessages = dataSnapshot.getValue(Group.class);
                       HashMap<String, Object> seenBy = groupMessages.getSeenBy();
                       if (seenBy.get(mAuth.getInstance().getCurrentUser().getUid()).equals("false") && !groupMessages.getFrom().equals(mAuth.getInstance().getCurrentUser().getUid())) {
                            numberOfUnseenMessages+=1;
                       }
                   }
                   if(numberOfUnseenMessages>0) {
                       holder.newMessages.setVisibility(View.VISIBLE);
                       holder.newMessages.setText(Integer.toString(numberOfUnseenMessages));
                   }else{
                       holder.newMessages.setVisibility(View.GONE);

                   }
               }else{
                   holder.newMessages.setVisibility(View.GONE);
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

    }

}
