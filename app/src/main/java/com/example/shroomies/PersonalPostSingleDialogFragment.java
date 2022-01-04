package com.example.shroomies;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Set;

public class PersonalPostSingleDialogFragment extends DialogFragment {
    private View v;
    private User user;
    private TextView userNameTextView;
    private ImageView userImageView;
    private ImageButton favouriteImageButton;
    private LottieAnimationView imageSkeleton, usernameSkeleton;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private UserFavourites userFavourites;
    private Set<String> favouriteSet;

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_map_personal_post_view, container, false);
        rootRef = FirebaseDatabase.getInstance().getReference(Config.users);
        mAuth = FirebaseAuth.getInstance();
        userFavourites = UserFavourites.getInstance(getContext(), mAuth.getCurrentUser().getUid());
        favouriteSet = userFavourites.getPersonalPostFavourites();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userNameTextView = v.findViewById(R.id.user_name_text_view);
        TextView priceTextView = v.findViewById(R.id.price_text_view);
        userImageView = v.findViewById(R.id.personal_post_image);
        Chip apartmentChip = v.findViewById(R.id.apartment_chip);
        Chip condoChip = v.findViewById(R.id.condo_chip);
        Chip townHouseChip = v.findViewById(R.id.house_chip);
        Chip flatChip = v.findViewById(R.id.flat_chip);
        imageSkeleton = v.findViewById(R.id.skeleton_loader_image);
        usernameSkeleton = v.findViewById(R.id.skeleton_loader_user_name);
        favouriteImageButton = v.findViewById(R.id.favorite_check_box_apartment);

        if (getArguments() != null) {
            PersonalPostModel personalPostModel = getArguments().getParcelable(Config.PERSONAL_POST);
            getLoadUserDetails(personalPostModel.getUserID());
            if (personalPostModel != null) {
                if (personalPostModel.getPrice() != 0) {
                    priceTextView.setText("RM" + personalPostModel.getPrice());
                    ArrayList<String> buildingTypes = personalPostModel.getBuildingTypes();
                    if (buildingTypes != null) {
                        for (String type :
                                buildingTypes) {
                            switch (type) {
                                case Config.TYPE_APARTMENT:
                                    apartmentChip.setVisibility(View.VISIBLE);
                                    break;
                                case Config.TYPE_CONDO:
                                    condoChip.setVisibility(View.VISIBLE);
                                    break;
                                case Config.TYPE_FLAT:
                                    flatChip.setVisibility(View.VISIBLE);
                                    break;
                                case Config.TYPE_HOUSE:
                                    townHouseChip.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
                if (favouriteSet != null) {
                    favouriteImageButton.setSelected(favouriteSet.contains(personalPostModel.getPostID()));
                }
                favouriteImageButton.setOnClickListener(v -> {
                    if (!favouriteImageButton.isSelected()) {
                        favouriteImageButton.setSelected(true);
                        userFavourites.addPersonalPostToFavourites(personalPostModel
                                .getPostID());
                    } else {
                        favouriteImageButton.setSelected(false);
                        userFavourites.removePersonalPostFavourites(personalPostModel
                                .getPostID());
                    }
                });
            }
        }

    }

    private void getLoadUserDetails(String userID) {
        rootRef.child(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user = task.getResult().getValue(User.class);
                if (user != null) {
                    String imageUrl = user.getImage();
                    String userName = user.getUsername();
                    if (userName != null) {
                        userNameTextView.setText(userName);
                        usernameSkeleton.setVisibility(View.GONE);
                    }
                    imageSkeleton.setVisibility(View.GONE);
                    imageSkeleton.pauseAnimation();
                    if (imageUrl != null) {
                        GlideApp.with(getContext())
                                .load(user.getImage())
                                .transform(new CircleCrop())
                                .error(R.drawable.ic_user_profile_svgrepo_com)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(userImageView);

                    }
                }
            }
        });

    }
}
