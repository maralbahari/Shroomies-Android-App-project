package com.example.shroomies;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;



public class FireBase_recycler_adapter extends FirebaseRecyclerAdapter
        <Model_personal, FireBase_recycler_adapter.MyViewHolder>

{

    public FireBase_recycler_adapter(@NonNull FirebaseRecyclerOptions<Model_personal> options) {
        super(options);
    }


    // setting data to
    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Model_personal model) {
        holder.TV_userDescription.setText(model.getDescription());
        holder.TV_DatePosted.setText(model.getDate());



    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate
                (R.layout.personal_post_custom_card, parent, false);


        return new MyViewHolder(view);
    }






    // view holder class to hold the view
    public  class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView IV_userPic;
        TextView TV_userName;
        TextView TV_userOccupation;
        TextView TV_userBudget;
        TextView TV_DatePosted;
        TextView TV_userDescription;
        CardView Lay_card;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            IV_userPic = itemView.findViewById(R.id.user_image_personal_card);
            TV_userName = itemView.findViewById(R.id.user_name_personal_card);
            TV_userOccupation = itemView.findViewById(R.id.bio_personal_card);
            TV_userBudget = itemView.findViewById(R.id.personal_post_budget_text_view);
            TV_DatePosted = itemView.findViewById(R.id.personal_post_date_text_view);
            TV_userDescription = itemView.findViewById(R.id.personal_card_text_view);
            Lay_card = itemView.findViewById(R.id.personal_card_view);
        }
    }
}
