package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ExploreApartmentsAdapter extends RecyclerView.Adapter<ExploreApartmentsAdapter.ViewHolder> {

    private List<Apartment> apartmentList;
    private Context context;
    private Geocoder geocoder;
    private DatabaseReference rootRef;



    public ExploreApartmentsAdapter(Context context, List<Apartment> apartmentList){
        this.apartmentList = apartmentList;
        this.context = context;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ExploreApartmentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        rootRef=FirebaseDatabase.getInstance().getReference();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.explore_apartment_card , parent , false);

        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        Apartment apartment = apartmentList.get(position);
        // Lets return in real stable id from here
        //getting the hash code will make every id unique
        return (apartment.getApartmentID()).hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        // set the price of the apartment
        holder.priceTV.setText(Integer.toString(apartmentList.get(position).getPrice()));
        holder.addressTV.setText(getApartmentLocality(position));

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(apartmentList.get(position).getImage_url().get(0));
//         Load the image using Glide
        GlideApp.with(context)
                .load(storageReference)
                .transform(new RoundedCorners(20))
                .into(holder.apartmentImage);
        List<Boolean> preferences = apartmentList.get(position).getPreferences();
        if(preferences.get(0)){holder.maleButton.setVisibility(View.VISIBLE);}else{holder.maleButton.setVisibility(View.GONE);}
        if(preferences.get(1)){holder.femaleButton.setVisibility(View.VISIBLE);}else{holder.femaleButton.setVisibility(View.GONE);}
        if(preferences.get(2)){holder.petsAllowedButton.setVisibility(View.VISIBLE);}else{holder.petsAllowedButton.setVisibility(View.GONE);}
        if(preferences.get(3)){holder.smokeFreeButton.setVisibility(View.VISIBLE);}else{holder.smokeFreeButton.setVisibility(View.GONE);}

        String id = apartmentList.get(position).getUserID();
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            final String Uid = firebaseUser.getUid();

            if(id.equals(Uid)){
                holder.favoriteCheckBox.setVisibility(View.GONE);
            }
//            holder.favoriteCheckBox.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    addOrRemoveFromFavorites(position);
//                }
//            });

    }




    @Override
    public int getItemCount() {
        return apartmentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView priceTV, addressTV;
        ImageView apartmentImage;
        ImageView maleButton, femaleButton,petsAllowedButton, smokeFreeButton;
        CheckBox favoriteCheckBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            priceTV = itemView.findViewById(R.id.price_text_view_apartment_card);

            addressTV = itemView.findViewById(R.id.city_text_view);
            apartmentImage = itemView.findViewById(R.id.apartment_image);

            maleButton = itemView.findViewById(R.id.male_image_view_apartment);
            femaleButton = itemView.findViewById(R.id.female_image_view_apartment);
            petsAllowedButton = itemView.findViewById(R.id.pets_allowd_image_view_apartment);
            smokeFreeButton = itemView.findViewById(R.id.non_smoking_image_view_apartment);
            favoriteCheckBox = itemView.findViewById(R.id.favorite_check_box);
            apartmentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToApartmentViewPage(getAdapterPosition());
                }
            });
        }

    }

//    private void addOrRemoveFromFavorites(int position) {
//        //favorite
//
//       rootRef.child("Favorite").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (checkClick[0])
//                    if (snapshot.child(Uid).child("ApartmentPost").hasChild(apartmentList.get(position).getId())) {
//                        favRef.child(Uid).child("ApartmentPost").child(apartmentList.get(position).getId()).removeValue();
//                        holder.BUT_fav_apt.setImageResource(R.drawable.ic_icon_awesome_star_unchecked);
//                        Toast.makeText(context, "Post removed from favorites", Toast.LENGTH_LONG).show();
//                        checkClick[0] = false;
//                    } else {
//                        DatabaseReference anotherRef = favRef.child(Uid).child("ApartmentPost").child(apartmentList.get(position).getId());
//                        anotherRef.child("id").setValue(apartmentList.get(position).getId());
//                        anotherRef.child("userID").setValue(apartmentList.get(position).getUserID());
//                        anotherRef.child("image_url").setValue(apartmentList.get(position).getImage_url());
//                        anotherRef.child("price").setValue(apartmentList.get(position).getPrice());
//                        anotherRef.child("date").setValue(apartmentList.get(position).getDate());
//                        anotherRef.child("description").setValue(apartmentList.get(position).getDescription());
//                        anotherRef.child("preferences").setValue(apartmentList.get(position).getPreferences());
//                        anotherRef.child("latitude").setValue(apartmentList.get(position).getLatitude());
//                        anotherRef.child("longitude").setValue(apartmentList.get(position).getLongitude());
//                        anotherRef.child("numberOfRoommates").setValue(apartmentList.get(position).getNumberOfRoommates());
//                        Toast.makeText(context, "Post added to favorites", Toast.LENGTH_LONG).show();
//                        holder.BUT_fav_apt.setImageResource(R.drawable.ic_icon_awesome_star_checked);
//                        checkClick[0] = false;
//
//                    }
//
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


    private String getApartmentLocality(int position) {
        try{
            geocoder = new Geocoder(context);
            LatLng latLng = new LatLng(apartmentList.get(position).getLatitude(), apartmentList.get(position).getLongitude());
            return geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0).getLocality();
        }catch (Exception e ){
            return "unknown";
        }


    }

    private void goToApartmentViewPage(int position) {
        Intent intent = new Intent(context, ApartmentViewPage.class);
        // add the parcelable apartment object to the intent and use it's values to
        //update the apartment view class
        intent.putExtra("apartment",apartmentList.get(position));
        context.startActivity(intent);
    }



}
