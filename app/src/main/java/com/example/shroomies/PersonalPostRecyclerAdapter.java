package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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


    public PersonalPostRecyclerAdapter(List<PersonalPostModel> personalPostModelList, Context context,String userId, Boolean isFromfav) {
        this.personalPostModelList = personalPostModelList;
        this.context = context;
        this.isFromfav = isFromfav;
        this.userId = userId;
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
            holder.TV_userDescription.setText("no description added");
        }else{
            holder.TV_userDescription.setText(description);
        }
//        holder.TV_DatePosted.setText(personalPostModelList.get(position).getDate().split(" ")[0]);
        holder.TV_userBudget.setText("Budget: " + personalPostModelList.get(position).getPrice());
        String id = personalPostModelList.get(position).getUserID();
        // getting data from user id
         myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    holder.TV_userName.setText(user.getName());
//                    holder.TV_userOccupation.setText(user.getBio());
                    if (user.getImage()!=null){
                    Glide.with(context).
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
            holder.BT_fav.setChecked(true);
        }

    }


    @Override
    public int getItemCount() { return personalPostModelList.size(); }
    // Viewholder class
    class PersonalPostViewHolder extends RecyclerView.ViewHolder{
        //initializing
        ImageView IV_userPic;
        TextView TV_userName,TV_userBudget, TV_DatePosted,TV_userDescription;
        RelativeLayout Lay_card;
        ImageButton  BT_message , deletPostButton;
        ImageView IV_male, IV_female, IV_pet, IV_smoke;
        CheckBox BT_fav;

        public PersonalPostViewHolder(@NonNull View itemView) {
            super(itemView);

            IV_userPic = itemView.findViewById(R.id.user_image_personal_card);
            TV_userName = itemView.findViewById(R.id.user_name_personal_card);

            TV_userBudget = itemView.findViewById(R.id.personal_post_budget_text_view);
            TV_DatePosted = itemView.findViewById(R.id.personal_post_date_text_view);
            TV_userDescription = itemView.findViewById(R.id.personal_card_text_view);
            Lay_card = itemView.findViewById(R.id.relative_layout_personal_card);
            IV_male = itemView.findViewById(R.id.male_image_view_apartment);
            IV_female = itemView.findViewById(R.id.female_image_view_apartment);
            IV_pet = itemView.findViewById(R.id.pets_allowed_image_view_apartment);
            IV_smoke = itemView.findViewById(R.id.non_smoking_image_view_apartment);
            BT_message = itemView.findViewById(R.id.start_chat_button);
            BT_fav = itemView.findViewById(R.id.favorite_check_button);
            deletPostButton = itemView.findViewById(R.id.personal_post_delete_button);

            BT_message.setOnClickListener(new View.OnClickListener() {
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
            BT_fav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
