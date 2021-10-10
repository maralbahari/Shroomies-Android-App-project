package com.example.shroomies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapSearchFragment extends Fragment {

    private View v;
    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private ExtendedFloatingActionButton searchButton;

    private ArrayList<Apartment> apartmentList;
    private ArrayList<PersonalPostModel> personalPosts;

    private CollectionReference apartmentPostReference;
    private CollectionReference personalPostReference;
    private ClusterManager<Apartment> apartmentClusterManager;
    private ClusterManager<PersonalPostModel> personalPostClusterManager;
    private static GoogleMap mMap;

    private LatLng currentLatLng;
    private boolean apartmentPostType = true, loading = true;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    public static final int POST_PER_PAGINATION = 50;
    final double radiusInM = 10 * 1000;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            apartmentClusterManager = new ClusterManager<>(getActivity(), mMap);
            personalPostClusterManager = new ClusterManager<>(getActivity(), mMap);
            personalPostClusterManager.setRenderer(new PersonalPostIconRendered(getActivity(), mMap, personalPostClusterManager));
            apartmentClusterManager.setRenderer(new CustomIconRendered(getActivity(), mMap, apartmentClusterManager));

            apartmentClusterManager.setOnClusterClickListener(cluster -> {
                if (cluster instanceof List) {
                    apartmentList = (ArrayList<Apartment>) cluster.getItems();
                } else {
                    apartmentList = new ArrayList<>(cluster.getItems());

                }

                ApartmentMapDialogFragment mapSearchDialog = new ApartmentMapDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(Config.APARTMENT_LIST, apartmentList);
                mapSearchDialog.setArguments(bundle);
                mapSearchDialog.show(getChildFragmentManager(), null);
                return true;
            });
            personalPostClusterManager.setOnClusterClickListener(cluster -> {
                if (cluster instanceof List) {
                    personalPosts = (ArrayList<PersonalPostModel>) cluster.getItems();
                } else {
                    personalPosts = new ArrayList<>(cluster.getItems());
                }
                PersonalPostMapDialogFragment personalPostMapDialogFragment = new PersonalPostMapDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(Config.PERSONAL_POST_LIST, personalPosts);
                personalPostMapDialogFragment.setArguments(bundle);
                personalPostMapDialogFragment.show(getChildFragmentManager(), null);
                return true;
            });

            mMap.setOnCameraIdleListener(apartmentClusterManager);
            mMap.setOnMarkerClickListener(apartmentClusterManager);
            apartmentClusterManager.setOnClusterItemClickListener(item -> {
                MapSingleDialogFragment mapApartmentDialogFragment = new MapSingleDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Config.apartment, item);
                mapApartmentDialogFragment.setArguments(bundle);
                mapApartmentDialogFragment.show(getParentFragmentManager(), null);
                return true;

            });
            personalPostClusterManager.setOnClusterItemClickListener(item -> {
                PersonalPostSingleDialogFragment personalPostSingleDialogFragment = new PersonalPostSingleDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Config.PERSONAL_POST, item);
                personalPostSingleDialogFragment.setArguments(bundle);
                personalPostSingleDialogFragment.show(getParentFragmentManager(), null);
                return true;
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
        FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();
        apartmentPostReference = mDocRef.collection(Config.APARTMENT_POST);
        personalPostReference = mDocRef.collection(Config.PERSONAL_POST);

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

        Button searchThisAreaButton = v.findViewById(R.id.search_this_area_button);
        materialButtonToggleGroup = v.findViewById(R.id.search_type_toggle_group);
        searchButton = v.findViewById(R.id.search_button);


        materialButtonToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            apartmentPostType = materialButtonToggleGroup.getCheckedButtonId() == R.id.apartment_toggle_button;
            if (mMap != null) {
                getApartments(mMap, mMap.getCameraPosition().target, false);
            }
        });

        searchButton.setOnClickListener(v -> addAutoTextIntent());

        // on click get the apartments within
        // within the specified area
        // if the camera is zoomed out
        // the camera will zoom in
        searchThisAreaButton.setOnClickListener(v -> {
            // check if the map hasn't been initialized yet
            if (mMap != null) {
                getApartments(mMap, mMap.getCameraPosition().target, false);
            }
        });


    }


    private void getApartments(final GoogleMap mMap, final LatLng latLng, boolean searchState) {
        if (loading) {
            loading = false;
            apartmentClusterManager.removeItems(apartmentClusterManager.getAlgorithm().getItems());
            personalPostClusterManager.removeItems(personalPostClusterManager.getAlgorithm().getItems());
            mMap.clear();

            final GeoLocation center = new GeoLocation(latLng.latitude, latLng.longitude);
            final List<GeoQueryBounds> bounds = new ArrayList<>(GeoFireUtils.getGeoHashQueryBounds(center, radiusInM));
            final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
            Query q;
            if (apartmentPostType) {
                for (GeoQueryBounds b : bounds) {
                    q = apartmentPostReference.orderBy(Config.GEO_HASH)
                            .orderBy(Config.TIME_STAMP, Query.Direction.ASCENDING)
                            .startAt(b.startHash)
                            .endAt(b.endHash)
                            .limit(POST_PER_PAGINATION);
                    tasks.add(q.get());
                }
            } else {
                for (GeoQueryBounds b : bounds) {
                    q = personalPostReference.orderBy(Config.GEO_HASH)
                            .orderBy(Config.TIME_STAMP, Query.Direction.ASCENDING)
                            .startAt(b.startHash)
                            .endAt(b.endHash)
                            .limit(POST_PER_PAGINATION);
                    tasks.add(q.get());
                }
            }

            // Collect all the query results together into a single list
            Tasks.whenAllComplete(tasks)
                    .addOnCompleteListener(t -> {
                        for (Task<QuerySnapshot> task : tasks) {
                            if (task.isSuccessful()) {
                                setMarkers(task.getResult(), center);
                            }
                        }

                        if (tasks.size() > 0) {
                            if (searchState) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                            } else {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                            }
                        } else {
                            new CustomToast((PublishPostActivity) getContext(), "Couldn't find posts at this location").showCustomToast();
                        }


                    }).addOnFailureListener(e -> {
                new CustomToast((MainActivity) getActivity(), "We encountered an error", R.drawable.ic_error_icon).showCustomToast();
                loading = true;
            });
        }
    }

    private void setMarkers(QuerySnapshot snap, GeoLocation center) {

        for (DocumentSnapshot doc : snap.getDocuments()) {
            double lat = doc.getDouble(Config.LATITUDE);
            double lng = doc.getDouble(Config.LONGITUDE);
            // We have to filter out a few false positives due to GeoHash
            // accuracy, but most will match
            GeoLocation docLocation = new GeoLocation(lat, lng);
            double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
            if (distanceInM <= radiusInM) {
                if (apartmentPostType) {
                    Apartment apartment = doc.toObject(Apartment.class);
                    apartment.setApartmentID(doc.getId());
                    apartmentClusterManager.addItem(apartment);
                    mMap.setOnCameraIdleListener(apartmentClusterManager);
                    mMap.setOnMarkerClickListener(apartmentClusterManager);
                    apartmentClusterManager.cluster();
                } else {
                    PersonalPostModel personalPostModel = doc.toObject(PersonalPostModel.class);
                    personalPostModel.setPostID(doc.getId());
                    personalPostClusterManager.addItem(personalPostModel);
                    mMap.setOnCameraIdleListener(personalPostClusterManager);
                    mMap.setOnMarkerClickListener(personalPostClusterManager);
                    personalPostClusterManager.cluster();
                }

            }
        }
        loading = true;
    }


    void setCurrentLatLng() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                if (locationResult == null) {
                    new CustomToast((PublishPostActivity) getContext(), "Couldn't find your current location", R.drawable.ic_error_icon).showCustomToast();
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        currentLatLng = new LatLng(latitude, longitude);
//                        // once the user's location is updated
//                        // get Apartments near him
                        getApartments(mMap, currentLatLng, false);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
                        return;
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return;
        }
        LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    private void requestPermission() {
        Dexter.withContext(getActivity())
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            setCurrentLatLng();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
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
                if (data != null) {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    if (place.getLatLng() != null) {
                        getApartments(mMap, place.getLatLng(), true);
                    } else {
                        new CustomToast((PublishPostActivity) getContext(), "Couldn't find this location", R.drawable.ic_error_icon).showCustomToast();
                    }
                } else {
                    new CustomToast((PublishPostActivity) getContext(), "We encountered an error", R.drawable.ic_error_icon).showCustomToast();

                }


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                if (data != null) {
                    Status status = Autocomplete.getStatusFromIntent(data);
                    if (status != null) {
                        Log.d("map error", status.getStatusMessage());
                    }
                }
                new CustomToast((PublishPostActivity) getContext(), "We encountered an error", R.drawable.ic_error_icon).showCustomToast();


            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


//


}

class CustomIconRendered extends DefaultClusterRenderer<Apartment> {
    private final Context context;

    public CustomIconRendered(Context context, GoogleMap map,
                              ClusterManager<Apartment> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    @Override
    protected int getColor(int clusterSize) {
        return context.getColor(R.color.LogoYellow);
    }


    @Override
    protected void onBeforeClusterItemRendered(Apartment item, MarkerOptions markerOptions) {
        markerOptions.icon(bitmapDescriptorFromVector(context));
        markerOptions.title("RM" + (item.getPrice()));
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_shroomies_yelllow_black_borders);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


}

class PersonalPostIconRendered extends DefaultClusterRenderer<PersonalPostModel> {
    private final Context context;
    GoogleMap mMap;

    public PersonalPostIconRendered(Context context, GoogleMap map,
                                    ClusterManager<PersonalPostModel> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        mMap = map;
    }

    @Override
    protected int getColor(int clusterSize) {
        return context.getColor(R.color.LogoYellow);
    }


    @Override
    protected void onBeforeClusterItemRendered(PersonalPostModel item, MarkerOptions markerOptions) {
        markerOptions.icon(bitmapDescriptorFromVector(context));
        markerOptions.title("RM" + (item.getPrice()));
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_person);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


}

