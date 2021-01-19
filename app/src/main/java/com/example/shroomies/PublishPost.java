package com.example.shroomies;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.make.dots.dotsindicator.DotsIndicator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class PublishPost extends Fragment implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    int currentTabPosition = 0;
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
    ImageButton cloudIconImageView;
    TextView numberOfPhotos;
    ImageView imageIconPost;
    Button publishPostButton;
    ViewPager viewPager;
    private DotsIndicator dotsIndicator;
    ImageView delteImageButton;
    ViewPagerAdapter viewPagerAdapter;
    int currentViewPagerPosition;
    private TabLayout postTabLayout;
    private RelativeLayout numberPickerRelativeLayout;
    private RelativeLayout uploadImageCardRelativeLayouyt;
    CheckBox maleCB;
    CheckBox femaleCB;
    CheckBox smokingCB;
    CheckBox petCB;
    EditText descriptionEditText;
    LottieAnimationView uploadingProgress;
    NestedScrollView nestedScrollView;
    StorageReference filePath;
    String postUniqueName;
    RelativeLayout successCard;
    Button okButton;
    String userName;



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
        successCard = v.findViewById(R.id.success_card);
        okButton = v.findViewById(R.id.ok_Button);
        numberOfRoomMatesNumberPicker = v.findViewById(R.id.roommate_number_picker);
        numberOfRoomMatesNumberPicker.setMinValue(1);
        numberOfRoomMatesNumberPicker.setMaxValue(7);
        mMapView =v.findViewById(R.id.address_map_view);
        locationEditText = v.findViewById(R.id.location_search_view);
        cloudIconImageView = v.findViewById(R.id.cloud_icon_publish_post);
        numberOfPhotos = v.findViewById(R.id.number_of_photos);
        publishPostButton =  v.findViewById(R.id.publish_post_button);
        initGoogleMap(savedInstanceState);
        numberOfPhotos = v.findViewById(R.id.number_of_photos);
        imageIconPost  = v.findViewById(R.id.image_icon_post);
        viewPager = v.findViewById(R.id.view_pager);
        dotsIndicator = v.findViewById(R.id.dotsIndicator);
        delteImageButton = v.findViewById(R.id.delete_image_post);
        postTabLayout = v.findViewById(R.id.tab_layout_publish_post);
        numberPickerRelativeLayout = v.findViewById(R.id.number_of_roommates_relative_layout);
        uploadImageCardRelativeLayouyt = v.findViewById(R.id.upload_image_card);
        maleCB = v.findViewById(R.id.male_image_button_publish_post);
        femaleCB = v.findViewById(R.id.female_image_button_post);
        smokingCB = v.findViewById(R.id.non_smoking_image_button_post);
        petCB = v.findViewById(R.id.pets_allowd_image_button_post);
        descriptionEditText = v.findViewById(R.id.description_edit_text);
        budgetNumberPicker = v.findViewById(R.id.budget_number_picker);
        uploadingProgress = v.findViewById(R.id.lottie_uploading_post);
        nestedScrollView = v.findViewById(R.id.nested);
        imageUri = new ArrayList<>();

        cloudIconImageView.setOnClickListener(new View.OnClickListener() {
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
        //close the card when ok is clicked
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                successCard.setVisibility(View.GONE);
            }
        });

        publishPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // list to hold all selected values from properties
                List<Boolean>properties = new ArrayList<>();
                properties.add(maleCB.isChecked());
                properties.add(femaleCB.isChecked());
                properties.add(petCB.isChecked());
                properties.add(smokingCB.isChecked());


                if(currentTabPosition==0) {
                    publishPost(latLng
                            , descriptionEditText.getText().toString()
                            , numberOfRoomMatesNumberPicker.getValue()
                            , budgetNumberPicker.getValue()
                            , properties
                            , imageUri);
                }else{
                    publishPersonalPost(latLng
                            , descriptionEditText.getText().toString()
                            , budgetNumberPicker.getValue()
                            , properties);
                }

            }
        });

        delteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFromView();
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageSelected(int position) {
                //set the index of the current image
                currentViewPagerPosition = position;
            }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        postTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==1){
                    //if user selects personal post remove some of the options
                    numberPickerRelativeLayout.setVisibility(View.GONE);
                    uploadImageCardRelativeLayouyt.setVisibility(View.GONE);
                    currentTabPosition=1;
                }else{
                    numberPickerRelativeLayout.setVisibility(View.VISIBLE);
                    uploadImageCardRelativeLayouyt.setVisibility(View.VISIBLE);
                    currentTabPosition=0;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }
    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        // initialize the map to be used in the getLocation method
        mMap = map;
        // check if the location has been updated form the map fragment
        // if the location is updated s
        if (MainActivity.updatedAdresses != null && MainActivity.updatedLatLng != null) {
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
        if(mMapView!=null) {
            mMapView.onSaveInstanceState(mapViewBundle);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        //locks the screen to portarait mode
        if(getActivity()!=null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


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
        if(getActivity()!=null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
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
                                        latLng = new LatLng(latitude, longitude);
                                        setMarker(latLng);
                                        setCurrentLocationAddress(latitude , longitude);
                                        // get the icon and resize it to set it as the marker
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
    void setCurrentLocationAddress(double latitude, double longitude)  {
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
            address = knownName +" , " +maddress;
            locationEditText.setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    private void getFragment (Fragment fragment) {
        fm = getActivity().getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

    private void setMarker(LatLng currentLatLng){
        // get the icon and resize it to set it as the marker
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("black_mushroom" ,  "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 90, 100, false);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(currentLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
        if(mMap!=null) {
            mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
        }

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

                    addToViewPager(selectedImageUri);
                    cloudIconImageView.startAnimation(new Animation() {
                    });


                } else {
                    Toast.makeText(getActivity(), "the image has already been uploaded", Toast.LENGTH_SHORT).show();

                }

            }
        }
    }

    void publishPost(final LatLng locationLtLng , final  String description , final int numberRoomMate , final int price , final  List<Boolean> property , final List<Uri> imageUri ) {
        uploadingProgress.setVisibility(View.VISIBLE);
        publishPostButton.setVisibility(View.INVISIBLE);
        uploadingProgress.playAnimation();
        nestedScrollView.setAlpha((float) 0.6);


        postUniqueName = getUniqueName();

        // get the referance of the database
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        //create a storage referance
        UploadTask uploadTask;
        final List<String> IMAGE_URLS = new ArrayList<>();
        for (Uri uri:
                imageUri ) {

            filePath = storageReference.child("apartment post image").child(uri.getLastPathSegment()
                    +postUniqueName+".jpg");
            filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {

                        IMAGE_URLS.add(task.getResult().getMetadata().getReference().getPath().toString());
                        // once all the pictures are added to the Storage add the rest of the posts using the urls to the database
                        if(IMAGE_URLS.size() == imageUri.size()){
                            addToRealTimeDatabase(locationLtLng ,  description ,  numberRoomMate ,  price , property ,IMAGE_URLS);
                        }
                    }else{
                        uploadingProgress.pauseAnimation();
                        uploadingProgress.setVisibility(View.GONE);
                        publishPostButton.setVisibility(View.VISIBLE);
                        nestedScrollView.setAlpha((float) 1);
                        Toast.makeText(getActivity(), "we ran into a problem uploading your photos" , Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }
    private String getUniqueName(){
        //create a unique id for the post by combining the date with uuid
        //get the date first
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat  currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        String saveCurrentDate = currentDate.format(calendarDate.getTime());

        //get the time in hours and minutes
        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        String saveCurrentTime = currentTime.format(calendarTime.getTime());

        //add the two string together

        return  saveCurrentDate+saveCurrentTime;
    }

    void addToRealTimeDatabase(LatLng locationLtLng , String description , int numberRoomMate , int price , List<Boolean> property ,List<String> IMAGE_URL){

        String userUid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        //get the username of the user
        DatabaseReference getUsername = FirebaseDatabase.getInstance().getReference("Users").child(userUid);

        getUsername.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);

                    userName = user.getName();
                    Toast.makeText(getActivity(), userName , Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        HashMap<String ,Object> post = new HashMap<>();

        post.put("description" , description);
        post.put("userID" , userUid);
        post.put("price", price);
        post.put("numberOfRoommates" , numberRoomMate);
        post.put("latitude", locationLtLng.latitude);
        post.put("longitude",locationLtLng.longitude);
        post.put("preferences" , property);
        post.put("image_url" , IMAGE_URL );
        post.put("userName" , userName);
        post.put("id" ,userUid+postUniqueName);
        // add the time of the post
        Calendar calendar = Calendar.getInstance();
        post.put("date",new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss aa").format(calendar.getTime()));

        //add image and date and user profile


        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseDatabase.child("postApartment").child(userUid+postUniqueName).setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                uploadingProgress.pauseAnimation();
                uploadingProgress.setVisibility(View.GONE);
                publishPostButton.setVisibility(View.VISIBLE);
                nestedScrollView.setAlpha((float) 1);
                if (task.isSuccessful()) {
                    successCard.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getContext(), "we faced a problem ", Toast.LENGTH_SHORT).show();
                }

            }

        });

    }

    void publishPersonalPost(LatLng locationLtLng , String description , int price , List<Boolean> property ){
        String userUid =  FirebaseAuth.getInstance().getCurrentUser().getUid();

        //get the username of the user
        DatabaseReference getUsername = FirebaseDatabase.getInstance().getReference("Users").child(userUid);

        getUsername.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);

                    userName = user.getName();
                    Toast.makeText(getActivity(), userName , Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        uploadingProgress.setVisibility(View.VISIBLE);
        publishPostButton.setVisibility(View.INVISIBLE);
        uploadingProgress.playAnimation();
        nestedScrollView.setAlpha((float) 0.6);
        HashMap<String ,Object> post = new HashMap<>();

        post.put("description" , description);
        post.put("userID" , userUid);
        post.put("price", price);
        post.put("latitude", locationLtLng.latitude);
        post.put("longitude",locationLtLng.longitude);
        post.put("preferences" , property);
        post.put("userName" , userName);
        post.put("id" , userUid+postUniqueName);

        // add the time of the post
        Calendar calendar = Calendar.getInstance();

        post.put("date",new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss aa").format(calendar.getTime()));
        //add image and date and user profile
        postUniqueName = getUniqueName();

        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseDatabase.child("postPersonal").child(userUid+postUniqueName).setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                uploadingProgress.pauseAnimation();
                uploadingProgress.setVisibility(View.GONE);
                publishPostButton.setVisibility(View.VISIBLE);
                nestedScrollView.setAlpha((float) 1);
                if (task.isSuccessful()) {
                    successCard.setVisibility(View.VISIBLE);
                }else{

                }

            }

        });

    }

    void addToViewPager(Uri newImageUri){
        imageIconPost.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        dotsIndicator.setVisibility(View.VISIBLE);
        delteImageButton.setVisibility(View.VISIBLE);
        //if no duplicate found  store the image
        imageUri.add(newImageUri);
        //set the the number of photos uploaded
        numberOfPhotos.setText(Integer.toString(imageUri.size()) + "x images");
        //initalize adapter with the list of uri
        viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUri);
        // set the view pager to the adapter
        viewPager.setAdapter(viewPagerAdapter);
        // add the indicator to the view pager and set to update on chage od dataset
        dotsIndicator.setViewPager(viewPager);
        viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
    }

    void deleteFromView(){
        if(imageUri.size()<=1){
            imageUri.remove(0);
        }else {
            imageUri.remove(currentViewPagerPosition);
        }
        //set the the number of photos uploaded
        numberOfPhotos.setText(Integer.toString(imageUri.size()) + "x images");
        //initalize adapter with the list of uri
        viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUri);
        // set the view pager to the adapter
        viewPager.setAdapter(viewPagerAdapter);
        // add the indicator to the view pager and set to update on chage od dataset
        dotsIndicator.setViewPager(viewPager);
        viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
        if (imageUri.size() == 0) {
            imageIconPost.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            dotsIndicator.setVisibility(View.GONE);
            delteImageButton.setVisibility(View.GONE);
            numberOfPhotos.setText("");
        }
        viewPager.setCurrentItem(currentViewPagerPosition);

    }


}
