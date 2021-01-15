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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        holder.lastMessage.setText(getLastMessage(groupList.get(position).getGroupID()));
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class GroupChatViewHolder extends RecyclerView.ViewHolder{
        TextView lastMessage,groupName;
        ImageView groupImage;
        RelativeLayout messageInboxViewHolderLayout;
        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            lastMessage=itemView.findViewById(R.id.inbox_last_message);
            groupImage =itemView.findViewById(R.id.inbox_chat_item_image);
           groupName =itemView.findViewById(R.id.inbox_chat_item_name);
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
    public String getLastMessage(String groupID){
        return "";
    }

}
