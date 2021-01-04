package com.example.shroomies;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class FireBase_recycler_adapter extends FirebaseRecyclerAdapter<Model_personal, FireBase_recycler_adapter.MyViewHolder> {
    private Context context;

    public FireBase_recycler_adapter(@NonNull FirebaseRecyclerOptions<Model_personal> options, Context context) {
        super(options);
        this.context = context;
    }

    public FireBase_recycler_adapter(@NonNull FirebaseRecyclerOptions<Model_personal> options) {
        super(options);
    }


    // setting data to
    @Override
    protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull Model_personal model) {
        holder.TV_userDescription.setText(model.getDescription());
        holder.TV_DatePosted.setText(model.getDate());
        String id = model.getUserId();

        // getting data from user id
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
         DatabaseReference myRef = ref.child(id);

        myRef.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    User user = snapshot.getValue(User.class);
                    holder.TV_userName.setText(user.getName());
                    holder.TV_userOccupation.setText(user.getBio());

                    Glide.with(holder.IV_userPic.getContext()).
                            load(user.getImage())
                            .fitCenter()
                            .centerCrop()
                            .into(holder.IV_userPic);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //setting preferences
        if(!model.getPreferences().get(0)){
            holder.IV_male.setVisibility(View.GONE); }
        else holder.IV_male.setVisibility(View.VISIBLE);
        if(!model.getPreferences().get(1)){
            holder.IV_female.setVisibility(View.GONE); }
        else holder.IV_female.setVisibility(View.VISIBLE);
        if(!model.getPreferences().get(2)){
            holder.IV_pet.setVisibility(View.GONE); }
        else holder.IV_pet.setVisibility(View.VISIBLE);
        if(!model.getPreferences().get(3)){
            holder.IV_smoke.setVisibility(View.GONE); }
        else holder.IV_smoke.setVisibility(View.VISIBLE);



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

        ImageView IV_male;
        ImageView IV_female;
        ImageView IV_pet;
        ImageView IV_smoke;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            IV_userPic = itemView.findViewById(R.id.user_image_personal_card);
            TV_userName = itemView.findViewById(R.id.user_name_personal_card);
            TV_userOccupation = itemView.findViewById(R.id.bio_personal_card);
            TV_userBudget = itemView.findViewById(R.id.personal_post_budget_text_view);
            TV_DatePosted = itemView.findViewById(R.id.personal_post_date_text_view);
            TV_userDescription = itemView.findViewById(R.id.personal_card_text_view);
            Lay_card = itemView.findViewById(R.id.personal_card_view);

            IV_male = itemView.findViewById(R.id.male_image_view_apartment);
            IV_female = itemView.findViewById(R.id.female_image_view_apartment);
            IV_pet = itemView.findViewById(R.id.pets_allowd_image_view_apartment);
            IV_smoke = itemView.findViewById(R.id.non_smoking_image_view_apartment);
        }
    }
}
