package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ApartmentViewPage extends AppCompatActivity implements OnMapReadyCallback {
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private ImageButton messageButton;
    private TextView username;
    private TextView preferencesTextView;
    private TextView genderTextView;
    private Apartment apartment;
    private CustomMapView mapView;

    private ImageView userImageView;
    private User user;


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartment_page);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();


        TextView locationAddressTextView = findViewById(R.id.location_address_text_view);
        TextView buildingTypeTextView = findViewById(R.id.building_type);
        ViewPager viewPager = findViewById(R.id.view_pager_apartment_view);
        messageButton = findViewById(R.id.message_user_button);
        DotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator_apartment_view);
        TextView priceTextView = findViewById(R.id.price_text_view_apartment_card);
        TextView descriptionTextView = findViewById(R.id.user_description_text_view);
        TextView numberOfRoomMates = findViewById(R.id.number_of_roommates_text_view);
        userImageView = findViewById(R.id.user_image_view);
        TextView date = findViewById(R.id.date_of_post_text_view);
        username = findViewById(R.id.name_of_user_text_view);
        Toolbar toolbar = findViewById(R.id.view_apartment_post_toolbar);
        ChipGroup genderChipGroup = findViewById(R.id.gender_chip_group);
        ChipGroup preferencesChipGroup = findViewById(R.id.preferences_chip_group);
        Chip maleChip = findViewById(R.id.male_chip);
        Chip femaleChip = findViewById(R.id.female_chip);
        Chip petChip = findViewById(R.id.pet_chip);
        Chip smokingChip = findViewById(R.id.smoking_chip);
        Chip alcoholChip = findViewById(R.id.alcohol_chip);
        preferencesTextView = findViewById(R.id.preferences_text_view);
        genderTextView = findViewById(R.id.gender_text_view);
        MotionLayout readMoreMotionLayout = findViewById(R.id.description_layout);
        Chip readMoreChip = findViewById(R.id.read_more_chip);


        readMoreMotionLayout.addTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                motionLayout.setEnabled(false);
                if (i1 == motionLayout.getEndState()) {
                    readMoreChip.setText("Read less", TextView.BufferType.NORMAL);
                }

            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {


            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                if (i == motionLayout.getStartState()) {
                    readMoreChip.setText("Read more", TextView.BufferType.NORMAL);

                }
                motionLayout.setEnabled(true);


            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

            }
        });

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle("Place");
        getSupportActionBar().setTitle("");
        mapView = findViewById(R.id.apartment_post_page_map);
        mapView.getMapAsync(this);
        initGoogleMap(savedInstanceState);
        if (getIntent().getExtras() != null) {
            apartment = new Apartment();
            apartment = getIntent().getExtras().getParcelable("apartment");
            //set the desicription
            if (apartment != null) {
                if (firebaseUser != null) {
                    getPostOwnerDetails(firebaseUser.getUid(), apartment.getUserID());
                }
                //format and set date
                if (apartment.getTimeStamp() != null) {
                    Timestamp timestamp = apartment.getTimeStamp();
                    Date formattedDate = timestamp.toDate();
                    SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    date.setText(sfd.format(formattedDate));
                }

                if (apartment.getDescription() != null) {
                    descriptionTextView.setText(apartment.getDescription());
                }

                int price = apartment.getPrice();
                int numRoomMates = apartment.getNumberOfRoommates();
                if (price > 0) {
                    priceTextView.setText(price + " RM");
                }
                numberOfRoomMates.setText(numRoomMates + " roommates");


                //set the location address
                String buildingType = apartment.getBuildingType();
                switch (buildingType) {
                    case Config.TYPE_APARTMENT:
                        buildingTypeTextView.setText("Apartment");
                        break;
                    case Config.TYPE_HOUSE:
                        buildingTypeTextView.setText("Town house");
                        break;
                    case Config.TYPE_FLAT:
                        buildingTypeTextView.setText("Flat");
                        break;
                    case Config.TYPE_CONDO:
                        buildingTypeTextView.setText("Condominium");
                        break;
                }

                if (buildingType.equals(Config.TYPE_HOUSE)) {
                    String locality = apartment.getLocality();
                    String subLocality = apartment.getSubLocality();
                    if (locality != null && subLocality != null) {
                        locationAddressTextView.setText(subLocality + ", " + locality);
                    } else {

                    }
                } else {
                    String buildingName = apartment.getBuildingName();
                    if (buildingName != null) {
                        locationAddressTextView.setText(buildingName);
                    } else {

                    }
                }
                int prefCounter = 0;
                int genderPrefsCounter = 0;

//                Toast.makeText(getApplication(), apartment.getPreferences(), Toast.LENGTH_SHORT).show();

                String prefs = apartment.getPreferences();
                if (prefs.charAt(0) == '1') {
                    maleChip.setVisibility(View.VISIBLE);
                    genderPrefsCounter++;
                }
                if (prefs.charAt(1) == '1') {
                    femaleChip.setVisibility(View.VISIBLE);
                    genderPrefsCounter++;
                }
                if (prefs.charAt(2) == '1') {
                    petChip.setVisibility(View.VISIBLE);
                    prefCounter++;
                }
                if (prefs.charAt(3) == '1') {
                    smokingChip.setVisibility(View.VISIBLE);
                    prefCounter++;
                }
                if (prefs.charAt(4) == '1') {
                    alcoholChip.setVisibility(View.VISIBLE);
                    prefCounter++;
                }
                if (prefCounter == 0) {
                    preferencesTextView.setVisibility(View.GONE);
                    preferencesChipGroup.setVisibility(View.GONE);
                }
                if (genderPrefsCounter == 0) {
                    genderTextView.setVisibility(View.GONE);
                    genderChipGroup.setVisibility(View.GONE);
                }


                ViewPagerAdapterApartmentView viewPagerAdapterApartmentView = new ViewPagerAdapterApartmentView(this, apartment.getImageUrl());
                viewPager.setAdapter(viewPagerAdapterApartmentView);
                dotsIndicator.setViewPager(viewPager);
                viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
//                expandImagesButton.setOnClickListener(v -> {
//                    ImageViewPager imageViewPager = new ImageViewPager(apartment.getImageUrl());
//                    imageViewPager.show(getSupportFragmentManager() , null);
//                });
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
                        username.setText("You");
                        preferencesTextView.setText("You're allowing:");
                        genderTextView.setText("You're looking for");
                    } else {
                        String userName = user.getUsername();
                        username.setText(userName);
                        preferencesTextView.setText(userName + " allows:");
                        genderTextView.setText(userName + " is looking for");
                    }

                    if (!user.getImage().isEmpty()) {
                        GlideApp.with(getApplication())
                                .load(user.getImage())
                                .transform(new CircleCrop())
                                .transition(DrawableTransitionOptions.withCrossFade())
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
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(bitmapDescriptorFromVector());
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

    private BitmapDescriptor bitmapDescriptorFromVector() {

        Drawable vectorDrawable = ContextCompat.getDrawable(this, R.drawable.ic_shroomies_yelllow_black_borders);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
        imageView.setOnClickListener(v -> {
            ImageViewPager imageViewPager = new ImageViewPager();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(Config.IMAGE_URL, (ArrayList<String>) imageUrls);
            imageViewPager.setArguments(bundle);
            imageViewPager.show(((ApartmentViewPage) context).getSupportFragmentManager(), null);
        });


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