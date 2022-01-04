package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MapSingleDialogFragment extends DialogFragment {
    private View v;
    private Apartment apartment;
    TextView locationTextView, priceTextView, numberRoommatesTextView;
    //    CheckBox favoriteCheckBox;
    ImageView apartmentImage;
    RelativeLayout apartmentMapLayout;

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
        v = inflater.inflate(R.layout.fragment_map_apartment_view, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        priceTextView = v.findViewById(R.id.price_text_view);
        locationTextView = v.findViewById(R.id.user_name_text_view);
        numberRoommatesTextView = v.findViewById(R.id.number_of_roommates_text_view);
        apartmentImage = v.findViewById(R.id.personal_post_image);
        apartmentMapLayout = v.findViewById(R.id.apartment_layout_map);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            apartment = bundle.getParcelable(Config.apartment);
        }
        if (apartment != null) {
            if (apartment.getImageUrl() != null) {
                StorageReference storageReference =
                        FirebaseStorage.getInstance()
                                .getReference()
                                .child(apartment.getImageUrl()
                                        .get(0));
                GlideApp.with(getActivity())
                        .load(storageReference)
                        .transform(new CenterCrop(), new RoundedCorners(10))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(apartmentImage);
            }
            int price = apartment.getPrice();
            int numRoomMates = apartment.getNumberOfRoommates();
            if (price > 0) {
                priceTextView.setText(price + " RM");
                numberRoommatesTextView.setText(" • " + numRoomMates + " roommates");
            } else {
                numberRoommatesTextView.setText(numRoomMates + " roommates");
            }
            String buildingType = apartment.getBuildingType();

            if (buildingType.equals(Config.TYPE_HOUSE)) {
                String locality = apartment.getLocality();
                String subLocality = apartment.getSubLocality();
                if (locality != null && subLocality != null) {
                    locationTextView.setText(subLocality + ", " + locality);
                } else {

                }
            } else {
                String buildingName = apartment.getBuildingName();
                if (buildingName != null) {
                    locationTextView.setText(buildingName);
                } else {

                }
            }


        }


        apartmentMapLayout.setOnClickListener(v -> {
            dismiss();
            Intent intent = new Intent(getActivity(), ApartmentViewPage.class);
            intent.putExtra(Config.apartment, apartment);
            startActivity(intent);
        });


    }
}
