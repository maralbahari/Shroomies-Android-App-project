package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.make.dots.dotsindicator.DotsIndicator;

import java.util.List;
import java.util.Set;

public class RecycleViewAdapterApartments extends RecyclerView.Adapter<RecycleViewAdapterApartments.ViewHolder> {
    private final List<Apartment> apartmentList;
    private final Context context;
    private final Set<String> favoriteSet;
    private final boolean isFromUserProfile;
    private final boolean isFromFavourites;
    private NotifyEmptyApartmentAdapter mNotifyEmptyAdapter;
    private final UserFavourites userFavourites;

    public RecycleViewAdapterApartments(List<Apartment> apartmentList, Context context, boolean isFromFavourites, boolean isFromUserProfile) {
        this.apartmentList = apartmentList;
        this.context = context;
        this.isFromUserProfile = isFromUserProfile;
        this.isFromFavourites = isFromFavourites;
        // retrieve the users favorite post ids from shared prefs
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userFavourites = UserFavourites.getInstance(context, mAuth.getCurrentUser().getUid());
        favoriteSet = userFavourites.getApartmentPostFavourites();

        setHasStableIds(true);
    }

    public RecycleViewAdapterApartments(List<Apartment> apartmentList, Context context, boolean isFromFavourites, boolean isFromUserProfile, NotifyEmptyApartmentAdapter mNotifyEmptyAdapter) {
        //call the first constructor
        this(apartmentList, context, isFromFavourites, isFromUserProfile);
        this.mNotifyEmptyAdapter = mNotifyEmptyAdapter;

    }


    @NonNull
    @Override
    public RecycleViewAdapterApartments.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.apartment_card, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        String buildingType = apartmentList.get(position).getBuildingType();
        switch (buildingType) {
            case Config.TYPE_APARTMENT:
                holder.buildingTypeTextView.setText("Apartment");
                break;
            case Config.TYPE_HOUSE:
                holder.buildingTypeTextView.setText("Town house");
                break;
            case Config.TYPE_FLAT:
                holder.buildingTypeTextView.setText("Flat");
                break;
            case Config.TYPE_CONDO:
                holder.buildingTypeTextView.setText("Condominium");
                break;
        }
        if (buildingType.equals(Config.TYPE_HOUSE)) {
            String locality = apartmentList.get(position).getLocality();
            String subLocality = apartmentList.get(position).getSubLocality();
            if (locality != null && subLocality != null) {
                holder.addressTV.setText(subLocality + ", " + locality);
            }
        } else {
            String buildingName = apartmentList.get(position).getBuildingName();
            if (buildingName != null) {
                holder.addressTV.setText(buildingName);
            }
        }
        //set the description
        holder.descriptionTV.setText(apartmentList.get(position).getDescription());
        // set the price of the apartment
        int price = apartmentList.get(position).getPrice();
        int numRoomMates = apartmentList.get(position).getNumberOfRoommates();
        if (price > 0) {
            holder.priceTV.setText(price + " RM");
            holder.numRoomMateTV.setText(" • " + numRoomMates + " roommates");
        } else {
            holder.numRoomMateTV.setText(numRoomMates + " roommates");
        }
//        holder.priceTV.setText(apartmentList.get(position).getPrice());
        //set the number of roommates

        ViewPagerPostAdapter viewPagerAdapter = new ViewPagerPostAdapter(context, apartmentList.get(position).getImageUrl());
        holder.apartmentViewPager.setAdapter(viewPagerAdapter);
        holder.dotsIndicator.setViewPager(holder.apartmentViewPager);
        viewPagerAdapter.registerDataSetObserver(holder.dotsIndicator.getDataSetObserver());

        int prefCounter = 0;

