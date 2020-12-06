package com.example.shroomies;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PublishPost extends Fragment implements OnMapReadyCallback {
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private double latitude;
    private double longitude;
    View v;
    FragmentManager fm;
    FragmentTransaction ft;
    String address;
    LatLng latLng;
    private static final int PICK_IMAGE_MULTIPLE= 1;
    List<Uri> imageUri;

    NumberPicker numberOfRoomMatesNumberPicker;
    NumberPicker budgetNumberPicker;
    private CustomMapView mMapView;
    private TextView locationEditText;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap mMap;
    Geocoder geocoder;
    ImageView cloudImageView;
    TextView numberOfPhotos;





    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_publish_post, container, false);
        // clear any adresses and coordinates from the main activity


        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        numberOfRoomMatesNumberPicker = v.findViewById(R.id.roommate_number_picker);
        numberOfRoomMatesNumberPicker.setMinValue(1);
        numberOfRoomMatesNumberPicker.setMaxValue(7);
        mMapView =v.findViewById(R.id.address_map_view);
        locationEditText = v.findViewById(R.id.location_search_view);
        cloudImageView = v.findViewById(R.id.cloud_icon_publish_post);
        numberOfPhotos = v.findViewById(R.id.number_of_photos);
        initGoogleMap(savedInstanceState);
        numberOfPhotos = v.findViewById(R.id.number_of_photos);
        imageUri = new ArrayList<>();
        cloudImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the photo chosen
                openGallery();


            }
        });

        //when edit text is pressed open the map fragement
        locationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragment(new MapsFragment());
            }
        });




    }
    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        // initialize the map to be used in the getLocation method
        mMap = map;
        // check if the location has been updated form the map fragment
        // if the location is updated s
        if(MainActivity.updatedAdresses!=null&& MainActivity.updatedLatLng!=null) {
            address = MainActivity.updatedAdresses;
            latLng = MainActivity.updatedLatLng;
            setMarker(latLng);
            locationEditText.setText(address);

        } else {

            map.setMyLocationEnabled(false);
            getLocationUpdateMarker();
        }
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private  void getLocationUpdateMarker() {

        //get the location of the phone in order to set the pin on the map to the current Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        //check the API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if permission to access fine location is granted
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //get the location
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {

                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    latLng = new LatLng(latitude, longitude);
                                    setCurrentLocationAddress();
                                    setMarker(latLng);
                                }
                            }
                        });
            } else {
                // make another  request to allow the app to access location
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }


    }
    void setCurrentLocationAddress()  {
        geocoder = new Geocoder(getActivity());
            List<Address> addresses;
            geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String maddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            address = maddress +" , " +city;
            locationEditText.setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        }



        }


    private void getFragment (Fragment fragment) {
        fm = getActivity().getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }
    private void setMarker(LatLng latLng){
        // get the icon and resize it to set it as the marker
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("black_mushroom" ,  "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 90, 100, false);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
        mMap.addMarker(markerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));

    }

    private void openGallery() {
        //add permisision denied handlers
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(gallery, PICK_IMAGE_MULTIPLE);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        boolean duplicateFound = false;
        Uri selectedImageUri;
        if(imageUri.size()>5){
            Toast.makeText(getActivity(), "too many photos" , Toast.LENGTH_SHORT).show();
        }else {
            if (resultCode == getActivity().RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
                //get selected photo
                selectedImageUri = data.getData();
                //check if the selected uri already exists in tge list
                for (Uri uri : imageUri) {
                    //if duplicate found break
                    if (selectedImageUri.equals(uri)) {
                        duplicateFound = true;
                        break;
                    }
                }
                if (!duplicateFound) {
                    //if no duplicate found  store the image
                    imageUri.add(selectedImageUri);
                    //set the the number of photos uploaded
                    numberOfPhotos.setText(Integer.toString(imageUri.size()) + "x uploaded  images");
                } else {
                    Toast.makeText(getActivity(), "the image has already been uploaded", Toast.LENGTH_SHORT).show();

                }


            }
        }
    }
}

