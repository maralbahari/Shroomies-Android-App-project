package com.example.shroomies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.UserDetailsViewHolder> {
    private List<User> usersList;
    private Context context;
    private DatabaseReference rootRef;
    private View view;

    public boolean fromSearchPage;

    public UserRecyclerAdapter(List<User> usersList, Context context,boolean fromSearchPage) {
        this.usersList = usersList;
        this.context = context;
        this.fromSearchPage=fromSearchPage;
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
     holder.bio.setText(usersList.get(position).getBio());

        // storage referance to load the user's image if the userhas an
        //uploaded image
        if(!usersList.get(position).getImage().isEmpty()) {
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(usersList.get(position).getID());
            firebaseDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String url = snapshot.child("image").getValue(String.class);
                    GlideApp.with(context)
                            .load(url)
                            .fitCenter()
                            .circleCrop()
                            .into(holder.userImageProfile);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }
     // if navigating from create group dialog 2 then we dont need the add user button to be shown or accessible
     if(!(fromSearchPage)){
         holder.addUser.setVisibility(view.GONE);
     }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class UserDetailsViewHolder extends RecyclerView.ViewHolder{
        ImageButton addUser;
        ImageView userImageProfile;
        TextView username,bio;

        public UserDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            addUser= itemView.findViewById(R.id.add_user_to_group_button);
            userImageProfile=itemView.findViewById(R.id.suggestion_list_image);
            username=itemView.findViewById(R.id.suggestion_list_username);
            bio=itemView.findViewById(R.id.suggestion_list_bio);
            addUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addUser.setImageResource(R.drawable.ic_done_outline);
                    CreateChatGroupFragment.addSelectedMembers(usersList.get(getAdapterPosition()));
                }
            });
        }
    }


}
