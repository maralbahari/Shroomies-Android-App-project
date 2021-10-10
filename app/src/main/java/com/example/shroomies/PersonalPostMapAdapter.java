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

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PersonalPostMapAdapter extends RecyclerView.Adapter<PersonalPostMapAdapter.PersonalPostViewHolder> {
    private final Context context;
    private final List<PersonalPostModel> personalPostModels;
    private final DatabaseReference rootRef;
    private Set<String> favouriteSet;
    private final FirebaseAuth mAuth;
    private final UserFavourites userFavourites;

    public PersonalPostMapAdapter(Context context, List<PersonalPostModel> personalPostModels) {
        this.context = context;
        this.personalPostModels = personalPostModels;
        rootRef = FirebaseDatabase.getInstance().getReference(Config.users);
        mAuth = FirebaseAuth.getInstance();
        userFavourites = UserFavourites.getInstance(context, mAuth.getCurrentUser().getUid());
        favouriteSet = userFavourites.getPersonalPostFavourites();

    }


    @NonNull
    @Override
    public PersonalPostMapAdapter.PersonalPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.personal_post_map_view, parent, false);
        return new PersonalPostMapAdapter.PersonalPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalPostMapAdapter.PersonalPostViewHolder holder, int position) {
        String userID = personalPostModels.get(position).getUserID();
        getLoadUserDetails(userID, holder);
        if (personalPostModels.get(position).getPrice() != 0) {
            holder.priceTextView.setText("RM" + personalPostModels.get(position).getPrice());
        }
        ArrayList<String> buildingTypes = personalPostModels.get(position).getBuildingTypes();
        if (buildingTypes != null) {
            for (String type :
                    buildingTypes) {
                switch (type) {
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
        if (favouriteSet != null) {
            holder.favouriteButton.setSelected(favouriteSet.contains(personalPostModels.get(position).getPostID()));
        }


    }


    @Override
    public int getItemCount() {
        return personalPostModels.size();
    }

    public class PersonalPostViewHolder extends RecyclerView.ViewHolder {
        private final TextView userNameTextView, priceTextView;
        private final ImageView userImage;
        private final ImageButton favouriteButton;
        private final LottieAnimationView userNameSkeleton, imageSkeleton;
        private final Chip apartmentChip, flatChip, condoChip, townHouseChip;

        public PersonalPostViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.user_name_text_view);
            userImage = itemView.findViewById(R.id.user_image);
            priceTextView = itemView.findViewById(R.id.price_text_view);
            userNameSkeleton = itemView.findViewById(R.id.skeleton_loader_user_name);
            imageSkeleton = itemView.findViewById(R.id.skeleton_loader_image);
            apartmentChip = itemView.findViewById(R.id.apartment_chip);
            condoChip = itemView.findViewById(R.id.condo_chip);
            townHouseChip = itemView.findViewById(R.id.house_chip);
            flatChip = itemView.findViewById(R.id.flat_chip);
            favouriteButton = itemView.findViewById(R.id.favorite_image_button_personal_post);
            favouriteButton.setOnClickListener(v -> {
                if (!favouriteButton.isSelected()) {
                    addToFavourites(favouriteButton, getAdapterPosition());
                } else {
                    removeFromFavourites(favouriteButton, getAdapterPosition());
                }
            });
        }

    }

    private void addToFavourites(ImageButton favouriteButton, int adapterPosition) {
        if (adapterPosition != -1) {
            favouriteButton.setSelected(true);
            userFavourites.addPersonalPostToFavourites(personalPostModels
                    .get(adapterPosition)
                    .getPostID());
        }
    }

    private void removeFromFavourites(ImageButton favouriteButton, int adapterPosition) {
        if (adapterPosition != -1) {
            favouriteButton.setSelected(false);
            userFavourites.removePersonalPostFavourites(personalPostModels
                    .get(adapterPosition)
                    .getPostID());
        }
    }

    private void getLoadUserDetails(String userID, PersonalPostViewHolder holder) {
        rootRef.child(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    String imageUrl = user.getImage();
                    String userName = user.getUsername();
                    if (userName != null) {
                        holder.userNameTextView.setText(userName);
                        holder.userNameSkeleton.setVisibility(View.GONE);
                    }
                    holder.imageSkeleton.setVisibility(View.GONE);
                    holder.imageSkeleton.pauseAnimation();
                    if (imageUrl != null) {
                        GlideApp.with(context)
                                .load(user.getImage())
                                .transform(new CircleCrop())
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .error(R.drawable.ic_user_profile_svgrepo_com)
                                .into(holder.userImage);
                    }
                }
            }
        });

    }
}
