package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PersonalPostRecyclerAdapter extends RecyclerView.Adapter<PersonalPostRecyclerAdapter.PersonalPostViewHolder> {
    private static final String PERSONAL_FAVOURITES = "PERSONAL_FAVOURITES";
    private List <PersonalPostModel> personalPostModelList;
    private Context context;
    private User receiverUser;
    private DatabaseReference myRef;
    private Boolean isFromfav;
    private Set<String> favoriteSet;
    private String userId;
    private boolean isFromUserProfile;


    public PersonalPostRecyclerAdapter(List<PersonalPostModel> personalPostModelList, Context context,String userId, Boolean isFromfav,boolean isFromUserProfile) {
        this.personalPostModelList = personalPostModelList;
        this.context = context;
        this.isFromfav = isFromfav;
        this.userId = userId;
        this.isFromUserProfile=isFromUserProfile;
        favoriteSet = context.getSharedPreferences(userId , Context.MODE_PRIVATE).getStringSet(PERSONAL_FAVOURITES, null);
        if(favoriteSet==null){
            favoriteSet = new HashSet<>();
        }
    }

    @NonNull
    @Override
    public PersonalPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view  = layoutInflater.inflate(R.layout.personal_post_custom_card,parent,false);

        return new PersonalPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PersonalPostViewHolder holder, final int position) {
        String description = personalPostModelList.get(position).getDescription();
        if(description.isEmpty()){
            holder.userDescription.setText("no description added");
        }else{
            holder.userDescription.setText(description);
        }
//        holder.TV_DatePosted.setText(personalPostModelList.get(position).getDate().split(" ")[0]);
        holder.userBudget.setText("Budget: " + personalPostModelList.get(position).getPrice());
        String id = personalPostModelList.get(position).getUserID();
        // getting data from user id
         myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    holder.userName.setText(user.getName());
//                    holder.TV_userOccupation.setText(user.getBio());
                    if (user.getImage()!=null){
                    Glide.with(context).
                            load(user.getImage())
                            .transform(new CenterCrop() , new CircleCrop())
                            .into(holder.userPic);
                    holder.userPic.setPadding(3,3,3,3);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
//        setting preferences
//        for (String preference
//                :personalPostModelList.get(position).getPreferences()) {
//            switch (preference) {
//                case "male":
//                    holder.IV_male.setVisibility(View.VISIBLE);
//                    break;
//                case "female":
//                    holder.IV_female.setVisibility(View.VISIBLE);
//                    break;
//                case "pet":
//                    holder.IV_pet.setVisibility(View.VISIBLE);
//                    break;
//                case "non_smoking":
//                    holder.IV_smoke.setVisibility(View.VISIBLE);
//            }
//        }
        if(favoriteSet.contains(personalPostModelList.get(position).getPostID())){
            holder.favButton.setChecked(true);
        }

    }


    @Override
    public int getItemCount() { return personalPostModelList.size(); }
    // Viewholder class
    class PersonalPostViewHolder extends RecyclerView.ViewHolder{
        //initializing
        ImageView userPic;
        TextView userName, userBudget, datePosted, userDescription;
        RelativeLayout relativeLayout;
        ImageButton messageButton, deletPostButton;
        ImageView maleIC, femaleIC, petIC, smokeIC;
        CheckBox favButton;

        public PersonalPostViewHolder(@NonNull View itemView) {
            super(itemView);
            userPic = itemView.findViewById(R.id.user_image_personal_card);
            userName = itemView.findViewById(R.id.user_name_personal_card);
            userBudget = itemView.findViewById(R.id.personal_post_budget_text_view);
            datePosted = itemView.findViewById(R.id.personal_post_date_text_view);
            userDescription = itemView.findViewById(R.id.personal_card_text_view);
            relativeLayout = itemView.findViewById(R.id.relative_layout_personal_card);
            maleIC = itemView.findViewById(R.id.male_image_view_apartment);
            femaleIC = itemView.findViewById(R.id.female_image_view_apartment);
            petIC = itemView.findViewById(R.id.pets_allowed_image_view_apartment);
            smokeIC = itemView.findViewById(R.id.non_smoking_image_view_apartment);
            messageButton = itemView.findViewById(R.id.start_chat_button);
            favButton = itemView.findViewById(R.id.favorite_check_button);
            deletPostButton = itemView.findViewById(R.id.personal_post_delete_button);
            if (isFromUserProfile){
                deletPostButton.setVisibility(View.VISIBLE);
                messageButton.setVisibility(View.GONE);
            }
            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(personalPostModelList.get(getAdapterPosition()).getUserID());

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            receiverUser = new User();
//                            receiverUser = snapshot.getValue(User.class);
//                            Log.d("user" , snapshot.toString());
//                            Toast.makeText(context ,snapshot.toString() ,Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, ChattingActivity.class);
                            intent.putExtra("USERID", snapshot.getKey());
                            context.startActivity(intent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
            deletPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deletPersonalPost();
                }
            });
            favButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences prefs = context.getSharedPreferences(userId, Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = prefs.edit();
                    if(isChecked) {
                        favoriteSet.add(personalPostModelList.get(getAdapterPosition()).getPostID());
                        edit.putStringSet(PERSONAL_FAVOURITES, favoriteSet);
                        edit.commit();
                    }else{
                        favoriteSet.remove(personalPostModelList.get(getAdapterPosition()).getPostID());
                        edit.putStringSet(PERSONAL_FAVOURITES, favoriteSet);
                        edit.commit();
                        if(isFromfav){
                            personalPostModelList.remove(getAdapterPosition());
                            notifyItemRemoved(getAdapterPosition());
                        }
                    }

                }
            });
}
        private void deletPersonalPost() {
            FirebaseFirestore
                    .getInstance()
                    .collection("postPersonal")
                    .document(personalPostModelList.get(getAdapterPosition()).getId()).
                    delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            personalPostModelList.remove(getAdapterPosition());
                            notifyItemRemoved(getAdapterPosition());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //TODO display a dialog
                }
            });

        }
    }
}
