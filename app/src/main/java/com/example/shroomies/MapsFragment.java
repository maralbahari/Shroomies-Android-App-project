package com.example.shroomies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MapsFragment extends DialogFragment {
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private MaterialButton updateAddressButton;
    private String subLocality, buildingName, buildingAddress;
    private LatLng selectedLatLng;
    private String locality;
    private OnLocationSet mOnLocationSet;
    private MarkerOptions mapMarker;
    private TextView locationTextView;
    private CardView searchBarCardView;
    private boolean townHouse;
    private String postType;
    private HorizontalScrollView horizontalScrollView;

    public interface OnLocationSet {
        void OnLocationForApartmentSet(LatLng selectedLatLng, String buildingName, String buildingAddress);

        void OnLocationForTownHouseSet(LatLng selectedLatLng, String subLocality, String locality, String buildingAddress);

        void OnLocationForPersonalPostSet(LatLng selectedLatLng, String subLocality, String locality);
    }

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
            } else {
                getCurrentLocation();
            }
            mMap.setMyLocationEnabled(false);
            if (getArguments() != null) {
                townHouse = getArguments().getBoolean(Config.TYPE_HOUSE);
                postType = getArguments().getString(Config.POST_TYPE);
            }

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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        updateAddressButton = v.findViewById(R.id.update_location_button);
        searchBarCardView = v.findViewById(R.id.search_bar_maps_fragment);
        locationTextView = v.findViewById(R.id.location_text_view);
        locationTextView.setSelected(true);

        MaterialToolbar toolbar = v.findViewById(R.id.map_fragment_toolbar);
        toolbar.setNavigationOnClickListener(v1 -> dismiss());

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

        updateAddressButton.setOnClickListener(v -> {
            if (postType.equals(Config.APARTMENT_POST)) {
                if (selectedLatLng == null) {
                    new CustomToast((PublishPostActivity) getContext(), "Please select a different location", R.drawable.ic_error_icon).showCustomToast();
                } else {
                    if (!townHouse) {
                        if (buildingName != null && buildingAddress != null) {
                            mOnLocationSet.OnLocationForApartmentSet(selectedLatLng, buildingName, buildingAddress);
                            dismiss();
                            mMap.clear();
                        } else {
                            new CustomToast((PublishPostActivity) getContext(), "Please search for the name of the building", R.drawable.ic_error_icon).showCustomToast();
                        }
                    } else {
                        if (subLocality != null && locality != null && buildingAddress != null) {
                            mOnLocationSet.OnLocationForTownHouseSet(selectedLatLng, subLocality, locality, buildingAddress);
                            dismiss();
                            mMap.clear();
                        } else {
                            new CustomToast((PublishPostActivity) getContext(), "Please select a different location", R.drawable.ic_error_icon).showCustomToast();
                        }
                    }
                }
            } else {
                if (selectedLatLng == null || locality == null || subLocality == null) {
                    new CustomToast((PublishPostActivity) getContext(), "Please select a different location", R.drawable.ic_error_icon).showCustomToast();
                } else {
                    mOnLocationSet.OnLocationForPersonalPostSet(selectedLatLng, subLocality, locality);
                    dismiss();
                    mMap.clear();
                }
            }


        });

    }

    void setMapMarker(LatLng latLng) {
        mapMarker = new MarkerOptions()
                .icon(bitmapDescriptorFromVector())
                .position(latLng);
//        CircleOptions circleOptions = new CircleOptions();
//        // Specifying the center of the circle
//        circleOptions.center(latLng);
//        // Radius of the circle
//        circleOptions.radius(100);
//        // Border color of the circle
//        circleOptions.strokeColor(Color.YELLOW);
//        // Fill color of the circle
//        circleOptions.fillColor(0x30ff0000);
//        // Border width of the circle
//        circleOptions.strokeWidth(2);
//        Circle drawnCircle = null;

        if (getArguments() != null) {
            if (townHouse || postType.equals(Config.PERSONAL_POST)) {
                mapMarker.draggable(true);
//                drawnCircle = mMap.addCircle(circleOptions);
            }
        }
        mMap.addMarker(mapMarker);


//        Circle finalDrawnCircle = drawnCircle;
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (marker.getPosition() != null) {
//                    finalDrawnCircle.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                selectedLatLng = marker.getPosition();
                buildingName = null;
                getAddressFromLatLng(selectedLatLng, true);
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
            dismiss();
            new CustomToast((PublishPostActivity) getContext(), "Something went wrong", R.drawable.ic_error_icon).showCustomToast();
        }
    }


    private void addAutoTextIntent() {
        searchBarCardView.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.NAME);
            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(getActivity());
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                selectedLatLng = place.getLatLng();
                buildingName = place.getName();
                buildingAddress = place.getAddress();
                if (postType.equals(Config.APARTMENT_POST)) {
                    if (selectedLatLng == null || buildingAddress == null) {
                        new CustomToast((PublishPostActivity) getContext(), "Couldn't get the address", R.drawable.ic_error_icon).showCustomToast();
                        selectedLatLng = null;
                        buildingAddress = null;
                        buildingName = null;
                        return;
                    }
                    if (!townHouse && buildingName == null) {
                        selectedLatLng = null;
                        buildingAddress = null;
                        buildingName = null;
                        new CustomToast((PublishPostActivity) getContext(), "Couldn't get the building name", R.drawable.ic_error_icon).showCustomToast();
                        return;
                    }
                    if (townHouse) {
                        getAddressFromLatLng(selectedLatLng, false);
                        locationTextView.setText(buildingAddress);
                    } else {
                        buildingName = place.getName();
                        locationTextView.setText(buildingName);
                    }
                    if (mMap != null && selectedLatLng != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 18));
                        //remove the marker and add a new one
                        mMap.clear();
                        setMapMarker(selectedLatLng);
                    }
                } else {
                    getAddressFromLatLng(selectedLatLng, true);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 18));
                    //remove the marker and add a new one
                    mMap.clear();
                    setMapMarker(selectedLatLng);

                }

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                new CustomToast((PublishPostActivity) getContext(), status.getStatusMessage(), R.drawable.ic_error_icon).showCustomToast();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }


    private void getCurrentLocation() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                if (locationResult == null) {
                    new CustomToast((PublishPostActivity) getContext(), "couldn't find your current location", R.drawable.ic_error_icon).showCustomToast();
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        selectedLatLng = new LatLng(latitude, longitude);
                        setMapMarker(selectedLatLng);
                        getAddressFromLatLng(selectedLatLng, true);
                        return;
                    }
                    new CustomToast((PublishPostActivity) getContext(), "couldn't find your current location", R.drawable.ic_error_icon).showCustomToast();

                }
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);

            }
        };
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    void getAddressFromLatLng(LatLng selectedLatLng, boolean updateLocationTextView) {
        try {
            Geocoder geocoder = new Geocoder(getActivity());
            // get the first index which os the best match
            List<Address> addresses = geocoder.getFromLocation(selectedLatLng.latitude, selectedLatLng.longitude, 1);
            if (addresses != null) {
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    subLocality = address.getSubLocality();
                    locality = address.getLocality();
                    buildingAddress = address.getAddressLine(0);
                    if (locality == null || subLocality == null || buildingAddress == null) {
                        new CustomToast((PublishPostActivity) getContext(), "Please select a different location", R.drawable.ic_error_icon).showCustomToast();
                        this.selectedLatLng = null;

                    } else {
                        if (updateLocationTextView) {
                            locationTextView.setText(buildingAddress);
                        }
                    }

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector() {

        Drawable vectorDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_shroomies_yelllow_black_borders);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void requestPermission() {
        Dexter.withContext(getActivity())
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            getCurrentLocation();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


}
