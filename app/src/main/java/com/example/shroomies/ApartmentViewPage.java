package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.util.Objects;

public class ApartmentViewPage extends AppCompatActivity implements OnMapReadyCallback {
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    ViewPager viewPager;
    ImageButton messageButton , expandImagesButton;
    DotsIndicator dotsIndicator;
    TextView priceTextView,descriptionTextView, numberOfRoomMates, date, username, locationAddressTextView;
    ViewPagerAdapterApartmentView viewPagerAdapterApartmentView;
    Apartment apartment;
    CustomMapView mapView;

    Geocoder geocoder;
    Toolbar toolbar;
    ImageView userImageView , maleImageView , femaleImageView , petsImageView , smokeFreeImageView;
    User user;
    FirebaseAuth mAuth;


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartment_page);
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        expandImagesButton = findViewById(R.id.expand_button);
        maleImageView = findViewById(R.id.male_image_view_apartment);
        femaleImageView = findViewById(R.id.female_image_view_apartment);
        petsImageView = findViewById(R.id.pets_allowd_image_view_apartment);
        smokeFreeImageView = findViewById(R.id.non_smoking_image_view_apartment);
        locationAddressTextView = findViewById(R.id.location_address_text_view);
        viewPager = findViewById(R.id.view_pager_apartment_view);
        messageButton = findViewById(R.id.message_user_button);
        dotsIndicator = findViewById(R.id.dotsIndicator_apartment_view);
        priceTextView = findViewById(R.id.price_text_view_apartment_card);
        descriptionTextView = findViewById(R.id.user_description_text_view);
        numberOfRoomMates = findViewById(R.id.number_of_roommates_text_view);
        userImageView = findViewById(R.id.user_image_view);
        date = findViewById(R.id.date_of_post_text_view);
        username = findViewById(R.id.name_of_user_text_view);
        toolbar=findViewById(R.id.view_apartment_post_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Shroomie Post");
        mapView = findViewById(R.id.apartment_post_page_map);
        mapView.getMapAsync(this);
        initGoogleMap(savedInstanceState);
        if (getIntent().getExtras() != null) {
            apartment = new Apartment();
            apartment = getIntent().getExtras().getParcelable("apartment");
            //set the desicription
            if(apartment!=null) {
                if(firebaseUser!=null) {
                    getPostOwnerDetails(firebaseUser.getUid(),apartment.getUserID());
                }
                descriptionTextView.setText(apartment.getDescription());
                numberOfRoomMates.setText((apartment.getNumberOfRoommates()) + " Roommates required ");
                date.setText(apartment.getTime_stamp());
                //set the location address
                geocoder = new Geocoder(getApplicationContext());
                try {
                    // for some reason the latitude and the longitude are saved oppositly
                    locationAddressTextView.setText(geocoder.getFromLocation(apartment.getLatitude(), apartment.getLongitude(), 1).get(0).getAddressLine(0));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> preferences = apartment.getPreferences();
                if (preferences!=null)
                    for (String preference
                            : preferences) {
                        if(preference!=null) {
                            switch (preference) {
                                case "male":
                                    maleImageView.setVisibility(View.VISIBLE);
                                    break;
                                case "female":
                                    femaleImageView.setVisibility(View.VISIBLE);
                                    break;
                                case "pet":
                                    petsImageView.setVisibility(View.VISIBLE);
                                    break;
                                case "non_smoking":
                                    smokeFreeImageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                viewPagerAdapterApartmentView = new ViewPagerAdapterApartmentView(getApplicationContext(), apartment.getImage_url());
                viewPager.setAdapter(viewPagerAdapterApartmentView);
                dotsIndicator.setViewPager(viewPager);
                viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
                expandImagesButton.setOnClickListener(v -> {
                    ImageViewPager imageViewPager = new ImageViewPager(apartment.getImage_url());
                    imageViewPager.show(getSupportFragmentManager() , null);
                });
            }


        }
        messageButton.setOnClickListener(v -> {
            if (user != null) {
                chatWithThisUser(user);
            }
        });


    }
    private void getPostOwnerDetails(String currentUserid,String userUid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Config.users).child(userUid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = new User();
                user = snapshot.getValue(User.class);
                if (user!=null) {
                    if (currentUserid.equals(user.getUserID())) {
                        messageButton.setVisibility(View.GONE);
                        username.setText("you");
                    } else {
                        username.setText(user.getUsername());
                    }

                    if (!user.getImage().isEmpty()) {
                        GlideApp.with(getApplication())
                                .load(user.getImage())
                                .transform(new CircleCrop())
                                .into(userImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void chatWithThisUser(User user) {
        Intent intent = new Intent(getApplication(), ChattingActivity.class);
        intent.putExtra("USER", user);
        startActivity(intent);
    }



    @Override
    protected void onStop() {
        super.onStop();
            mapView.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
            mapView.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
            mapView.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
            mapView.onResume();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

            mapView.onLowMemory();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        if(mapView!=null) {
            mapView.onSaveInstanceState(outState);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if(apartment!=null) {
            LatLng latLng = new LatLng(apartment.getLatitude(), apartment.getLongitude());
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("black_mushroom", "drawable", getApplicationContext().getPackageName()));
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 90, 100, false);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
            if (googleMap != null) {
                googleMap.addMarker(markerOptions);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            }
        }
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);


    }

    }


// create a new view pager adapter
class ViewPagerAdapterApartmentView extends PagerAdapter {
    Context context;
    private final List<String> imageUrls;
    ViewPagerAdapterApartmentView(Context context , List<String> imageUrls){
        this.context = context;
        this.imageUrls = imageUrls;

    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(imageUrls.get(position));

        // Load the image using Glide

        GlideApp.with(this.context)
                .load(storageReference)
                .transform(new CenterCrop())
                .into(imageView);

        ViewPager vp = (ViewPager) container;
        vp.addView(imageView);
        return imageView;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }



}