package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;

public class RecycleViewAdapterApartments extends RecyclerView.Adapter<RecycleViewAdapterApartments.ViewHolder> {

    private List<Apartment> apartmentList;
    private Context context;
    private Geocoder geocoder;
    private User receiverUser;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    public RecycleViewAdapterApartments(List<Apartment> apartmentList, Context context){
        this.apartmentList = apartmentList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecycleViewAdapterApartments.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view  = layoutInflater.inflate(R.layout.apartment_card,parent,false);
        rootRef=FirebaseDatabase.getInstance().getReference();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        //initialize geocoeder to get the location from the latlng
        geocoder = new Geocoder(context);


        // set the price of the apartment
        holder.priceTV.setText(Integer.toString(apartmentList.get(position).getPrice()));
        //set the number of roommates
        holder.numRoomMateTV.setText(Integer.toString(apartmentList.get(position).getNumberOfRoommates()));
        LatLng latLng = new LatLng(apartmentList.get(position).getLatitude(), apartmentList.get(position).getLongitude());
        // get the location from the latlng]
        try {
            holder.addressTV.setText(geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0).getLocality());
        } catch (IOException e) {
            e.printStackTrace();
        }


        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(apartmentList.get(position).getImage_url().get(0));

        // Load the image using Glide
        GlideApp.with(this.context)
                .load(storageReference)
                .fitCenter()
                .centerCrop()
                .into(holder.apartmentImage);
        // on click go to the apartment view
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ApartmentViewPage.class);
                // add the parcelable apartment object to the intent and use it's values to
                //update the apartment view class
                intent.putExtra("apartment",apartmentList.get(position));
                context.startActivity(intent);
            }
        });
        List<Boolean> preferences = apartmentList.get(position).getPreferences();
        if(preferences.get(0)){holder.maleButton.setVisibility(View.VISIBLE);}
        if(preferences.get(1)){holder.femaleButton.setVisibility(View.VISIBLE);}
        if(preferences.get(2)){holder.petsAllowedButton.setVisibility(View.VISIBLE);}
        if(preferences.get(3)){holder.smokeFreeButton.setVisibility(View.VISIBLE);}




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
        CardView cardView;
        ImageView maleButton;
        ImageView femaleButton;
        ImageView petsAllowedButton;
        ImageView smokeFreeButton;
        Button sendMessageButton;
        ImageButton BUT_fav_apt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            priceTV = itemView.findViewById(R.id.TV_price);
            numRoomMateTV = itemView.findViewById(R.id.Room_mate_num);
            addressTV = itemView.findViewById(R.id.TV_address);
            apartmentImage = itemView.findViewById(R.id.Iv_RoomImg);
            cardView = itemView.findViewById(R.id.apartment_card_view);
            maleButton = itemView.findViewById(R.id.male_image_apartment_card);
            femaleButton = itemView.findViewById(R.id.female_image_apartment_card);
            petsAllowedButton = itemView.findViewById(R.id.pets_allowd_image_apartment_card);
            smokeFreeButton = itemView.findViewById(R.id.non_smoking_image_view_apartment_card);
            sendMessageButton=itemView.findViewById(R.id.start_chat_button_apartment_card);
            BUT_fav_apt = itemView.findViewById(R.id.BUT_fav_apt);

            sendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get user details and pass to chatting activity
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(apartmentList.get(getAdapterPosition()).getUserID());

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            receiverUser = new User();
                            receiverUser = snapshot.getValue(User.class);
                            Intent intent = new Intent(context, ChattingActivity.class);
                            intent.putExtra("USERID", receiverUser.getID());
                            context.startActivity(intent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        }
    }

}


