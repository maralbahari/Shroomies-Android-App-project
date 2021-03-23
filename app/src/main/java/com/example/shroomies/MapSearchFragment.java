package com.example.shroomies;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.NumberFormat;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import  androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
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
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSearchFragment extends Fragment {
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    public static final int APARTMENT_PER_PAGINATION = 20;
    private Query query;
    private View v;
    private static GoogleMap mMap;
    private Map<Marker, Apartment> markerMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CollectionReference apartmentPostReference;
    private FirebaseFirestore mDocRef;

    private Button searchThisAreaButton;
    private LatLng currentLatLng;
    final double radiusInM = 10 * 1000;
    private AutocompleteSupportFragment autocompleteFragment;

    Toolbar toolbar;
    SearchView searchView;

    @Override
    public void onStart() {
        super.onStart();
        mDocRef = FirebaseFirestore.getInstance();
        setSearchViewVisible();
        apartmentPostReference = mDocRef.collection("postApartment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        setSearchViewGone();
    }

    @Override
    public void onPause() {
        super.onPause();
        setSearchViewGone();
    }

    @Override
    public void onStop() {
        super.onStop();
        setSearchViewGone();
    }

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
           // change the background of the marker
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window,
                            null);
                    TextView infoTitle=  v.findViewById(R.id.info_window_text);
                    infoTitle.setText(marker.getTitle());

                    return v;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    return null;
                }
            });

            // open the apartment dialog
            // to display apartment info on click of th emarker
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Apartment selectedApartment = markerMap.get(marker);
                    LatLng latLng = new LatLng(selectedApartment.getLatitude() , selectedApartment.getLongitude());
                    MapApartmentDialogFragment mapApartmentDialogFragment = new MapApartmentDialogFragment(selectedApartment);
                    mapApartmentDialogFragment.show(getParentFragmentManager() , null);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                    return false;

                }
            });

            // sets the view map to the proximity of the current location
            setCurrentLatLng();

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_map_search, container, false);
        Places.initialize(getActivity(), getString(R.string.api_key));
        return v;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);

        }

        searchThisAreaButton = v.findViewById(R.id.search_this_area_button);
        toolbar = getActivity().findViewById(R.id.toolbar);
        searchView = toolbar.findViewById(R.id.SVsearch_disc);

        // on click get the apartments within
        // within the specified area
        // if the camera is zoomed out
        // the camera will zoom in
        searchThisAreaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the map hasn't been initialized yet
                if (mMap != null) {
                    getApartments(mMap, mMap.getCameraPosition().target);
                }
            }
        });


        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAutoTextIntent();
            }
        });


    }



    private List<Apartment> getApartments(final GoogleMap mMap, final LatLng latLng) {
        final GeoLocation center = new GeoLocation(latLng.latitude, latLng.longitude);
        final List<Apartment> apartments = new ArrayList<>();

        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = apartmentPostReference
                    .orderBy("geoHash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);

            tasks.add(q.get());
        }

        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {
                        Toast.makeText(getActivity(), "found", Toast.LENGTH_SHORT).show();
                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                double lat = doc.getDouble("latitude");
                                double lng = doc.getDouble("longitude");
                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                if (distanceInM <= radiusInM) {
                                    Apartment apartment = doc.toObject(Apartment.class);
                                    apartment.setApartmentID(doc.getId());
                                    apartments.add(apartment);

                                    addMarkers(apartment, mMap);
                                }
                            }
                        }

                        if (tasks.size() > 0) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        } else {
                            //TODO add message
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        return apartments;
    }

    private void addMarkers(Apartment apartment, GoogleMap mMap) {
        // gformat the price of the unit
        // then set the title of the marker to the price;
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);
        String formattedPrice = numberFormat.format(apartment.getPrice());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("black_mushroom", "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 90, 100, false);
        LatLng latLng = new LatLng(apartment.getLatitude(), apartment.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                .title(formattedPrice)
                ;

        // show the snippet without click
        Marker locationMarker = mMap.addMarker(markerOptions);
        // add the marker and the apartment to the map
        // this enables quick access when setting an on click listener
        markerMap.put(locationMarker, apartment);
        locationMarker.showInfoWindow();
    }


    void setCurrentLatLng() {
        // set the location of the map to the current location of the user
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
            //check the API level
            //get the location
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            // once the user's location is updated
                            // get Apartments near him
                            getApartments(mMap, currentLatLng);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
                        }
                    });

        } else {
            //TODO add permission requests
        }
    }


    private void addAutoTextIntent() {
                List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.NAME);
                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(getActivity());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                getApartments(mMap , place.getLatLng());

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


    void setSearchViewVisible(){
        toolbar.findViewById(R.id.SVsearch_disc).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.search_settings).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.logo).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.logo_toolbar).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.logo).setVisibility(View.GONE);
        toolbar.findViewById(R.id.logo_toolbar).setVisibility(View.GONE);
        toolbar.findViewById(R.id.SVsearch_disc).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.search_settings).setVisibility(View.VISIBLE);

    }

    void setSearchViewGone(){
        toolbar.findViewById(R.id.SVsearch_disc).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.search_settings).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.logo).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.logo_toolbar).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.SVsearch_disc).setVisibility(View.GONE);
        toolbar.findViewById(R.id.search_settings).setVisibility(View.GONE);
        toolbar.findViewById(R.id.logo).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.logo_toolbar).setVisibility(View.VISIBLE);
    }
}