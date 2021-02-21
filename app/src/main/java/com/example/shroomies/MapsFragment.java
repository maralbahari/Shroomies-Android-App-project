package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends DialogFragment {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double latitude;
    private double longitude;
    private ListView searchResultsListView;
    private SearchView mapSearchView;
    private ArrayAdapter<String> arrayAdapter;
    private Button updateAddressButton;
    private String updatedAddress;
    private Address selectedAddress;
    private List<Address> suggestedAddresses;
    private LatLng newLatLng;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;

    private OnLocationSet mOnLocationSet;
    private SearchView.OnQueryTextListener onQueryTextListener;
    private Geocoder geocoder;

    CustomLoadingProgressBar customLoadingProgressBar;

     public interface OnLocationSet {
         void sendNewLocation(LatLng newLatLng , String updatedAddress);
     };

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    ||ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
                mMap = googleMap;
                googleMap.setMyLocationEnabled(false);
                getCurrentLocationFromAsyncTask();

            final Geocoder geocoder = new Geocoder(getActivity());
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("black_mushroom" ,  "drawable", getActivity().getPackageName()));
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 90, 100, false);
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                    .draggable(true);
            mMap.addMarker(markerOptions);
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) { }

                @Override
                public void onMarkerDrag(Marker marker) { }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    newLatLng = marker.getPosition();
                    mapSearchView.setOnQueryTextListener(null);
                    // remove the query change listener to prevent the methods in it from being called
                    //
                    try {
                        address= geocoder.getFromLocation(newLatLng.latitude , newLatLng.longitude , 1);
                        updatedAddress = address.get(0).getAddressLine(0);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mapSearchView.setQuery(updatedAddress, false);
                    mapSearchView.setIconified(false);
                    searchResultsListView.setVisibility(View.GONE);

                    // add the listener back once the dragging has stopped

                    mapSearchView.setOnQueryTextListener(onQueryTextListener);
                }
            });

        }

    };


    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);

        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity()!=null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(getActivity()!=null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_maps, container, false);
        customLoadingProgressBar= new CustomLoadingProgressBar(getActivity(), "Searching..." , R.raw.search_anim);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mapSearchView = v.findViewById(R.id.search_view_map_fragment);
        searchResultsListView = v.findViewById(R.id.list_view_map_fragment);
        updateAddressButton =  v.findViewById(R.id.update_location_button);

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

            onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //start the progressbar
                customLoadingProgressBar.show();
                //set the list view to visible if it has been set to gone in the on close call
                //get the adresses and add them to the list view
                new GetAdressesAsyncTask(getActivity() , query).execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchResultsListView.setVisibility(View.VISIBLE);
                new GetAdressesAsyncTask(getActivity() , newText).execute();
                return false;
            }
        };
        mapSearchView.setOnQueryTextListener(onQueryTextListener);
        mapSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //in case any list item are still in the list , set the list visibility to gone
                searchResultsListView.setVisibility(View.GONE);
                return false;
            }
        });
        searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //store  the address name from the adapter
                address =  parent.getItemAtPosition(position);
                updatedAddress = parent.getItemAtPosition(position).toString();
                // store the lat and long of the new location
                mapSearchView.setQuery(updatedAddress,true);
                searchResultsListView.setVisibility(View.GONE);
                newLatLng = latLngs.get(position);
                setMarker(newLatLng);
            }
        });
        updateAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newLatLng != null && updatedAddress !=null){
                    // if a new address has been added return to the publish post fragment with the new adresses
                    mOnLocationSet.sendNewLocation(newLatLng , updatedAddress);
                    dismiss();
                }
            }
        });

    }


    private void  getCurrentLocationFromAsyncTask() {
        new CurrentLocationAsync(getActivity()).execute();
    }

    // async task to get the last known locatio n and update the marker
    // this task will be called from the on map reaady method

    private class CurrentLocationAsync extends AsyncTask<Void , String , LatLng> {
        FusedLocationProviderClient fusedLocationProviderClient;
        double latitude;
        double longitude;
        Context context;
        LatLng latLng;

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
                                        latLng = new LatLng(latitude, longitude);
                                        mapSearchView.setOnQueryTextListener(null);
                                        // remove the query change listener to prevent the methods in it from being called
                                        //
                                        try {
                                            address= geocoder.getFromLocation(latitude, longitude , 1).get(0);
                                            updatedAddress = address.getAddressLine(0);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        mapSearchView.setQuery(updatedAddress, false);
                                        mapSearchView.setIconified(false);
                                        searchResultsListView.setVisibility(View.GONE);
                                        setMarker((LatLng) latLng);

                                    }
                                }
                            });
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
            return latLng;
        }
    }


    //    async task class to find adresses using geocoder  on the background
//            geoecoder needs a seperate thread to run smoothly especially with live search
   public class GetAdressesAsyncTask extends AsyncTask<Void , String , List<String>> {
        Geocoder geocoder;
        Context context;
        String text;
        List<String> stringAddresses;

        GetAdressesAsyncTask(Context context , String text ) {
            this.text = text;
            this.context = context;
            geocoder = new Geocoder(context);

        }

        @Override
        protected List<String> doInBackground(Void... voids) {
             suggestedAddresses = new ArrayList<>();
            try {
                suggestedAddresses = geocoder.getFromLocationName(text,5);
                // for each address in the list of adresses get the address line and add it to the list
                for (Address ad:
                        suggestedAddresses) {

                    stringAddresses.add(ad.getFeatureName()+" , "+ad.getAddressLine(0));
                    // stop the progressBar
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
            return stringAddresses;

        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            if(stringAddresses!= null){
                updateAdapter(stringAddresses);
            }
        }

    }
    // called from the async task class to populate the values in the adapter
    public  void updateAdapter(List<String> listAddress){
        //stop the the progressbar
        customLoadingProgressBar.dismiss();
        arrayAdapter = new ArrayAdapter<String>(getActivity() , android.R.layout.simple_list_item_1,listAddress);
        searchResultsListView.setAdapter(arrayAdapter);

    }

    void setMarker(LatLng latLng){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnLocationSet = (OnLocationSet) getTargetFragment();
        }catch(ClassCastException e){

        }
    }

}