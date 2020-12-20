package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RecycleViewAdapterApartments extends RecyclerView.Adapter<RecycleViewAdapterApartments.ViewHolder> {

    private List<Apartment> apartmentList;
    private Context context;
    private Geocoder geocoder;

    public RecycleViewAdapterApartments(List<Apartment> apartmentList, Context context){
        this.apartmentList = apartmentList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecycleViewAdapterApartments.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view  = layoutInflater.inflate(R.layout.apartment_card,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        //initialize geocoeder to get the location from the latlng
        geocoder = new Geocoder(context);

        holder.priceTV.setText(Integer.toString(apartmentList.get(position).getPrice()));
        LatLngCustom latLng = apartmentList.get(position).getLocationLatLng();
        // get the location from the latlng
        try {
            holder.addressTV.setText(geocoder.getFromLocation(latLng.getLatitude(),latLng.getLongitude(),1).get(0).getLocality());
        } catch (IOException e) {
            e.printStackTrace();
        }


        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(apartmentList.get(position).getImage_url().get(0));

        // Load the image using Glide
                GlideApp.with(this.context)
                        .load(storageReference)
                        .into(holder.apartmentImage);
        // on click go to the apartment view
        holder.cradView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ApartmentViewPage.class);
                // add the parcelable apartment object to the intent and use it's values to
                //update the apartment view class
                intent.putExtra("apartment",apartmentList.get(position));
                context.startActivity(intent);
            }
        });


    }





    @Override
    public int getItemCount() {
        return apartmentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView priceTV ;
        TextView numRoomMateTV;
        TextView addressTV;
        ImageView apartmentImage;
        CardView cradView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            priceTV = itemView.findViewById(R.id.TV_price);
            numRoomMateTV = itemView.findViewById(R.id.Room_mate_num);
            addressTV = itemView.findViewById(R.id.TV_address);
            apartmentImage = itemView.findViewById(R.id.Iv_RoomImg);
            cradView = itemView.findViewById(R.id.apartment_card_view);

        }

    }
}
