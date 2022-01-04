package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Set;

public class MapSearchAdapter extends RecyclerView.Adapter<MapSearchAdapter.MapSearchViewHolder> {
    private final Context context;
    private final List<Apartment> apartmentList;
    private FirebaseAuth mAuth;
    private final Set<String> favouriteSet;
    private final UserFavourites userFavourites;

    public MapSearchAdapter(Context context, List<Apartment> apartmentList) {
        this.context = context;
        this.apartmentList = apartmentList;
        mAuth = FirebaseAuth.getInstance();
        userFavourites = UserFavourites.getInstance(context, mAuth.getCurrentUser().getUid());
        favouriteSet = userFavourites.getApartmentPostFavourites();


    }

    @NonNull
    @Override
    public MapSearchAdapter.MapSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.map_search_card, parent, false);

        return new MapSearchViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MapSearchAdapter.MapSearchViewHolder holder, int position) {
        if (apartmentList.get(position).getImageUrl() != null) {
            StorageReference storageReference =
                    FirebaseStorage.getInstance()
                            .getReference()
                            .child(apartmentList.get(position)
                                    .getImageUrl()
                                    .get(0));
            GlideApp.with(this.context)
                    .load(storageReference)
                    .transform(new CenterCrop(), new RoundedCorners(10))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.apartmentImageView);
        }
        String buildingType = apartmentList.get(position).getBuildingType();

        if (buildingType.equals(Config.TYPE_HOUSE)) {
            String locality = apartmentList.get(position).getLocality();
            String subLocality = apartmentList.get(position).getSubLocality();
            if (locality != null && subLocality != null) {
                holder.locationTextView.setText(subLocality + ", " + locality);
            } else {

            }
        } else {
            String buildingName = apartmentList.get(position).getBuildingName();
            if (buildingName != null) {
                holder.locationTextView.setText(buildingName);
            } else {

            }
        }

        int price = apartmentList.get(position).getPrice();
        int numRoomMates = +apartmentList.get(position).getNumberOfRoommates();
        if (price > 0) {
            holder.priceTextView.setText(price + " RM");
            holder.numRoomMateTV.setText(" • " + numRoomMates + " roommates");
        } else {
            holder.numRoomMateTV.setText(numRoomMates + " roommates");
        }

        if (favouriteSet != null) {
            holder.favouriteButton.setSelected(favouriteSet.contains(apartmentList.get(position).getApartmentID()));
        }


    }

    @Override
    public int getItemCount() {
        return apartmentList.size();
    }

    public class MapSearchViewHolder extends RecyclerView.ViewHolder {
        private final TextView locationTextView, priceTextView, numRoomMateTV;
        private final ImageView apartmentImageView;
        private final ImageButton favouriteButton;
        private final RelativeLayout holderRelativeLayout;

        public MapSearchViewHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.apartment_location);
            priceTextView = itemView.findViewById(R.id.apartment_price);
            apartmentImageView = itemView.findViewById(R.id.apartmnent_image);
            numRoomMateTV = itemView.findViewById(R.id.apartment_roommates);
            favouriteButton = itemView.findViewById(R.id.favorite_image_button_apartment);
            holderRelativeLayout = itemView.findViewById(R.id.relative_layout);

            holderRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToApartmentViewPage(getAdapterPosition());
                }
            });


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
            userFavourites.addApartmentPostToFavourites(apartmentList
                    .get(adapterPosition)
                    .getApartmentID());
        }
    }

    private void removeFromFavourites(ImageButton favouriteButton, int adapterPosition) {
        if (adapterPosition != -1) {
            favouriteButton.setSelected(false);
            userFavourites.removeApartmentPostFavourites(apartmentList
                    .get(adapterPosition)
                    .getApartmentID());
        }
    }

    private void goToApartmentViewPage(int position) {
        Intent intent = new Intent(context, ApartmentViewPage.class);
        // add the parcelable apartment object to the intent and use it's values to
        //update the apartment view class
        intent.putExtra(Config.apartment, apartmentList.get(position));
        context.startActivity(intent);
    }


}
