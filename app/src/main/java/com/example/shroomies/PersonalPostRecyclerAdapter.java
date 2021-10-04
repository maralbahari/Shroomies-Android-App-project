package com.example.shroomies;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PersonalPostRecyclerAdapter extends RecyclerView.Adapter<PersonalPostRecyclerAdapter.PersonalPostViewHolder> {
    private final List <PersonalPostModel> personalPostModelList;
    private final Context context;
    private DatabaseReference rootRef;
    private static Set<String> favoriteSet;
    private NotifyEmptyPersonalPostAdapter notifyEmptyPersonalPostAdapter;
    private final boolean isFromFavourite;
    private final UserFavourites userFavourites;

    public PersonalPostRecyclerAdapter(List<PersonalPostModel> personalPostModelList, Context context, Boolean isFromFavourite,boolean isFromUserProfile ) {
        this.personalPostModelList = personalPostModelList;
        this.context = context;
        this.isFromFavourite=isFromFavourite;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userFavourites = UserFavourites.getInstance(context , mAuth.getCurrentUser().getUid());
        favoriteSet = userFavourites.getPersonalPostFavourites();
        setHasStableIds(true);

    }

    public PersonalPostRecyclerAdapter(List<PersonalPostModel> personalPostModelList, Context context, Boolean isFromfav,boolean isFromUserProfile  , NotifyEmptyPersonalPostAdapter notifyEmptyPersonalPostAdapter){
        this(personalPostModelList , context , isFromfav , isFromUserProfile);
        this.notifyEmptyPersonalPostAdapter =  notifyEmptyPersonalPostAdapter;
    }

    @NonNull
    @Override
    public PersonalPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view  = layoutInflater.inflate(R.layout.personal_post_custom_card,parent,false);
        rootRef=FirebaseDatabase.getInstance().getReference();

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
        if(personalPostModelList.get(position).getPrice()!=0){
            holder.userBudget.setText("RM"+personalPostModelList.get(position).getPrice());
        }

        if(personalPostModelList.get(position).getTimeStamp()!=null){
            Timestamp timestamp = personalPostModelList.get(position).getTimeStamp();
            Date formattedDate= timestamp.toDate();
            SimpleDateFormat sfd = new SimpleDateFormat("dd MMM" , Locale.getDefault());
            holder.datePosted.setText(sfd.format(formattedDate));
        }

        String userID = personalPostModelList.get(position).getUserID();
        // getting data from user id
        getUser(userID ,holder);

        ArrayList<String> buildingTypes = personalPostModelList.get(position).getBuildingTypes();
        if(buildingTypes!=null){
            for (String type:
                    buildingTypes){
                switch (type){
                    case Config.TYPE_APARTMENT:
                        holder.apartmentChip.setVisibility(View.VISIBLE);
                        break;
                    case Config.TYPE_CONDO:
                        holder.condoChip.setVisibility(View.VISIBLE);
                        break;
                    case Config.TYPE_FLAT:
                        holder.flatChip.setVisibility(View.VISIBLE);
                        break;
                    case Config.TYPE_HOUSE:
                        holder.townHouseChip.setVisibility(View.VISIBLE);
                }
            }
        }
        int prefCounter=0;
        String prefs = personalPostModelList.get(position).getPreferences();
        if(prefs.charAt(0)=='1'){
            holder.maleIC.setVisibility(View.VISIBLE);
            prefCounter++;
        }else{
            holder.maleIC.setVisibility(View.GONE);
        }
        if(prefs.charAt(1)=='1'){
            holder.femaleIC.setVisibility(View.VISIBLE);
            prefCounter++;
        }else{
            holder.femaleIC.setVisibility(View.GONE);

        }
        if(prefs.charAt(2)=='1'){
            holder.petIC.setVisibility(View.VISIBLE);
            prefCounter++;
        }else{
            holder.petIC.setVisibility(View.GONE);
        }
        if(prefs.charAt(3)=='1'){
            holder.smokeIC.setVisibility(View.VISIBLE);
            prefCounter++;
        }else{
            holder.smokeIC.setVisibility(View.GONE);
        }
        if(prefs.charAt(4)=='1'){
            holder.alcoholIC.setVisibility(View.VISIBLE);
            prefCounter++;

        }else{
            holder.alcoholIC.setVisibility(View.GONE);
        }
        if(prefCounter==0){
            holder.preferencesLinearLayout.setVisibility(View.GONE);
        }
        if(favoriteSet!=null){
            holder.favouriteButton.setSelected(favoriteSet.contains(personalPostModelList.get(position).getPostID()));
        }

    }

    @Override
    public long getItemId(int position) {
        PersonalPostModel personalPostModel = personalPostModelList.get(position);
        // Lets return in real stable id from here
        //getting the hash code will make every id unique
        return (personalPostModel.getPostID()).hashCode();
    }



    private void getUser(String id , PersonalPostRecyclerAdapter.PersonalPostViewHolder holder) {
        rootRef.child(Config.users).child(id).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                User user = task.getResult().getValue(User.class);
                if (user!=null) {
                    holder.userName.setText(user.getUsername());
                    if (user.getImage()!=null){
                        Glide.with(context).
                                load(user.getImage())
                                .transform(new CircleCrop())
                                .into(holder.userPic);
                    }else{
                        holder.userPic.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_user_profile_svgrepo_com));
                    }
                }
            }else{
                //todo handle error
            }
        });
    }
    private void addToFavourites(ImageButton favouriteButton, int adapterPosition) {
        if(adapterPosition!=-1){
            favouriteButton.setSelected(true);
            userFavourites.addPersonalPostToFavourites(personalPostModelList
                            .get(adapterPosition)
                            .getPostID());
        }

    }

    void removeFromFavourites(ImageButton favouriteButton, int adapterPosition) {
        if(adapterPosition!=-1){
            favouriteButton.setSelected(false);
            userFavourites.removePersonalPostFavourites(personalPostModelList
                            .get(adapterPosition)
                            .getPostID());
            if(isFromFavourite){
                personalPostModelList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                if (personalPostModelList.isEmpty()){
                    notifyEmptyPersonalPostAdapter.onEmptyPersonalPostAdapter();
                }
            }
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
        ImageButton deletePostButton, favouriteButton;
        ImageView maleIC, femaleIC, petIC, smokeIC , alcoholIC;
        Chip apartmentChip,flatChip,condoChip,townHouseChip;
        LinearLayout preferencesLinearLayout;

        public PersonalPostViewHolder(@NonNull View itemView) {
            super(itemView);
            userPic = itemView.findViewById(R.id.user_image_personal_card);
            userName = itemView.findViewById(R.id.user_name_personal_card);
            userBudget = itemView.findViewById(R.id.personal_post_budget_text_view);
            datePosted = itemView.findViewById(R.id.personal_post_date_text_view);
            userDescription = itemView.findViewById(R.id.personal_card_text_view);
            relativeLayout = itemView.findViewById(R.id.relative_layout_personal_card);
            maleIC = itemView.findViewById(R.id.male_image_view_personal);
            femaleIC = itemView.findViewById(R.id.female_image_view_personal);
            petIC = itemView.findViewById(R.id.pets_allowed_image_view_personal);
            smokeIC = itemView.findViewById(R.id.non_smoking_image_view_personal);
            alcoholIC = itemView.findViewById(R.id.alcohol_image_view_personal);
            preferencesLinearLayout = itemView.findViewById(R.id.properties_linear_layout);

            favouriteButton = itemView.findViewById(R.id.favorite_check_button);
            deletePostButton = itemView.findViewById(R.id.personal_post_delete_button);
            apartmentChip = itemView.findViewById(R.id.apartment_chip);
            condoChip = itemView.findViewById(R.id.condo_chip);
            townHouseChip = itemView.findViewById(R.id.house_chip);
            flatChip = itemView.findViewById(R.id.flat_chip);


            favouriteButton.setOnClickListener(v -> {
                if(!favouriteButton.isSelected()){
                    addToFavourites(favouriteButton,getAdapterPosition());
                }else{
                    removeFromFavourites(favouriteButton , getAdapterPosition());
                }
            });

//            if (isFromUserProfile){
//                deletPostButton.setVisibility(View.VISIBLE);
//                messageButton.setVisibility(View.GONE);
//            }
//            messageButton.setOnClickListener(v -> {
//                String postOwnerID=personalPostModelList.get(getAdapterPosition()).getUserID();
//                Intent intent = new Intent(context, ChattingActivity.class);
//                intent.putExtra("USERID", postOwnerID);
//                context.startActivity(intent);
//            });
//            deletPostButton.setOnClickListener(v -> deletPersonalPost());
//            favButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                SharedPreferences prefs = context.getSharedPreferences(userId, Context.MODE_PRIVATE);
//                SharedPreferences.Editor edit = prefs.edit();
//                if(isChecked) {
//                    favoriteSet.add(personalPostModelList.get(getAdapterPosition()).getPostID());
//                    edit.putStringSet(PERSONAL_FAVOURITES, favoriteSet);
//                    edit.apply();
//                }else{
//                    favoriteSet.remove(personalPostModelList.get(getAdapterPosition()).getPostID());
//                    edit.putStringSet(PERSONAL_FAVOURITES, favoriteSet);
//                    edit.commit();
//                    if(isFromfav){
//                        personalPostModelList.remove(getAdapterPosition());
//                        notifyItemRemoved(getAdapterPosition());
//                    }
//                }
//            });
        }




//        private void deletPersonalPost() {
//            FirebaseFirestore firestore=FirebaseFirestore.getInstance();
//            firestore.collection("postPersonal")
//                .document(personalPostModelList.get(getAdapterPosition()).getId()).
//                delete()
//                .addOnSuccessListener(aVoid -> {
//                    personalPostModelList.remove(getAdapterPosition());
//                    notifyItemRemoved(getAdapterPosition());
//                }).addOnFailureListener(e -> {
//                    //TODO display a dialog
//                    Toast.makeText(context,"Something went wrong!",Toast.LENGTH_SHORT).show();
//                });
//
//        }
    }


}

interface NotifyEmptyPersonalPostAdapter{
    void onEmptyPersonalPostAdapter();
}

