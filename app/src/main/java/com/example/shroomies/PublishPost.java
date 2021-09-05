package com.example.shroomies;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.icu.text.NumberFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.make.dots.dotsindicator.DotsIndicator;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PublishPost extends Fragment  implements MapsFragment.OnLocationSet {
    public static final int DIALOG_FRAGMENT_REQUEST_CODE = 1;
    public static final int MAPS_FRAGMENT_REQUEST_CODE = 2;
    public  static final int NUMBER_OF_IMAGES_ALLOWED = 5;
    private static final int PICK_IMAGE_MULTIPLE= 1;

    private View v;
    private TextView locationTextView ;
    private ImageView userImageView;
    private ChipGroup postTypeChipGroup;
    private Chip apartmentPostChip,personalPostChip;

    private EditText descriptionEditText;
    private MaterialButton nextButton;
//
//    private int currentViewPagerPosition;
    private String address,locality,subLocality;
    private LatLng selectedLatLng;
    private Geocoder geocoder;


//    private List<Uri> imageUri;
//    private List<String> preferences ;
//    private ViewPagerAdapter viewPagerAdapter;

    private FragmentManager fm;
    private FragmentTransaction ft;


    private RequestQueue requestQueue;
    //    String postUniqueName;

    private FirebaseAuth mAuth;
    private StorageReference filePath;
//
//    private Validate validate;

    // override the interface method "sendNewLocation" to get the preferances
    @Override
    public void sendNewLocation(LatLng selectedLatLng, String selectedAddress , String selectedLocationName) {
        this.selectedLatLng = selectedLatLng;
        this.address = selectedAddress;
        this.address = selectedAddress;
        locationTextView.setText(selectedLocationName +" "+ selectedAddress);
        geocoder = new Geocoder(getActivity());
        try {
            List<Address> addresses = geocoder.getFromLocation(selectedLatLng.latitude , selectedLatLng.longitude , 1);
            if(addresses!=null){
                if(addresses.size()>0) {
                    Address address =  addresses.get(0);
                    locality = address.getLocality();
                    subLocality = address.getSubLocality();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_publish_post, container, false);
        mAuth=FirebaseAuth.getInstance();
        requestQueue= Volley.newRequestQueue(getActivity());
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationTextView = v.findViewById(R.id.location_chip_view);

        postTypeChipGroup = v.findViewById(R.id.chip_group);
        personalPostChip  = v.findViewById(R.id.personal_post_chip);
        apartmentPostChip = v.findViewById(R.id.apartment_post_chip);
        nextButton = v.findViewById(R.id.publish_post_next_button);

        descriptionEditText = v.findViewById(R.id.post_description);
        userImageView = v.findViewById(R.id.user_image_publish_post);

        if(selectedLatLng ==null){
            getLocation();
        }else{
            setCurrentLocationAddress(selectedLatLng.latitude, selectedLatLng.longitude);
        }

        getUserImage();
        nextButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            String postType;
            Toast.makeText(getActivity(),postTypeChipGroup.getCheckedChipId()+" "+R.id.apartment_post_chip, Toast.LENGTH_LONG).show();
            if(postTypeChipGroup.getCheckedChipId()==R.id.apartment_post_chip){
                postType = Config.APARTMENT_POST;
            }else{
                postType = Config.PERSONAL_POST;
            }
            if(postType.equals(Config.APARTMENT_POST)&&locality!=null&&subLocality!=null&& descriptionEditText.getText().toString().trim()!=null){
                bundle.putString(Config.LOCALITY,  locality);
                bundle.putString(Config.SUB_LOCALITY , subLocality);
                bundle.putParcelable(Config.SELECTED_LAT_LNG,  selectedLatLng);
                bundle.putString(Config.DESCRIPTION , descriptionEditText.getText().toString().trim());
                bundle.putString(Config.POST_TYPE ,postType);
                getFragment(new PublishPostPreferances() , bundle);
            }else if( descriptionEditText.getText().toString().trim()!=null){
                bundle.putString(Config.DESCRIPTION , descriptionEditText.getText().toString().trim());
                bundle.putString(Config.POST_TYPE ,postType);
                getFragment(new PublishPostPreferances() , bundle);


            }else{
                //todo handle
            }

        });


        // sets focus to the edit text as soon as the page is open
        descriptionEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(descriptionEditText, InputMethodManager.SHOW_IMPLICIT);

        locationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsFragment mapsFragment = new MapsFragment();
                mapsFragment.setTargetFragment(PublishPost.this ,MAPS_FRAGMENT_REQUEST_CODE );
                mapsFragment.show(getParentFragmentManager() , null);
            }
        });

        postTypeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId==R.id.apartment_post_chip){
                locationTextView.setVisibility(View.VISIBLE);
                apartmentPostChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.LogoYellow)));
                personalPostChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.white)));

            }else{
                personalPostChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.LogoYellow)));
                apartmentPostChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.white)));

                locationTextView.setVisibility(View.GONE);

            }

        });



    }


    private  void getLocation() {
        new CurrentLocationAsync(getActivity()).execute();
    }



    private class CurrentLocationAsync extends AsyncTask<Void , String , LatLng> {
        FusedLocationProviderClient fusedLocationProviderClient;
        double latitude;
        double longitude;
        Context context;


        CurrentLocationAsync(Context context){
            this.context = context;
        }
        @Override
        protected LatLng doInBackground(Void... voids) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check if permission to access fine location is granted
                if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //get the location of the phone in order to set the pin on the map to the current Location
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
                    //check the API level
                    //get the location
                    fusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        latitude = location.getLatitude();
                                        longitude = location.getLongitude();
                                        selectedLatLng = new LatLng(latitude, longitude);

                                        setCurrentLocationAddress(latitude , longitude);
                                        // get the icon and resize it to set it as the marker
                                    }
                                }
                            });
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    getLocation();
                }
            }
            return selectedLatLng;
        }
    }
    void setCurrentLocationAddress(double latitude, double longitude)  {
        geocoder = new Geocoder(getActivity());
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
           addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
           if(addresses!=null){
               if(addresses.size()>0){
                   locality = addresses.get(0).getLocality();
                   subLocality = addresses.get(0).getSubLocality();
                   String mAddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                   String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                   if(mAddress!=null || knownName!=null) {
                       address = knownName + " , " + mAddress;
                       locationTextView.setText(address);

                   }
               }
           }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getFragment (Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);
        fm = getActivity().getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.replace(R.id.publish_post_container, fragment);
        ft.commit();
    }
    private void getUserImage(){
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser= mAuth.getCurrentUser();
        rootRef.child(Config.users).child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String url = snapshot.child("image").getValue(String.class);
                    if(!url.isEmpty()){
                        GlideApp.with(getContext())
                                .load(url)
                                .centerCrop()
                                .circleCrop()
                                .into(userImageView);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}






