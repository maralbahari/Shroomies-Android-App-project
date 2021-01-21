package com.example.shroomies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PersonalPostRecyclerAdapter extends RecyclerView.Adapter<PersonalPostRecyclerAdapter.PersonalPostViewHolder> {
    List <PersonalPostModel> personalPostModelList;
    Context context;

    public PersonalPostRecyclerAdapter(List<PersonalPostModel> personalPostModelList, Context context) {
        this.personalPostModelList = personalPostModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public PersonalPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view  = layoutInflater.inflate(R.layout.personal_post_custom_card,parent,false);
//        rootRef= FirebaseDatabase.getInstance().getReference();
        return new PersonalPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PersonalPostViewHolder holder, final int position) {
        holder.TV_userDescription.setText(personalPostModelList.get(position).getDescription());
        holder.TV_DatePosted.setText(personalPostModelList.get(position).getDate());
        String id = personalPostModelList.get(position).getUserID();

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
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        //setting preferences
        if(!personalPostModelList.get(position).getPreferences().get(0)){
            holder.IV_male.setVisibility(View.GONE); }
        else holder.IV_male.setVisibility(View.VISIBLE);
        if(!personalPostModelList.get(position).getPreferences().get(1)){
            holder.IV_female.setVisibility(View.GONE); }
        else holder.IV_female.setVisibility(View.VISIBLE);
        if(!personalPostModelList.get(position).getPreferences().get(2)){
            holder.IV_pet.setVisibility(View.GONE); }
        else holder.IV_pet.setVisibility(View.VISIBLE);
        if(!personalPostModelList.get(position).getPreferences().get(3)){
            holder.IV_smoke.setVisibility(View.GONE); }
        else holder.IV_smoke.setVisibility(View.VISIBLE);


        // getting current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String Uid = firebaseUser.getUid();

        if(id.equals(Uid)){
            holder.BT_message.setVisibility(View.GONE);
            holder.BT_fav.setVisibility(View.GONE);
        }
        else {
            holder.BT_message.setVisibility(View.VISIBLE);
            holder.BT_fav.setVisibility(View.VISIBLE);
        }
        final Boolean[] checkClick = {false};

        final DatabaseReference favRef = FirebaseDatabase.getInstance().getReference().child("Favorite");

        holder.BT_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkClick[0] = true;
                favRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(checkClick[0]){
                            if(snapshot.child(Uid).child("PersonalPost").hasChild(personalPostModelList.get(position).getId())){
                                favRef.child(Uid).child("PersonalPost").child(personalPostModelList.get(position).getId()).removeValue();
                                checkClick[0] = false;
                            }
                            else {
                                favRef.child(Uid).child("PersonalPost")
                                        .child(personalPostModelList.get(position).getId()).setValue(personalPostModelList.get(position).getId());
                                checkClick[0] = false;

                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() { return personalPostModelList.size(); }








    // Viewholder class
    class PersonalPostViewHolder extends RecyclerView.ViewHolder{

        //initializing
        ImageView IV_userPic;
        TextView TV_userName;
        TextView TV_userOccupation;
        TextView TV_userBudget;
        TextView TV_DatePosted;
        TextView TV_userDescription;
        CardView Lay_card;
        ImageButton BT_fav;
        Button BT_message;

        ImageView IV_male;
        ImageView IV_female;
        ImageView IV_pet;
        ImageView IV_smoke;


        public PersonalPostViewHolder(@NonNull View itemView) {
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
            BT_message = itemView.findViewById(R.id.start_chat_button);
            BT_fav = itemView.findViewById(R.id.BUT_fav);



        }
    }

}