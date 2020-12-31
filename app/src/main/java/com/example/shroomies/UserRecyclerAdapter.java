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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.UserDetailsViewHolder> {
    private List<User> usersList;
    private Context context;
    private DatabaseReference rootRef;
    private View view;
    private ArrayList<User> members;
    public boolean fromSearchPage;
    public UserRecyclerAdapter(List<User> usersList, Context context,boolean fromSearchPage) {
        this.usersList = usersList;
        this.context = context;
        this.fromSearchPage=fromSearchPage;
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
    public void onBindViewHolder(@NonNull UserDetailsViewHolder holder, int position) {
     holder.username.setText(usersList.get(position).getName());
     holder.bio.setText(usersList.get(position).getBio());
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
                    members.add(usersList.get(getAdapterPosition()));
                }
            });
        }
    }
    public ArrayList<User> getMemberList(){
        return members;
    }

}
