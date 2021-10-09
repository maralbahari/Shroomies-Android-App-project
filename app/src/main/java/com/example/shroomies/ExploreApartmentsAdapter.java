package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExploreApartmentsAdapter extends RecyclerView.Adapter<ExploreApartmentsAdapter.ViewHolder> {
    private static final String APARTMENT_FAVOURITES = "APARTMENT_FAVOURITES";
    private final List<Apartment> apartmentList;
    private final Context context;
    private final String userId;
    private Set<String> favoriteSet;


    public ExploreApartmentsAdapter(Context context, List<Apartment> apartmentList, String userId) {

        this.apartmentList = apartmentList;
        this.context = context;
        this.userId = userId;
        setHasStableIds(true);
        favoriteSet = context.getSharedPreferences(userId, Context.MODE_PRIVATE).getStringSet(APARTMENT_FAVOURITES, null);
        if(favoriteSet==null){
            favoriteSet = new HashSet<>();
        }
    }

    @NonNull
    @Override
    public ExploreApartmentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        holder.priceTV.setText(apartmentList.get(position).getPrice() + " RM");

        String buildingType = apartmentList.get(position).getBuildingType();
        if (buildingType != null) {
            if (buildingType.equals(Config.TYPE_HOUSE)) {
                String locality = apartmentList.get(position).getLocality();
                String subLocality = apartmentList.get(position).getSubLocality();
                if (locality != null && subLocality != null) {
                    holder.addressTV.setText(subLocality + ", " + locality);
                } else {

                }
            } else {
                String buildingName = apartmentList.get(position).getBuildingName();
                if (buildingName != null) {
                    holder.addressTV.setText(buildingName);
                } else {

                }
            }
        }
        int prefCounter = 0;
        String prefs = apartmentList.get(position).getPreferences();
//        if(prefs.charAt(0)=='1'){
//            holder.maleButton.setVisibility(View.VISIBLE);
//            prefCounter++;
//        }
//        if(prefs.charAt(1)=='1'){
//            holder.femaleButton.setVisibility(View.VISIBLE);
//            prefCounter++;
//        }
//        if(prefs.charAt(2)=='1'){
//            holder.petsAllowedButton.setVisibility(View.VISIBLE);
//            prefCounter++;
//        }
//        if(prefs.charAt(3)=='1'){
//            holder.smokeFreeButton.setVisibility(View.VISIBLE);
//            prefCounter++;
//        }
//        if(prefCounter==0){
//            holder.prefereanceLinearLayout.setVisibility(View.GONE);
//        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(apartmentList.get(position).getImageUrl().get(0));
//         Load the image using Glide
        GlideApp.with(context)
                .load(storageReference)
                .transform(new RoundedCorners(5))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.apartmentImage);


//        if(apartmentList.get(position).isFemale()){
//            holder.femaleButton.setVisibility(View.VISIBLE);
//            prefCounter++;
//        }
//        if(apartmentList.get(position).isMale()){
//            holder.maleButton.setVisibility(View.VISIBLE);
//            prefCounter++;
//        }
//        if(apartmentList.get(position).isNonSmoking()){
//            holder.smokeFreeButton.setVisibility(View.VISIBLE);
//            prefCounter++;
//        }
//        if(apartmentList.get(position).isPet()){
//            holder.petsAllowedButton.setVisibility(View.VISIBLE);
//            prefCounter++;
//        }

        if (prefCounter == 0) {
//            holder..setVisibility(View.GONE);
        }

        if (favoriteSet != null) {
            if (favoriteSet.contains(apartmentList.get(position).getApartmentID())) {
                holder.favoriteCheckBox.setChecked(true);
            }
        }


    }




    @Override
    public int getItemCount() {
        return apartmentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView priceTV, addressTV;
        ImageView apartmentImage;
        ImageView maleButton, femaleButton, petsAllowedButton, smokeFreeButton;
        CheckBox favoriteCheckBox;
        LinearLayout prefLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            priceTV = itemView.findViewById(R.id.price_text_view_apartment_card);

            addressTV = itemView.findViewById(R.id.city_text_view);
            apartmentImage = itemView.findViewById(R.id.apartment_image);

            maleButton = itemView.findViewById(R.id.male_image_view_personal);
            femaleButton = itemView.findViewById(R.id.female_image_view_personal);
            petsAllowedButton = itemView.findViewById(R.id.pets_allowd_image_view_apartment);
            smokeFreeButton = itemView.findViewById(R.id.non_smoking_image_view_personal);
            favoriteCheckBox = itemView.findViewById(R.id.favorite_check_box);
            apartmentImage.setOnClickListener(v -> goToApartmentViewPage(getAdapterPosition()));
            favoriteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences prefs = context.getSharedPreferences(userId, Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();
                if (isChecked) {
                    favoriteSet.add(apartmentList.get(getAdapterPosition()).getApartmentID());
                } else {
                    favoriteSet.remove(apartmentList.get(getAdapterPosition()).getApartmentID());
                }
                edit.putStringSet(APARTMENT_FAVOURITES, favoriteSet);
                edit.apply();

            });
        }


    }


//    private String getApartmentLocality(int position) {
//        try{
//            Geocoder geocoder = new Geocoder(context);
//            LatLng latLng = new LatLng(apartmentList.get(position).getLatitude(), apartmentList.get(position).getLongitude());
//            return geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0).getLocality();
//        }catch (Exception e ){
//            return "unknown";
//        }
//
//
//    }

    private void goToApartmentViewPage(int position) {
        Intent intent = new Intent(context, ApartmentViewPage.class);
        // add the parcelable apartment object to the intent and use it's values to
        //update the apartment view class
        intent.putExtra("apartment", apartmentList.get(position));

//        intent.putExtra("POST_DATE"  , apartmentList.get(position).getTime_stamp().toDate().toInstant().atZone(currentZoneID).format(myFormatObj));
        context.startActivity(intent);
    }



}
