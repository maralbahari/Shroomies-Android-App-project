package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MapsFragment extends DialogFragment {
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleMap mMap;
    View v;
    private Button updateAddressButton;
    private String selectedAddress;
    private LatLng selectedLatLng;
    private String selectedLocationName;
    private OnLocationSet mOnLocationSet;
    private MarkerOptions mapMarker;
    private Geocoder geocoder;
    private PlacesClient placesClient;
    private CustomLoadingProgressBar customLoadingProgressBar;
    private TextView locationTextView;
    private LinearLayout searchBarLinearLayout;

    public interface OnLocationSet {
        void sendNewLocation(LatLng selectedLatLng, String selectedAddress, String selectedLocationName);
    }


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            mMap = googleMap;
            mMap.setMyLocationEnabled(false);

            getCurrentLocation();

        }

    };


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);

        }
        // Initialize the SDK
        Places.initialize(getActivity(), getString(R.string.api_key));
        // Create a new PlacesClient instance
        placesClient = Places.createClient(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_maps, container, false);
        customLoadingProgressBar = new CustomLoadingProgressBar(getActivity(), "Finding your current location...", R.raw.search_anim);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        // set cancelable to not allow the user
        // to use the search before the map and the marker
        // has been initialized
        customLoadingProgressBar.setCancelable(false);
        updateAddressButton = v.findViewById(R.id.update_location_button);
        searchBarLinearLayout = v.findViewById(R.id.search_bar_maps_fragment);
        locationTextView = v.findViewById(R.id.location_text_view_map_fragment);

        addAutoTextIntent();


        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }


        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLatLng != null && selectedAddress != null) {
                    // if a new address has been added return to the publish post fragment with the new adresses
                    mOnLocationSet.sendNewLocation(selectedLatLng, selectedAddress, selectedLocationName);
                    dismiss();
                }
            }
        });

    }

    private void getCurrentLocation() {
        customLoadingProgressBar.show();
        geocoder = new Geocoder(getActivity());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if permission to access fine location is granted
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Use fields to define the data types to return.
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG,
                        Place.Field.NAME, Place.Field.ADDRESS);

                // Use the builder to create a FindCurrentPlaceRequest.
                FindCurrentPlaceRequest request =
                        FindCurrentPlaceRequest.builder(placeFields).build();
                placesClient.findCurrentPlace(request).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {

                            FindCurrentPlaceResponse response = (FindCurrentPlaceResponse) task.getResult();
                            ArrayList<PlaceLikelihood> placeLikelihoods = new ArrayList<>();
                            placeLikelihoods.addAll(response.getPlaceLikelihoods());

                            //response.getPlaceLikelihoods() will return list of PlaceLikelihood
                            //we need to create a custom comparator to sort list by likelihoods
                            Collections.sort(placeLikelihoods, new Comparator<PlaceLikelihood>() {
                                @Override
                                public int compare(PlaceLikelihood o1, PlaceLikelihood o2) {
                                    return new Double(o1.getLikelihood()).compareTo(o2.getLikelihood());
                                }
                            });

                            //After sort ,it will order by ascending , we just reverse it to get first item as nearest place
                            Collections.reverse(placeLikelihoods);

                            selectedLocationName = placeLikelihoods.get(0).getPlace().getName();
                            selectedLatLng = placeLikelihoods.get(0).getPlace().getLatLng();
                            selectedAddress = placeLikelihoods.get(0).getPlace().getAddress();
                            // initialize the marker on the map
                            if (selectedLatLng != null) {
                                setMapMarker(selectedLatLng);

                            }
                            if (selectedLocationName != null) {
                                locationTextView.setText(selectedLocationName);
                            }
                            customLoadingProgressBar.dismiss();


                            //Removing item of the list at 0 index
                            placeLikelihoods.remove(0);

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // if the places api fails get the location from geocoder
                        getCurrentLocationFromGeoCoder();
                    }
                });

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    void setMapMarker(LatLng latLng) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("black_mushroom", "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 90, 100, false);
        mapMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                .draggable(true)
                .position(latLng);
        mMap.addMarker(mapMarker);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                selectedLatLng = marker.getPosition();
                getLocationFromLatLng(selectedLatLng);

            }
        });
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnLocationSet = (OnLocationSet) getTargetFragment();
        } catch (ClassCastException e) {

        }
    }


    private void addAutoTextIntent() {
        searchBarLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.NAME);
                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(getActivity());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                selectedAddress = place.getAddress();
                selectedLatLng = place.getLatLng();
                selectedLocationName = place.getName();
                if (selectedLocationName != null || selectedAddress!=null) {
                    locationTextView.setText(selectedLocationName+" ,"+selectedAddress);
                }
                if (mMap != null && selectedLatLng != null) {
                    mapMarker.position(selectedLatLng);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 18));
                    //remove the marker and add a new one
                    mMap.clear();
                    setMapMarker(selectedLatLng);

                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);


            } else if (resultCode == Activity.RESULT_CANCELED) {
                // TODO: Handle cancelation.
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);

    }


    private void getCurrentLocationFromGeoCoder() {

        //get the location of the phone in order to set the pin on the map to the current Location
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        //check the API level
        //get the location
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {


            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            customLoadingProgressBar.dismiss();
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                selectedLatLng = new LatLng(latitude, longitude);
                                setMapMarker(selectedLatLng);
                                getLocationFromLatLng(selectedLatLng);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    customLoadingProgressBar.dismiss();
                    Toast.makeText(getActivity() , " couldn't load your current location" , Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void getLocationFromLatLng(LatLng selectedLatLng){
        try {
            geocoder = new Geocoder(getActivity());
            // get the first index which os the best match
            List<Address> addresses = geocoder.getFromLocation(selectedLatLng.latitude , selectedLatLng.longitude , 1);
             if(addresses!=null){
                 if(addresses.size()>0) {
                     Address address = addresses.get(0);
                     selectedAddress = address.getAddressLine(0);
                     selectedLocationName = address.getFeatureName();
                     locationTextView.setText(selectedLocationName + ", " + selectedAddress);
                 }
             }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}