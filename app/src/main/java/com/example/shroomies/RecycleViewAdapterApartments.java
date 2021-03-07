package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.make.dots.dotsindicator.DotsIndicator;

import java.io.IOException;
import java.util.List;

public class RecycleViewAdapterApartments extends RecyclerView.Adapter<RecycleViewAdapterApartments.ViewHolder> {

    private List<Apartment> apartmentList;
    private Context context;
    private Geocoder geocoder;
    private User receiverUser;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    Boolean isFromAptFav;


    public RecycleViewAdapterApartments(List<Apartment> apartmentList, Context context, Boolean isFromAptFav){
        this.apartmentList = apartmentList;
        this.context = context;
        this.isFromAptFav = isFromAptFav;
        setHasStableIds(true);
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        //initialize geocoeder to get the location from the latlng
        geocoder = new Geocoder(context);

        //set the description
        holder.descriptionTV.setText(apartmentList.get(position).getDescription());
        // set the price of the apartment
        holder.priceTV.setText(apartmentList.get(position).getPrice()+" ");
        //set the number of roommates
        holder.numRoomMateTV.setText(" • "+apartmentList.get(position).getNumberOfRoommates() + " roommates");
        LatLng latLng = new LatLng(apartmentList.get(position).getLatitude(), apartmentList.get(position).getLongitude());
        // get the location from the latlng]
        try {
            holder.addressTV.setText(geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0).getLocality());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ViewPagerPostAdapter viewPagerAdapter = new ViewPagerPostAdapter(context , apartmentList.get(position).getImage_url());
        holder.apartmentViewPager.setAdapter(viewPagerAdapter);
        holder.dotsIndicator.setViewPager(holder.apartmentViewPager);
        viewPagerAdapter.registerDataSetObserver(holder.dotsIndicator.getDataSetObserver());

        List<String> preferences = apartmentList.get(position).getPreferences();

        for (String preference
                :preferences) {
            switch (preference) {
                case "male":
                    holder.maleButton.setVisibility(View.VISIBLE);
                    break;
                case "female":
                    holder.femaleButton.setVisibility(View.VISIBLE);
                    break;
                case "pet":
                    holder.petsAllowedButton.setVisibility(View.VISIBLE);
                    break;
                case "non_smoking":
                    holder.smokeFreeButton.setVisibility(View.VISIBLE);
            }
        }
//
//        String id = apartmentList.get(position).getUserID();
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        final String Uid = firebaseUser.getUid();
//
//        if(id.equals(Uid)){
//            holder.sendMessageButton.setVisibility(View.GONE);
//            holder.BUT_fav_apt.setVisibility(View.GONE);
//        }
//        else {
//            holder.sendMessageButton.setVisibility(View.VISIBLE);
//            holder.BUT_fav_apt.setVisibility(View.VISIBLE);
//        }
//
//        //favorite
//        final Boolean[] checkClick = {false};
//
//        final DatabaseReference favRef = FirebaseDatabase.getInstance().getReference().child("Favorite");
//
//        DatabaseReference anotherFavRef = FirebaseDatabase.getInstance().getReference().child("Favorite");
//        anotherFavRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.child(Uid).child("ApartmentPost").hasChild(apartmentList.get(position).getId())){
//                    holder.BUT_fav_apt.setImageResource(R.drawable.ic_icon_awesome_star_checked);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        if(isFromAptFav){
//
//            holder.BUT_fav_apt.setImageResource(R.drawable.ic_icon_awesome_star_checked);
//
//        }
//
//        else {
//
//            holder.BUT_fav_apt.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    checkClick[0] = true;
//                    favRef.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (checkClick[0])
//                                if (snapshot.child(Uid).child("ApartmentPost").hasChild(apartmentList.get(position).getId())) {
//                                    favRef.child(Uid).child("ApartmentPost").child(apartmentList.get(position).getId()).removeValue();
//                                    holder.BUT_fav_apt.setImageResource(R.drawable.ic_icon_awesome_star_unchecked);
//                                    Toast.makeText(context, "Post removed from favorites", Toast.LENGTH_LONG).show();
//                                    checkClick[0] = false;
//                                } else {
//                                    DatabaseReference anotherRef = favRef.child(Uid).child("ApartmentPost").child(apartmentList.get(position).getId());
//                                    anotherRef.child("id").setValue(apartmentList.get(position).getId());
//                                    anotherRef.child("userID").setValue(apartmentList.get(position).getUserID());
//                                    anotherRef.child("image_url").setValue(apartmentList.get(position).getImage_url());
//                                    anotherRef.child("price").setValue(apartmentList.get(position).getPrice());
//                                    anotherRef.child("date").setValue(apartmentList.get(position).getDate());
//                                    anotherRef.child("description").setValue(apartmentList.get(position).getDescription());
//                                    anotherRef.child("preferences").setValue(apartmentList.get(position).getPreferences());
//                                    anotherRef.child("latitude").setValue(apartmentList.get(position).getLatitude());
//                                    anotherRef.child("longitude").setValue(apartmentList.get(position).getLongitude());
//                                    anotherRef.child("numberOfRoommates").setValue(apartmentList.get(position).getNumberOfRoommates());
//                                    Toast.makeText(context, "Post added to favorites", Toast.LENGTH_LONG).show();
//                                    holder.BUT_fav_apt.setImageResource(R.drawable.ic_icon_awesome_star_checked);
//                                    checkClick[0] = false;
//
//                                }
//
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                }
//            });
//        }
    }


    @Override
    public long getItemId(int position) {
        Apartment apartment = apartmentList.get(position);
        // Lets return in real stable id from here
        //getting the hash code will make every id unique
        return (apartment.getApartmentID()).hashCode();
    }

    @Override
    public int getItemCount() {
        return apartmentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView priceTV,numRoomMateTV, addressTV ,descriptionTV;
        ImageView  maleButton,femaleButton, petsAllowedButton, smokeFreeButton;
        ViewPager apartmentViewPager;
        ImageButton sendMessageButton;
        CheckBox favoriteCheckBox;
        DotsIndicator dotsIndicator;
        RelativeLayout apartmentCardRelativeLayout;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            priceTV = itemView.findViewById(R.id.price_text_view_apartment_card);
            numRoomMateTV = itemView.findViewById(R.id.Room_mate_num);
            addressTV = itemView.findViewById(R.id.address_text_view);
            apartmentViewPager = itemView.findViewById(R.id.apartment_card_view_pager);
            maleButton = itemView.findViewById(R.id.male_image_apartment_card);
            femaleButton = itemView.findViewById(R.id.female_image_apartment_card);
            petsAllowedButton = itemView.findViewById(R.id.pets_allowed_image_apartment_card);
            smokeFreeButton = itemView.findViewById(R.id.non_smoking_image_apartment_card);
            sendMessageButton=itemView.findViewById(R.id.start_chat_button_apartment_card);
            dotsIndicator = itemView.findViewById(R.id.dotsIndicator_apartment_card);
            favoriteCheckBox = itemView.findViewById(R.id.favorite_check_box_apartment);
            descriptionTV = itemView.findViewById(R.id.apartment_description);
            apartmentCardRelativeLayout = itemView.findViewById(R.id.apartment_card_layout);
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
            // on click go to the apartment view
            apartmentCardRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ApartmentViewPage.class);
                    // add the parcelable apartment object to the intent and use it's values to
                    //update the apartment view class
                    intent.putExtra("apartment",apartmentList.get(getAdapterPosition()));
                    context.startActivity(intent);
                }
            });





            if(isFromAptFav){

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                final String Uid = firebaseUser.getUid();
                final DatabaseReference favRef = FirebaseDatabase.getInstance().getReference().child("Favorite");

            favoriteCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    favRef.child(Uid).child("ApartmentPost").child(apartmentList.get(getAdapterPosition()).getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                               apartmentList.remove(getAdapterPosition());
                                notifyItemRemoved(getAdapterPosition());

                        }
                    });

                }
            });
        }
    }

}
}


    class ViewPagerPostAdapter extends PagerAdapter {
        Context context;
        private List<String> apartmentImages;

        ViewPagerPostAdapter(Context context , List<String> apartmentImages ){
        this.context = context;
        this.apartmentImages = apartmentImages;

        }

        @Override
        public int getCount() {
                return apartmentImages.size();

        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view==object;

        }


        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
                ImageView imageView = new ImageView(context);
                imageView.setPadding(5,5,5,5);
                imageView.setElevation(3);
                MultiTransformation multiLeft = new MultiTransformation(new CenterCrop(), new RoundedCorners(25));
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(apartmentImages.get(position));
                // Load the image using Glide
                GlideApp.with(this.context)
                    .load(storageReference)
                    .transform(multiLeft)
                    .into(imageView);

                container.addView(imageView);
                return imageView;

        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View)object);

        }

}



