package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.DateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExploreApartmentsAdapter extends RecyclerView.Adapter<ExploreApartmentsAdapter.ViewHolder> {
    private static final String APARTMENT_FAVOURITES = "APARTMENT_FAVOURITES";
    private List<Apartment> apartmentList;
    private Context context;
    private Geocoder geocoder;
    private DatabaseReference rootRef;
    private String userId;
    private Set favoriteSet;



    public ExploreApartmentsAdapter(Context context, List<Apartment> apartmentList ,  String userId ){

        this.apartmentList = apartmentList;
        this.context = context;
        this.userId = userId;
        setHasStableIds(true);
        favoriteSet = context.getSharedPreferences(userId , Context.MODE_PRIVATE).getStringSet(APARTMENT_FAVOURITES, null);
        if(favoriteSet==null){
            favoriteSet = new HashSet<>();
        }
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
        List<String> preferences = apartmentList.get(position).getPreferences();
        for (String preference
                :preferences) {
            switch (preference) {
                case "male":
                    holder.maleButton.setVisibility(View.VISIBLE);break;
                case "female":
                    holder.femaleButton.setVisibility(View.VISIBLE);break;
                case "pet":
                    holder.petsAllowedButton.setVisibility(View.VISIBLE);break;
                case "non_smoking":
                    holder.smokeFreeButton.setVisibility(View.VISIBLE);
            }
        }
        if(favoriteSet!=null){
            if(favoriteSet.contains(apartmentList.get(position).getApartmentID())) {
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
            favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()

            {
                @Override
                public void onCheckedChanged (CompoundButton buttonView,boolean isChecked){
                    SharedPreferences prefs = context.getSharedPreferences(userId, Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = prefs.edit();
                    if (isChecked) {
                        favoriteSet.add(apartmentList.get(getAdapterPosition()).getApartmentID());
                        edit.putStringSet(APARTMENT_FAVOURITES, favoriteSet);
                        edit.commit();
                    } else {
                        favoriteSet.remove(apartmentList.get(getAdapterPosition()).getApartmentID());
                        edit.putStringSet(APARTMENT_FAVOURITES, favoriteSet);
                        edit.commit();
                    }

                }
            });
        }



    }


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

        ZoneId currentZoneID = ZonedDateTime.now(ZoneId.systemDefault()).getZone();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        intent.putExtra("POST_DATE"  , apartmentList.get(position).getTime_stamp().toDate().toInstant().atZone(currentZoneID).format(myFormatObj));
        context.startActivity(intent);
    }



}
