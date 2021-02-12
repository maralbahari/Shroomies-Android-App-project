package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnSuccessListener;
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
    DatabaseReference db;
    private User receiverUser;
    DatabaseReference myRef;
    Boolean isFromPersonalProfile;
    Boolean isFromfav;


    public PersonalPostRecyclerAdapter(List<PersonalPostModel> personalPostModelList, Context context, Boolean isFromfav) {
        this.personalPostModelList = personalPostModelList;
        this.context = context;
        this.isFromfav = isFromfav;
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
        String description = personalPostModelList.get(position).getDescription();
        if(description.isEmpty()){
            holder.TV_userDescription.setText("no description added");
        }else{
            holder.TV_userDescription.setText(description);
        }
        holder.TV_DatePosted.setText(personalPostModelList.get(position).getDate().split(" ")[0]);
        holder.TV_userBudget.setText("Budget: " + personalPostModelList.get(position).getPrice());
        String id = personalPostModelList.get(position).getUserID();

        // getting data from user id
         myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    User user = snapshot.getValue(User.class);
                    holder.TV_userName.setText(user.getName());
//                    holder.TV_userOccupation.setText(user.getBio());
                    if (user.getImage()!=null){
                    Glide.with(holder.IV_userPic.getContext()).
                            load(user.getImage())
                            .transform(new CenterCrop() , new CircleCrop())
                            .into(holder.IV_userPic);
                    holder.IV_userPic.setPadding(3,3,3,3);
                    }

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
//        if(isFromPersonalProfile){ holder.}


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

        DatabaseReference anotherFavRef = FirebaseDatabase.getInstance().getReference().child("Favorite");
        anotherFavRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(Uid).child("PersonalPost").hasChild(personalPostModelList.get(position).getId())){
                    holder.BT_fav.setImageResource(R.drawable.ic_icon_awesome_star_checked);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(isFromfav){

            holder.BT_fav.setImageResource(R.drawable.ic_icon_awesome_star_checked);
        }


        else{
        holder.BT_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkClick[0] = true;
                favRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(checkClick[0])
                            if(snapshot.child(Uid).child("PersonalPost").hasChild(personalPostModelList.get(position).getId())){
                                favRef.child(Uid).child("PersonalPost").child(personalPostModelList.get(position).getId()).removeValue();
                                holder.BT_fav.setImageResource(R.drawable.ic_icon_awesome_star_unchecked);
                                Toast.makeText(context,"Post removed from favorites",Toast.LENGTH_LONG).show();
                                checkClick[0] = false;
                            }
                            else {
                                DatabaseReference anotherRef = favRef.child(Uid).child("PersonalPost").child(personalPostModelList.get(position).getId());
                                anotherRef.child("id").setValue(personalPostModelList.get(position).getId());
                                anotherRef.child("userID").setValue(personalPostModelList.get(position).getUserID());
                                anotherRef.child("price").setValue(personalPostModelList.get(position).getPrice());
                                anotherRef.child("date").setValue(personalPostModelList.get(position).getDate());
                                anotherRef.child("description").setValue(personalPostModelList.get(position).getDescription());
                                anotherRef.child("preferences").setValue(personalPostModelList.get(position).getPreferences());
                                anotherRef.child("latitude").setValue(personalPostModelList.get(position).getLatitude());
                                anotherRef.child("longitude").setValue(personalPostModelList.get(position).getLongitude());
                                Toast.makeText(context,"Post added to favorites",Toast.LENGTH_LONG).show();
                                holder.BT_fav.setImageResource(R.drawable.ic_icon_awesome_star_checked);
                                checkClick[0] = false;

                            }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });}

    }

    @Override
    public int getItemCount() { return personalPostModelList.size(); }

    // Viewholder class
    class PersonalPostViewHolder extends RecyclerView.ViewHolder{

        //initializing
        ImageView IV_userPic;
        TextView TV_userName;
//        TextView TV_userOccupation;
        TextView TV_userBudget;
        TextView TV_DatePosted;
        TextView TV_userDescription;
        RelativeLayout Lay_card;
        ImageButton BT_fav;
        Button BT_message;
        ImageButton deletePost;
        ImageView IV_male;
        ImageView IV_female;
        ImageView IV_pet;
        ImageView IV_smoke;


        public PersonalPostViewHolder(@NonNull View itemView) {
            super(itemView);

            IV_userPic = itemView.findViewById(R.id.user_image_personal_card);
            TV_userName = itemView.findViewById(R.id.user_name_personal_card);
//            TV_userOccupation = itemView.findViewById(R.id.bio_personal_card);
            TV_userBudget = itemView.findViewById(R.id.personal_post_budget_text_view);
            TV_DatePosted = itemView.findViewById(R.id.personal_post_date_text_view);
            TV_userDescription = itemView.findViewById(R.id.personal_card_text_view);
            Lay_card = itemView.findViewById(R.id.relative_layout_personal_card);

            IV_male = itemView.findViewById(R.id.male_image_view_apartment);
            IV_female = itemView.findViewById(R.id.female_image_view_apartment);
            IV_pet = itemView.findViewById(R.id.pets_allowd_image_view_apartment);
            IV_smoke = itemView.findViewById(R.id.non_smoking_image_view_apartment);
            BT_message = itemView.findViewById(R.id.start_chat_button);
            BT_fav = itemView.findViewById(R.id.BUT_fav);

            BT_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(personalPostModelList.get(getAdapterPosition()).getUserID());

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            receiverUser = new User();
                            receiverUser = snapshot.getValue(User.class);
                            Intent intent = new Intent(context, ChattingActivity.class);
                            intent.putExtra("USERID", receiverUser.getID());
                            context.startActivity(intent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            if (isFromfav) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                final String Uid = firebaseUser.getUid();
                final DatabaseReference favRef = FirebaseDatabase.getInstance().getReference().child("Favorite");
                BT_fav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        favRef.child(Uid).child("PersonalPost").child(personalPostModelList.get(getAdapterPosition()).getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                personalPostModelList.remove(getAdapterPosition());
                                notifyItemRemoved(getAdapterPosition());
                            }


            });
        }
    });

}
}
    }
}
