package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MapApartmentDialogFragment extends DialogFragment {
    private View v;
    private Apartment apartment;
    TextView priceTextView , descriptionTextView;
    CheckBox favoriteCheckBox;
    ImageView apartmentImage;
    RelativeLayout apartmentMapLayout;


    MapApartmentDialogFragment(Apartment apartment){
        this.apartment= apartment;
    }


    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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
        v= inflater.inflate(R.layout.fragment_map_apartment_view, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        descriptionTextView = v.findViewById(R.id.description_text_view_map_view);
//        priceTextView = v.findViewById(R.id.price_text_view_map_view);
//        apartmentImage = v.findViewById(R.id.apartment_image_map_view);
        apartmentMapLayout = v.findViewById(R.id.apartment_layout_map);
        if(apartment!=null){
            if(apartment.getDescription()!=null){
                descriptionTextView.setText(apartment.getDescription());
            }
            //load the first image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(apartment.getImageUrl().get(0));
            GlideApp.with(getActivity())
                    .load(storageReference)
                    .centerInside()
                    .transform(new CenterCrop(), new GranularRoundedCorners(20, 0, 0, 20))
                    .into(apartmentImage);
        }

        apartmentMapLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Intent intent = new Intent(getActivity(), ApartmentViewPage.class);
                    intent.putExtra("apartment", apartment);
                    startActivity(intent);
            }
        });



    }
}
