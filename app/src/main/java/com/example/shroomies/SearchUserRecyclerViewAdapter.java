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

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SearchUserRecyclerViewAdapter extends RecyclerView.Adapter<SearchUserRecyclerViewAdapter.ViewHolder>{
    private Context context;
    private List<User> userList;
    private DatabaseReference rootRef;


    public SearchUserRecyclerViewAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public SearchUserRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view  = layoutInflater.inflate(R.layout.search_user_adapter_card,parent,false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        return new SearchUserRecyclerViewAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull SearchUserRecyclerViewAdapter.ViewHolder holder, int position) {
        User user = userList.get(position);
        if(user.getBio()!=null) {
            holder.userBio.setText(userList.get(position).getBio());
        }
        holder.userName.setText(user.getUsername());
        if (user.getImage()!=null){
            if(!user.getImage().trim().isEmpty()) {
                GlideApp.with(context).
                        load(user.getImage())
                        .transform(new CenterCrop(), new CircleCrop())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(holder.userImage);
            }

        }


    }

    @Override
    public long getItemId(int position) {
        // Lets return in real stable id from here
        //getting the hash code will make every id unique
        return (userList.get(position).getUserID()).hashCode();
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName , userBio;
        RelativeLayout userCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage =  itemView.findViewById(R.id.user_image_search_card);
            userName =itemView.findViewById(R.id.user_name_search_card);
            userBio = itemView.findViewById(R.id.user_bio_search_card);
            userCard = itemView.findViewById(R.id.user_card);
            userCard.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChattingActivity.class);
                intent.putExtra("USER", userList.get(getAdapterPosition()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }

    }

}
