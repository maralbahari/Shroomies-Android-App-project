package com.example.shroomies;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.UserDetailsViewHolder> {
    private List<User> usersList;
    private List<User> selectedUserList;
    private Context context;
    private DatabaseReference rootRef;
    private View view;
    private String groupID;
    public String fromWhere;
    private String currentUserId;
    private FirebaseAuth mAuth;


    public UserRecyclerAdapter(List<User> usersList, Context context,String fromWhere , String groupID ) {
        this.usersList = usersList;
        this.context = context;
        this.fromWhere = fromWhere;
        this.groupID = groupID;
        this.currentUserId = mAuth.getInstance().getCurrentUser().getUid();

        // create a new list to hold all selected users
    }

    public UserRecyclerAdapter(List<User> usersList, Context context, String fromWhere, List<User> selectedUserList ) {
        this.usersList = usersList;
        this.context = context;
        this.fromWhere = fromWhere;
        this.selectedUserList = selectedUserList;

    }

    public UserRecyclerAdapter(List<User> usersList, Context context,String fromWhere ) {
        this.usersList = usersList;
        this.context = context;
        this.fromWhere = fromWhere;
        // create a new list to hold all selected users
    }

    @NonNull
    @Override
    public UserDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view  = layoutInflater.inflate(R.layout.suggestion_user_list_,parent,false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        return new UserDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserDetailsViewHolder holder, int position) {
     holder.username.setText(usersList.get(position).getName());


        // storage referance to load the user's image if the userhas an
        //uploaded image
        if(!usersList.get(position).getImage().isEmpty()) {
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(usersList.get(position).getID());
            firebaseDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String url = snapshot.child("image").getValue(String.class);
                    if(!url.isEmpty()) {
                        GlideApp.with(context)
                                .load(url)
                                .fitCenter()
                                .circleCrop()
                                .into(holder.userImageProfile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }
     // if navigating from create group dialog 2 then we dont need the add user button to be shown or accessible
     if(!fromWhere.equals("SEARCH_PAGE")){
         holder.addUser.setVisibility(view.GONE);
     }
     // if the page is edit group chat
        // enable the user to delte by setting the visiblity of the icon to visible
        // also the visibilty should be gone for the user himself
     if(fromWhere.equals("EDIT_GROUP_INFO") ){
         holder.removeUserFromGroupChat.setVisibility(View.GONE);
         holder.addUser.setVisibility(View.GONE);

     }
     // turn the check button on if the user is selected
     // only for create chatgroupFragment
     if(selectedUserList!=null){
         if(selectedUserList.contains(usersList.get(position))){
             holder.addUser.setChecked(true);
         }
     }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class UserDetailsViewHolder extends RecyclerView.ViewHolder{
        ImageButton  removeUserFromGroupChat;
        ImageView userImageProfile;
        TextView username;
        CheckBox addUser;
        public UserDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            addUser= itemView.findViewById(R.id.add_user_to_group_button);
            userImageProfile=itemView.findViewById(R.id.suggestion_list_image);
            username=itemView.findViewById(R.id.suggestion_list_username);

            removeUserFromGroupChat = itemView.findViewById(R.id.remove_user_from_group_chat_image_button);
            if(fromWhere.equals("EDIT_GROUP_INFO")) {

                removeUserFromGroupChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String userId = usersList.get(getAdapterPosition()).getID();
                        rootRef.child("GroupChats").child(groupID).child("groupMembers").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    List<String> groupMemberIds =(ArrayList<String>) snapshot.getValue();
                                    groupMemberIds.remove(userId);
                                    HashMap<String , Object> updateGroupMembers  = new HashMap<>();
                                    updateGroupMembers.put("groupMembers" , groupMemberIds);
                                    rootRef.child("GroupChats").child(groupID).updateChildren(updateGroupMembers).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            usersList.remove(getAdapterPosition());
                                            notifyItemRemoved(getAdapterPosition());
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }
            addUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(addUser.isChecked()){
                        Toast.makeText(context , "user is checked" , Toast.LENGTH_LONG).show();
                        CreateChatGroupFragment.addSelectedMembers(usersList.get(getAdapterPosition()));
                    }else {
                        Toast.makeText(context , "user is unchecked" , Toast.LENGTH_LONG).show();
                        CreateChatGroupFragment.removeSelectedMember(usersList.get(getAdapterPosition()));
                    }
                }
            });
        }
    }


}