        String prefs = apartmentList.get(position).getPreferences();
        if (prefs.charAt(0) == '1') {
            holder.maleButton.setVisibility(View.VISIBLE);
            prefCounter++;
        } else {
            holder.maleButton.setVisibility(View.GONE);
        }
        if (prefs.charAt(1) == '1') {
            holder.femaleButton.setVisibility(View.VISIBLE);
            prefCounter++;
        } else {
            holder.femaleButton.setVisibility(View.GONE);

        }
        if (prefs.charAt(2) == '1') {
            holder.petsAllowedButton.setVisibility(View.VISIBLE);
            prefCounter++;
        } else {
            holder.petsAllowedButton.setVisibility(View.GONE);
        }
        if (prefs.charAt(3) == '1') {
            holder.smokeFreeButton.setVisibility(View.VISIBLE);
            prefCounter++;
        } else {
            holder.smokeFreeButton.setVisibility(View.GONE);
        }
        if (prefs.charAt(4) == '1') {
            holder.alcoholButton.setVisibility(View.VISIBLE);
            prefCounter++;
        } else {
            holder.smokeFreeButton.setVisibility(View.GONE);
        }
        if (prefCounter == 0) {
            holder.prefereanceLinearLayout.setVisibility(View.GONE);
        }
        if (favoriteSet != null) {
            holder.favoriteButton.setSelected(favoriteSet.contains(apartmentList.get(position).getApartmentID()));
        }

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

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView priceTV, numRoomMateTV, addressTV, descriptionTV, buildingTypeTextView;
        ImageView maleButton, femaleButton, petsAllowedButton, smokeFreeButton, alcoholButton;
        ViewPager apartmentViewPager;
        ImageButton deletePostButton, favoriteButton;
        DotsIndicator dotsIndicator;
        RelativeLayout apartmentCardRelativeLayout;
        LinearLayout prefereanceLinearLayout;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            priceTV = itemView.findViewById(R.id.price_text_view_apartment_card);
            buildingTypeTextView = itemView.findViewById(R.id.building_type);
            numRoomMateTV = itemView.findViewById(R.id.Room_mate_num);
            addressTV = itemView.findViewById(R.id.address_text_view);
            apartmentViewPager = itemView.findViewById(R.id.apartment_card_view_pager);
            maleButton = itemView.findViewById(R.id.male_image_apartment_card);
            femaleButton = itemView.findViewById(R.id.female_image_apartment_card);
            petsAllowedButton = itemView.findViewById(R.id.pets_allowed_image_apartment_card);
            smokeFreeButton = itemView.findViewById(R.id.non_smoking_image_apartment_card);
            alcoholButton = itemView.findViewById(R.id.alcohol_image_apartment_card);
            dotsIndicator = itemView.findViewById(R.id.dotsIndicator_apartment_card);
            favoriteButton = itemView.findViewById(R.id.favorite_check_box_apartment);
            descriptionTV = itemView.findViewById(R.id.apartment_price);
            apartmentCardRelativeLayout = itemView.findViewById(R.id.apartment_card_layout);
            deletePostButton = itemView.findViewById(R.id.apartment_post_delete_button);
            prefereanceLinearLayout = itemView.findViewById(R.id.preference_linear_layout);

            if (isFromUserProfile) {
                deletePostButton.setVisibility(View.VISIBLE);
            }

            // on click go to the apartment view
            apartmentCardRelativeLayout.setOnClickListener(v -> goToApartmentViewPage(getAdapterPosition()));
            // on check add to shared preferences

            favoriteButton.setOnClickListener(v -> {
                if (!favoriteButton.isSelected()) {
                    addToFavourites(favoriteButton, getAdapterPosition());
                } else {
                    removeFromFavourites(favoriteButton, getAdapterPosition());
                }
            });
        }

    }

    private void addToFavourites(final ImageButton favoriteButton, int position) {
        if (position != -1) {
            favoriteButton.setSelected(true);
            userFavourites.addApartmentPostToFavourites(apartmentList
                    .get(position)
                    .getApartmentID());
        }

    }

    void removeFromFavourites(final ImageButton favoriteButton, int position) {
        if (position != -1) {
            favoriteButton.setSelected(false);
            userFavourites.removeApartmentPostFavourites(apartmentList
                    .get(position)
                    .getApartmentID());
            if (isFromFavourites) {
                apartmentList.remove(position);
                notifyItemRemoved(position);
                if (apartmentList.isEmpty()) {
                    mNotifyEmptyAdapter.onEmptyApartmentAdapter();
                }
            }
        }

    }

    private void goToApartmentViewPage(int position) {
        Intent intent = new Intent(context, ApartmentViewPage.class);
        // add the parcelable apartment object to the intent and use it's values to
        //update the apartment view class
        intent.putExtra("apartment", apartmentList.get(position));
        context.startActivity(intent);
    }
}

    class ViewPagerPostAdapter extends PagerAdapter {
        Context context;
        private final List<String> apartmentImages;

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
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(apartmentImages.get(position));
                // Load the image using Glide
            GlideApp.with(this.context)
                    .load(storageReference)
                    .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCorners(25)))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
                container.addView(imageView);
            return imageView;

        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

    }

interface NotifyEmptyApartmentAdapter {
    void onEmptyApartmentAdapter();
}




