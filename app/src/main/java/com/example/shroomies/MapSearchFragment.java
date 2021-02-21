package com.example.shroomies;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSearchFragment extends Fragment {
    static GoogleMap mMap;
    Map<Marker, Apartment> markerMap;
    FusedLocationProviderClient fusedLocationProviderClient;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            markerMap = new HashMap<>();
            mMap = googleMap;
            getApartments(mMap);

            // set on click listeners to go to the apartment
            // view page of the selected markers
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Apartment selectedApartment = markerMap.get(marker);
                    Intent intent = new Intent(getActivity(), ApartmentViewPage.class);
                    intent.putExtra("apartment", selectedApartment);
                    startActivity(intent);
                }
            });
            // sets the view map to the proximity of the current location
            setCameraLocation();

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);

        }
    }



    private List<Apartment> getApartments(final GoogleMap mMap){
        final List<Apartment> apartments = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("postApartment");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot
                        : snapshot.getChildren()){
                    Apartment apartment = dataSnapshot.getValue(Apartment.class);
                    apartments.add(apartment);
                    //  add the marker for  the apartment to the map
                    addMarkers(apartment , mMap);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return apartments;
    }
    private void addMarkers(Apartment apartment , GoogleMap mMap){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("black_mushroom" ,  "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 90, 100, false);
        LatLng latLng = new LatLng(apartment.getLatitude(), apartment.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                .title(Integer.toString(apartment.getPrice()))
                .snippet("price")
                ;
        // show the snippet without click
        Marker locationMarker = mMap.addMarker(markerOptions);
        // add the marker and the apartment to the map
        // this enables quick access when setting an on click listener
        markerMap.put(locationMarker, apartment);
        locationMarker.showInfoWindow();
    }
    public static void setLocationView(LatLng latLng, int zoom){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }

    void setCameraLocation(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(getActivity());
            //check the API level
            //get the location
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>(){
                        @Override
                        public void onSuccess(Location location){
                            LatLng lastLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            setLocationView(lastLocationLatLng, 13);
                        }
                    });

        }
    }



}